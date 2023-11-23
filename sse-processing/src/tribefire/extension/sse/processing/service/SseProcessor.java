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
package tribefire.extension.sse.processing.service;

import java.util.List;
import java.util.Set;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.common.lcd.Pair;
import com.braintribe.model.notification.Notify;
import com.braintribe.model.processing.notification.api.builder.NotificationsBuilder;
import com.braintribe.model.processing.notification.api.builder.impl.BasicNotificationsBuilder;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.common.context.UserSessionAspect;
import com.braintribe.model.processing.service.impl.ServiceProcessors;
import com.braintribe.model.service.api.PushRequest;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.PushResponse;
import com.braintribe.model.usersession.UserSession;

import tribefire.extension.sse.api.model.SseRequest;
import tribefire.extension.sse.api.model.event.Events;
import tribefire.extension.sse.api.model.event.GetStatistics;
import tribefire.extension.sse.api.model.event.NotificationResult;
import tribefire.extension.sse.api.model.event.PollEvents;
import tribefire.extension.sse.api.model.event.PushNotification;
import tribefire.extension.sse.api.model.event.Statistics;
import tribefire.extension.sse.model.PushEvent;
import tribefire.extension.sse.processing.data.PushRequestStore;
import tribefire.extension.sse.processing.util.StatisticsCollector;
import tribrefire.extension.sse.common.SseCommons;

public class SseProcessor implements ServiceProcessor<SseRequest, Object>, SseCommons {

	private String eventEncoding;
	private PushRequestStore pushRequestStore;
	private StatisticsCollector statistics;

	private final ServiceProcessor<ServiceRequest, Object> ddsaDispatcher = ServiceProcessors.dispatcher(config -> {
		config.register(PollEvents.T, this::poll);
		config.register(PushNotification.T, this::pushNotification);
		config.register(GetStatistics.T, this::getStatistics);
	});

	// ***************************************************************************************************
	// Setters
	// ***************************************************************************************************

	@Required
	@Configurable
	public void setEventEncoding(String eventEncoding) {
		this.eventEncoding = eventEncoding;
	}

	@Required
	@Configurable
	public void setPushRequestStore(PushRequestStore pushRequestStore) {
		this.pushRequestStore = pushRequestStore;
	}

	@Required
	@Configurable
	public void setStatistics(StatisticsCollector statistics) {
		this.statistics = statistics;
	}

	// ***************************************************************************************************
	// Processing
	// ***************************************************************************************************

	@Override
	public Object process(ServiceRequestContext requestContext, SseRequest request) {
		return ddsaDispatcher.process(requestContext, request);
	}

	protected Statistics getStatistics(ServiceRequestContext context, GetStatistics request) {
		return statistics.getStatistics();
	}

	protected NotificationResult pushNotification(ServiceRequestContext context, PushNotification request) {

		String message = request.getMessage();

		NotificationsBuilder notificationsBuilder = new BasicNotificationsBuilder();
		//@formatter:off
		notificationsBuilder
			.add()
				.message()
				.info(message)
			.close();
		//@formatter:on

		Notify notify = Notify.T.create();
		notify.setNotifications(notificationsBuilder.list());

		PushRequest pr = PushRequest.T.create();

		pr.setClientIdPattern(request.getClientIdPattern());
		pr.setPushChannelId(request.getPushChannelId());
		pr.setRolePattern(request.getRolePattern());
		pr.setSessionIdPattern(request.getSessionIdPattern());

		pr.setServiceRequest(notify);
		PushResponse pushResponse = pr.eval(context.getEvaluator()).get();

		NotificationResult result = NotificationResult.T.create();
		result.getResponseMessages().addAll(pushResponse.getResponseMessages());

		return result;
	}

	protected Events poll(ServiceRequestContext context, PollEvents request) {

		UserSession userSession = context.findAspect(UserSessionAspect.class);
		if (userSession == null) {
			throw new IllegalStateException("Could not find the user session aspect.");
		}
		String sessionId = userSession.getSessionId();
		Set<String> roles = userSession.getEffectiveRoles();

		Pair<String, List<PushEvent>> allEvents = pushRequestStore.getPushEvents(request, sessionId, roles);

		Events result = Events.T.create();
		result.setLastSeenId(allEvents.first);
		result.setEventEncoding(eventEncoding);
		result.getEvents().addAll(allEvents.second);

		return result;
	}

}
