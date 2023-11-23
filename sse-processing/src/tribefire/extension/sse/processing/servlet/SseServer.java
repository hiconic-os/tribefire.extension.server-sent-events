// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package tribefire.extension.sse.processing.servlet;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.marshaller.api.HasStringCodec;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.codec.marshaller.api.MarshallerRegistry;
import com.braintribe.common.lcd.Numbers;
import com.braintribe.exception.Exceptions;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.security.reason.InvalidSession;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.processing.service.common.context.UserSessionAspect;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.user.User;
import com.braintribe.model.usersession.UserSession;
import com.braintribe.util.servlet.remote.RemoteClientAddressResolver;
import com.braintribe.utils.RandomTools;
import com.braintribe.utils.collection.impl.AttributeContexts;
import com.braintribe.utils.lcd.StopWatch;
import com.braintribe.utils.lcd.StringTools;
import com.braintribe.utils.stream.CountingPrintWriter;

import tribefire.extension.sse.api.model.event.Events;
import tribefire.extension.sse.api.model.event.PollEvents;
import tribefire.extension.sse.model.PushEvent;
import tribefire.extension.sse.processing.data.PushRequestStore;
import tribefire.extension.sse.processing.util.StatisticsCollector;

public class SseServer extends HttpServlet {

	private static final Logger logger = Logger.getLogger(SseServer.class);

	private static final long serialVersionUID = 1L;

	private String domainId;

	private Evaluator<ServiceRequest> evaluator;

	private MarshallerRegistry marshallerRegistry;

	private Integer retry = null;

	private Long maxConnectionTtlInMs = (long) Numbers.MILLISECONDS_PER_HOUR;
	private Long blockTimeoutInMs = (long) Numbers.MILLISECONDS_PER_SECOND * 30;

	private PushRequestStore pushRequestStore;
	private StatisticsCollector statistics;

	private RemoteClientAddressResolver remoteAddressResolver;

