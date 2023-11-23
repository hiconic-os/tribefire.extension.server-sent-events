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
package tribefire.extension.sse.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.model.notification.Notify;
import com.braintribe.model.processing.notification.api.builder.NotificationsBuilder;
import com.braintribe.model.processing.notification.api.builder.impl.BasicNotificationsBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.service.api.PushRequest;
import com.braintribe.model.service.api.result.PushResponse;
import com.braintribe.model.service.api.result.PushResponseMessage;
import com.braintribe.testing.category.VerySlow;

import tribefire.extension.sse.api.model.event.Events;
import tribefire.extension.sse.api.model.event.PollEvents;
import tribefire.extension.sse.model.PushEvent;
import tribrefire.extension.sse.common.SseCommons;

public class PushTest extends AbstractSseTest {

	private PersistenceGmSession session;
	private PersistenceGmSessionFactory sessionFactory;

	@Before
	public void init() {
		sessionFactory = apiFactory().buildSessionFactory();
		session = sessionFactory.newSession("cortex");
	}

	@Test
	public void testPushEventsPolling() throws Exception {

		NotificationsBuilder notificationsBuilder = new BasicNotificationsBuilder();
		//@formatter:off
		notificationsBuilder
			.add()
				.message()
				.info("Hello world")
			.close();
		//@formatter:on

		Notify notify = Notify.T.create();
		notify.setNotifications(notificationsBuilder.list());

		PushRequest pr = PushRequest.T.create();
		pr.setRolePattern(".*");
		pr.setServiceRequest(notify);
		PushResponse pushResponse = pr.eval(session).get();

		List<PushResponseMessage> responseMessages = pushResponse.getResponseMessages();
		for (PushResponseMessage m : responseMessages) {
			System.out.println(m.getMessage());
		}
	}

	@Test
	public void testPushEventsPollingWithLastSeenId() throws Exception {

		NotificationsBuilder notificationsBuilder = new BasicNotificationsBuilder();
		//@formatter:off
		notificationsBuilder
			.add()
				.message()
				.info("Hello world")
			.close();
		//@formatter:on

		Notify notify = Notify.T.create();
		notify.setNotifications(notificationsBuilder.list());

		PushRequest pr = PushRequest.T.create();
		pr.setRolePattern(".*");
		pr.setServiceRequest(notify);
		pr.eval(session).get();

		PollEvents pe = PollEvents.T.create();
		pe.setLastEventId("no-existing-id");
		pe.setDomainId(SseCommons.DEFAULT_SSE_SERVICE_DOMAIN_ID);
		Events events = pe.eval(session).get();

		List<PushEvent> list = events.getEvents();
		assertThat(list.size()).isGreaterThanOrEqualTo(1);

		String lastSeenId = events.getLastSeenId();

		pe.setLastEventId(lastSeenId);
		events = pe.eval(session).get();

		list = events.getEvents();
		assertThat(list.size()).isEqualTo(0);

		pr.eval(session).get();

		pe.setLastEventId(lastSeenId);
		events = pe.eval(session).get();

		list = events.getEvents();
		assertThat(list.size()).isEqualTo(1);
	}

	@Test
	@Category(VerySlow.class)
	public void testPushEventsAsyncListening() throws Exception {
		String baseURL = apiFactory().getURL();
		String sessionId = session.getSessionAuthorization().getSessionId();
		CountDownLatch startSending = new CountDownLatch(1);
		AsyncListener listener = new AsyncListener(baseURL, sessionId, 10000L, startSending);

		Thread t = Thread.ofVirtual().name("SSE Event Listener").start(listener);

		startSending.await(10000L, TimeUnit.MILLISECONDS);

		System.out.println("Listener started.");

		NotificationsBuilder notificationsBuilder = new BasicNotificationsBuilder();
		//@formatter:off
		notificationsBuilder
			.add()
				.message()
				.info("Hello world")
			.close();
		//@formatter:on

		Notify notify = Notify.T.create();
		notify.setNotifications(notificationsBuilder.list());

		PushRequest pr = PushRequest.T.create();
		pr.setRolePattern(".*");
		pr.setServiceRequest(notify);
		pr.eval(session).get();

		System.out.println("Push Sent.");

		t.join(15000L);

		if (listener.getException() != null) {
			throw listener.getException();
		}
		String content = listener.getReceiverBuffer().toString();
		System.out.println("Result:\n" + content);
		assertThat(content).contains("Hello world");
	}
}
