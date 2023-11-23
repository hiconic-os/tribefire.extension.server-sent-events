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
package tribefire.extension.sse.processing.data;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.marshaller.api.HasStringCodec;
import com.braintribe.common.lcd.Pair;
import com.braintribe.logging.Logger;
import com.braintribe.model.service.api.PushRequest;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.date.NanoClock;
import com.braintribe.utils.lcd.StopWatch;

import tribefire.extension.sse.api.model.event.PollEvents;
import tribefire.extension.sse.model.PushEvent;

public class PushRequestStore {

	private static final Logger logger = Logger.getLogger(PushRequestStore.class);

	public static final DateTimeFormatter DATETIME_FORMAT = new DateTimeFormatterBuilder().optionalStart().appendPattern("yyyyMMddHHmmssSSS")
			.toFormatter();

	private AtomicInteger rollingCounter = new AtomicInteger(0);
	private AtomicLong totalCounter = new AtomicLong(0);

	private HasStringCodec codec;

	private int maxSize = 256;
	private TreeMap<String, PushEvent> events = new TreeMap<>();
	private ReadWriteLock lock = new ReentrantReadWriteLock();

	private ReentrantLock observerLock = new ReentrantLock();
	private Condition eventAdded = observerLock.newCondition();

	private volatile String lastRecordedEventId = createEventId();

	public void addPushRequest(PushRequest request) {

		StopWatch stopWatch = new StopWatch();

		ServiceRequest payload = request.getServiceRequest();

		String encodedPayload = codec.getStringCodec().encode(payload);

		stopWatch.intermediate("Encoding");

		PushEvent event = PushEvent.T.create();

		Lock writeLock = lock.writeLock();
		writeLock.lock();
		try {
			stopWatch.intermediate("Write Lock");

			String eventId = createEventId();

			event.setId(eventId);
			event.setClientIdPattern(request.getClientIdPattern());
			event.setSessionIdPattern(request.getSessionIdPattern());
			event.setRolePattern(request.getRolePattern());
			event.setPushChannelId(request.getPushChannelId());

			event.setDate(new Date());
			event.setContent(encodedPayload);

			if (events.size() == maxSize) {
				events.remove(events.firstKey());
			}
			events.put(eventId, event);

			lastRecordedEventId = eventId;
			totalCounter.incrementAndGet();

			stopWatch.intermediate("Storing with ID: " + eventId);

		} finally {
			writeLock.unlock();
		}
		observerLock.lock();
		try {
			eventAdded.signalAll();
		} finally {
			observerLock.unlock();
		}

		logger.debug(() -> "Stored PushEvent: " + stopWatch);
	}

	private String createEventId() {
		int count = rollingCounter.incrementAndGet();
		if (count == Integer.MAX_VALUE) {
			rollingCounter.set(0);
		}
		String countString = Integer.toString(count, 36);
		String countStringPrepended = StringTools.extendStringInFront(countString, '0', 6);
		String formattedDateTime = getServerTimeUtc();
		String eventId = formattedDateTime + "-" + countStringPrepended + "-" + UUID.randomUUID().toString();
		return eventId;
	}

	public Pair<String, List<PushEvent>> getPushEvents(PollEvents request, String sessionId, Set<String> roles) {

		String lastSeenId = request.getLastEventId();
		Long blockTimeoutInMs = request.getBlockTimeoutInMs();
		String clientId = request.getClientId();
		String pushChannelId = request.getPushChannelId();

		long start = System.currentTimeMillis();
		Long remainingTimeToWait = blockTimeoutInMs != null ? blockTimeoutInMs : null;

		List<PushEvent> list = null;
		if (lastSeenId == null) {
			lastSeenId = lastRecordedEventId;
		}
		String lastEventId = lastSeenId;

		list = getPushEventsInternal(lastSeenId, sessionId, roles, clientId, pushChannelId);

		while (list.isEmpty() && remainingTimeToWait != null && remainingTimeToWait > 0L) {
			observerLock.lock();
			try {
				eventAdded.await(remainingTimeToWait, TimeUnit.MILLISECONDS);
			} catch (InterruptedException ie) {
				logger.debug(() -> "Got interrupted.");
				break;
			} finally {
				observerLock.unlock();
			}
			remainingTimeToWait = blockTimeoutInMs - (System.currentTimeMillis() - start);

			list = getPushEventsInternal(lastSeenId, sessionId, roles, clientId, pushChannelId);
		}

		if (!list.isEmpty()) {
			lastEventId = list.get(list.size() - 1).getId();
		}

		return new Pair<>(lastEventId, list);
	}

	private List<PushEvent> getPushEventsInternal(String lastSeenId, String sessionId, Set<String> roles, String clientId, String pushChannelId) {
		List<PushEvent> list;
		Lock readLock = lock.readLock();
		readLock.lock();
		try {
			if (!events.isEmpty()) {
				if (events.containsKey(lastSeenId)) {
					list = new ArrayList<>(events.tailMap(lastSeenId, false).values());
				} else {
					list = new ArrayList<>(events.values());
				}

				List<PushEvent> filteredEvents = list.stream().filter(pe -> match(pe, sessionId, roles, clientId, pushChannelId))
						.collect(Collectors.toList());
				list = filteredEvents;

			} else {
				list = Collections.emptyList();
			}

		} finally {
			readLock.unlock();
		}
		return list;
	}

	private static boolean match(PushEvent pe, String sessionId, Set<String> roles, String clientId, String pushChannelId) {
		String clientIdPattern = pe.getClientIdPattern();
		if (!StringTools.isBlank(clientIdPattern)) {
			if (StringTools.isBlank(clientId) || !clientId.matches(clientIdPattern)) {
				return false;
			}
		}
		String sessionIdPattern = pe.getSessionIdPattern();
		if (!StringTools.isBlank(sessionIdPattern)) {
			if (StringTools.isBlank(sessionId) || !sessionId.matches(sessionIdPattern)) {
				return false;
			}
		}
		String eventPushChannelId = pe.getPushChannelId();
		if (!StringTools.isBlank(eventPushChannelId)) {
			if (!eventPushChannelId.equals(pushChannelId)) {
				return false;
			}
		}
		String rolePattern = pe.getRolePattern();
		if (!StringTools.isBlank(rolePattern)) {
			boolean foundMatchingRole = false;
			if (roles != null) {
				for (String role : roles) {
					if (role != null) {
						if (role.matches(rolePattern)) {
							foundMatchingRole = true;
							break;
						}
					}
				}
			}
			if (!foundMatchingRole) {
				return false;
			}
		}

		return true;
	}

	public long getPushRequestCount() {
		return totalCounter.get();
	}
	public String getServerTimeUtc() {
		Instant instant = NanoClock.INSTANCE.instant();
		ZonedDateTime dateTime = ZonedDateTime.ofInstant(instant, ZoneOffset.UTC);
		String formattedDateTime = DATETIME_FORMAT.format(dateTime);
		return formattedDateTime;
	}

	@Required
	@Configurable
	public void setCodec(HasStringCodec codec) {
		this.codec = codec;
	}
	@Configurable
	public void setMaxSize(Integer maxSizeObject) {
		if (maxSizeObject != null && maxSizeObject > 0) {
			this.maxSize = maxSizeObject;
		}
	}

}