	private static ConcurrentHashMap<String, String> connectionIdPerSessionIds = new ConcurrentHashMap<>();
	private static ReentrantLock lock = new ReentrantLock();
	private boolean enforceSingleConnectionPerSessionId = false;

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {

		String lastEventId = req.getHeader("Last-Event-ID");
		String clientId = req.getParameter("clientId");
		String pushChannelId = req.getParameter("pushChannelId");

		String username = getUsername();
		String clientIp = (remoteAddressResolver != null) ? remoteAddressResolver.getRemoteIpLenient(req) : null;
		String connectionId = (clientId != null ? clientId : "anonymous") + "-" + RandomTools.newStandardUuid()
				+ (clientIp != null ? "@" + clientIp : "") + (username != null ? "-" + username : "");
		UserSession userSession = getUserSession();
		String sessionId = userSession != null ? userSession.getSessionId() : null;

		resp.setCharacterEncoding("UTF-8");
		resp.setContentType("text/event-stream");
		resp.setHeader("Cache-Control", "no-store");
		resp.setHeader("X-Accel-Buffering", "no");

		long start = System.currentTimeMillis();
		boolean debug = logger.isDebugEnabled();

		if (debug) {
			logger.debug("Received a new SSE connection (" + connectionId + ") with parameters lastEventId: " + lastEventId + ", clientId: "
					+ clientId + ", pushChannelId: " + pushChannelId);
		}

		if (sessionId != null) {
			lock.lock();
			try {
				connectionIdPerSessionIds.put(sessionId, connectionId);
			} finally {
				lock.unlock();
			}
		}

		CountingPrintWriter writer = new CountingPrintWriter(resp.getWriter());
		try {
			statistics.registerPollConnection(connectionId, clientId, lastEventId, clientIp, username, sessionId);

			while (true) {

				writer.resetCount();
				StopWatch stopWatch = new StopWatch();

				PollEvents pollEvents = PollEvents.T.create();
				pollEvents.setDomainId(domainId);
				pollEvents.setLastEventId(lastEventId);
				pollEvents.setBlockTimeoutInMs(blockTimeoutInMs);

				pollEvents.setClientId(clientId);
				pollEvents.setPushChannelId(pushChannelId);

				Maybe<Events> eventsMaybe = pollEvents.eval(evaluator).getReasoned();
				// This is kept for backward compatibility. In newer TF versions, this is already a AuthorizedWebTerminal.
				if (eventsMaybe.isUnsatisfiedBy(InvalidSession.T)) {
					if (!resp.isCommitted()) {
						resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
					}
					logger.info(() -> "Client " + connectionId + " does not use a valid session.");
					break;
				}
				Events events = eventsMaybe.get();

				stopWatch.intermediate("Poll Events");

				String newLastSeenId = events.getLastSeenId();
				if (newLastSeenId != null) {
					lastEventId = newLastSeenId;
				}

				List<PushEvent> newEvents = events.getEvents();
				if (!newEvents.isEmpty()) {
					if (debug) {
						logger.debug("Sending " + newEvents.size() + " events to (" + connectionId + ")");
					}

					for (PushEvent pe : newEvents) {
						String getData = convertData(pe.getContent(), events.getEventEncoding(), "application/json");
						List<String> lines = StringTools.getLines(getData);

						writer.write("event: PushRequest\n");
						writer.write("id: " + pe.getId() + "\n");
						for (String line : lines) {
							writer.write("data: " + line + "\n");
						}
					}

					stopWatch.intermediate("Sending " + newEvents.size() + " Events");

				} else {
					writer.write("event: ping\n");
					writer.write("data: {\"serverTimeUTC\": " + pushRequestStore.getServerTimeUtc() + ", \"totalRequests\": "
							+ pushRequestStore.getPushRequestCount() + "}\n");

					stopWatch.intermediate("Sending Ping");
				}
				if (retry != null) {
					writer.write("retry: " + retry + "\n");
				}
				writer.write("\n");
				writer.flush();
				resp.flushBuffer();

				stopWatch.intermediate("Flushing");

				if (newEvents.isEmpty()) {
					statistics.registerServletPing(connectionId, writer.getCount());
				} else {
					statistics.registerServletEvents(connectionId, newEvents.size(), writer.getCount());
				}

				if (writer.checkError()) {
					logger.debug(() -> "Error while sending data to connection (" + connectionId + ") clientId: " + clientId + ", pushChannelId: "
							+ pushChannelId);
					break;
				}

				if (maxConnectionTtlInMs != null) {
					long lifetime = System.currentTimeMillis() - start;
					if (lifetime > maxConnectionTtlInMs) {
						logger.debug(() -> "Max TTL of connection (" + connectionId + ") reached for clientId " + clientId + ", pushChannelId: "
								+ pushChannelId);
						break;
					}
				}

				if (!newEvents.isEmpty()) {
					logger.debug(() -> "Sending of events to connection (" + connectionId + ") took: " + stopWatch);
				} else {
					logger.trace(() -> "Sending of events to connection (" + connectionId + ") took: " + stopWatch);
				}

				if (enforceSingleConnectionPerSessionId && !isSessionOwner(sessionId, connectionId)) {
					logger.info(() -> "Another thread is serving session Id " + sessionId + " (" + username + ")");
					// Another thread has taken over this sessionId
					break;
				}
			}
		} catch (IOException ioe) {
			String message = "Got an IO Exception: " + ioe.getMessage() + ". Terminating connection " + connectionId;
			if (logger.isTraceEnabled()) {
				logger.trace(() -> message, ioe);
			}
			if (debug) {
				logger.debug(message);
			}
		} catch (Exception e) {
			throw Exceptions.unchecked(e);
		} finally {
			if (debug) {
				logger.debug("Terminated SSE connection (" + connectionId + ")");
			}
			statistics.unregisterPollConnection(connectionId);

			lock.lock();
			try {
				if (isSessionOwner(sessionId, connectionId)) {
					connectionIdPerSessionIds.remove(sessionId);
				}
			} finally {
				lock.unlock();
			}
		}

	}

	private boolean isSessionOwner(String sessionId, String myConnectionId) {
		if (sessionId != null) {
			String connectionIdForSessionId = connectionIdPerSessionIds.get(sessionId);
			if (connectionIdForSessionId != null && !myConnectionId.equals(connectionIdForSessionId)) {
				// Another thread has taken over this sessionId
				return false;
			}
		}
		return true;
	}

	private UserSession getUserSession() {
		try {
			UserSession userSession = AttributeContexts.peek().findOrNull(UserSessionAspect.class);
			return userSession;
		} catch (Exception e) {
			if (logger.isTraceEnabled()) {
				logger.debug("Could not determine current user.", e);
			} else {
				logger.debug(() -> "Could not determine current user.");
			}
		}
		return null;
	}

	private String getUsername() {
		try {
			UserSession userSession = AttributeContexts.peek().findOrNull(UserSessionAspect.class);
			if (userSession != null) {
				User user = userSession.getUser();
				if (user != null) {
					return user.getName();
				}
			}
		} catch (Exception e) {
			if (logger.isTraceEnabled()) {
				logger.debug("Could not determine current user.", e);
			} else {
				logger.debug(() -> "Could not determine current user.");
			}
		}
		return null;
	}

	private String convertData(String content, String eventEncoding, String accepted) {
		if (StringTools.isBlank(accepted) || accepted.equalsIgnoreCase(eventEncoding)) {
			return content;
		}
		Marshaller sourceMarshaller = marshallerRegistry.getMarshaller(eventEncoding);
		ServiceRequest payload = (ServiceRequest) sourceMarshaller.unmarshall(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
		Marshaller targetMarshaller = marshallerRegistry.getMarshaller(accepted);
		if (!(targetMarshaller instanceof HasStringCodec)) {
			throw new IllegalStateException("Could not use marshaller for " + accepted);
		}
		HasStringCodec hsc = (HasStringCodec) targetMarshaller;
		String encoded = hsc.getStringCodec().encode(payload);
		return encoded;
	}

	@Configurable
	@Required
	public void setRequestEvaluator(Evaluator<ServiceRequest> evaluator) {
		this.evaluator = evaluator;
	}
	@Required
	@Configurable
	public void setMarshallerRegistry(MarshallerRegistry marshallerRegistry) {
		this.marshallerRegistry = marshallerRegistry;
	}
	@Configurable
	public void setRetry(Integer retry) {
		this.retry = retry;
	}
	@Configurable
	public void setMaxConnectionTtlInMs(Long maxConnectionTtlInMs) {
		if (maxConnectionTtlInMs != null) {
			this.maxConnectionTtlInMs = maxConnectionTtlInMs;
		}
	}
	@Configurable
	@Required
	public void setDomainId(String domainId) {
		this.domainId = domainId;
	}

	@Required
	@Configurable
	public void setPushRequestStore(PushRequestStore pushRequestStore) {
		this.pushRequestStore = pushRequestStore;
	}
	@Configurable
	public void setBlockTimeoutInMs(Long blockTimeoutInMs) {
		if (blockTimeoutInMs != null) {
			this.blockTimeoutInMs = blockTimeoutInMs;
		}
	}
	@Required
	@Configurable
	public void setStatistics(StatisticsCollector statistics) {
		this.statistics = statistics;
	}
	@Required
	@Configurable
	public void setRemoteAddressResolver(RemoteClientAddressResolver remoteAddressResolver) {
		this.remoteAddressResolver = remoteAddressResolver;
	}
	@Required
	@Configurable
	public void setEnforceSingleConnectionPerSessionId(Boolean enforceSingleConnectionPerSessionId) {
		if (enforceSingleConnectionPerSessionId != null) {
			this.enforceSingleConnectionPerSessionId = enforceSingleConnectionPerSessionId;
		}
	}

}
