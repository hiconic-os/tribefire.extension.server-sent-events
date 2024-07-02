// ============================================================================
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ============================================================================
package tribefire.extension.sse.processing.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.braintribe.codec.marshaller.json.JsonStreamMarshaller;
import com.braintribe.common.lcd.Numbers;
import com.braintribe.common.lcd.Pair;
import com.braintribe.exception.Exceptions;
import com.braintribe.model.notification.Notify;
import com.braintribe.model.processing.notification.api.builder.NotificationsBuilder;
import com.braintribe.model.processing.notification.api.builder.impl.BasicNotificationsBuilder;
import com.braintribe.model.service.api.PushRequest;
import com.braintribe.utils.date.NanoClock;

import tribefire.extension.sse.api.model.event.PollEvents;
import tribefire.extension.sse.model.PushEvent;

public class PushRequestStoreTest {

	private static JsonStreamMarshaller marshaller = new JsonStreamMarshaller();

	@Test
	public void testAddAndGetSimple() {
		PushRequestStore store = store();

		PollEvents intialEmptyPoll = pollEvents("testClient", "testPushChannelId", null, 1L);
		Pair<String, List<PushEvent>> initialEmptyEvents = store.getPushEvents(intialEmptyPoll, "testSessionId", Set.of("tf-admin"));

		String lastEventId = initialEmptyEvents.first();
		assertThat(initialEmptyEvents.second()).hasSize(0);

		PushRequest request = request("Hello, world", null, null, null, null);
		store.addPushRequest(request);

		PollEvents poll = pollEvents("testClient", "testPushChannelId", lastEventId, null);
		Pair<String, List<PushEvent>> events = store.getPushEvents(poll, "testSessionId", Set.of("tf-admin"));

		assertThat(events.second()).hasSize(1);

	}

	private class Reader implements Runnable {

		private List<String> receivedMessageTexts = new ArrayList<>();
		private CountDownLatch pollBarrier;
		private PushRequestStore store;
		private CountDownLatch stopBarrier;
		private String sessionId = UUID.randomUUID().toString();

		public Reader(PushRequestStore store, CountDownLatch pollBarrier, CountDownLatch stopBarrier) {
			this.store = store;
			this.pollBarrier = pollBarrier;
			this.stopBarrier = stopBarrier;

		}

		@Override
		public void run() {
			// Initialize with the first poll to get a lastEventId; this is supposed to be empty
			PollEvents intialEmptyPoll = pollEvents("testClient", "testPushChannelId", null, 1L);
			Pair<String, List<PushEvent>> initialEmptyEvents = store.getPushEvents(intialEmptyPoll, "testSessionId", Set.of("tf-admin"));

			String lastEventId = initialEmptyEvents.first();
			assertThat(initialEmptyEvents.second()).hasSize(0);

			pollBarrier.countDown();
			try {
				pollBarrier.await();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}

			try {
				while (!stopBarrier.await(1L, TimeUnit.MILLISECONDS)) {

					PollEvents poll = pollEvents("testClient", "testPushChannelId", lastEventId, 250L);
					Pair<String, List<PushEvent>> events = store.getPushEvents(poll, sessionId, Set.of("tf-admin"));

					events.second().forEach(e -> {
						String content = e.getContent();
						receivedMessageTexts.add(content);
					});

					lastEventId = events.first();
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}

		}

		public List<String> getReceivedMessageTexts() {
			return receivedMessageTexts;
		}

	}

	private class Writer implements Runnable {

		private PushRequestStore store;
		private int iterations;
		private String writerId = UUID.randomUUID().toString();
		private long waitInterval;

		public Writer(PushRequestStore store, int iterations, long waitInterval) {
			this.store = store;
			this.iterations = iterations;
			this.waitInterval = waitInterval;
		}

		@Override
		public void run() {
			for (int i = 0; i < iterations; ++i) {
				if (waitInterval > 0 && i > 0) {
					try {
						Thread.sleep(waitInterval);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						throw Exceptions.unchecked(e);
					}
				}
				PushRequest request = request("Hello, world from " + writerId + " #" + i, null, null, null, null);
				store.addPushRequest(request);

			}

		}

	}

	@Test
	public void testAddAndGetMultiThreaded() {
		PushRequestStore store = store();

		int writers = 5;
		int readers = 20;
		int messagesPerWriter = 10;
		long intervalBetweenWritesMs = 0L;
		CountDownLatch pollBarrier = new CountDownLatch(readers);
		CountDownLatch stopBarrier = new CountDownLatch(1);

		try (ExecutorService threadPool = Executors.newFixedThreadPool(readers + writers)) {

			List<Reader> readerList = new ArrayList<>();
			for (int i = 0; i < readers; ++i) {
				Reader reader = new Reader(store, pollBarrier, stopBarrier);
				readerList.add(reader);
				threadPool.submit(reader);
			}

			try {
				if (!pollBarrier.await(10L, TimeUnit.SECONDS)) {
					fail("The reader threads did not start in time.");
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				fail("Got interrupted.");
			}

			List<Writer> writerList = new ArrayList<>();
			for (int i = 0; i < writers; ++i) {
				Writer writer = new Writer(store, messagesPerWriter, intervalBetweenWritesMs);
				writerList.add(writer);
				threadPool.submit(writer);
			}

			int expectedTotalCount = readers * writers * messagesPerWriter;
			Instant start = NanoClock.INSTANCE.instant();
			while (true) {

				int totalCount = readerList.stream().mapToInt(r -> r.getReceivedMessageTexts().size()).reduce(0, Integer::sum);
				if (totalCount == expectedTotalCount) {
					stopBarrier.countDown();
					return;
				}

				Instant now = NanoClock.INSTANCE.instant();
				Duration duration = Duration.between(start, now);
				if (duration.getSeconds() > 30) {
					stopBarrier.countDown();
					fail("Test took too long.");
				}

			}
		}

	}

	private PushRequestStore store() {
		PushRequestStore store = new PushRequestStore();
		store.setMaxSize(256);
		store.setCodec(marshaller);
		return store;
	}

	private PushRequest request(String message, String clientIdPattern, String pushChannelId, String rolePattern, String sessionIdPattern) {
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

		pr.setClientIdPattern(clientIdPattern);
		pr.setPushChannelId(pushChannelId);
		pr.setRolePattern(rolePattern);
		pr.setSessionIdPattern(sessionIdPattern);

		pr.setServiceRequest(notify);
		return pr;
	}

	private PollEvents pollEvents(String clientId, String pushChannelId, String lastEventId, Long blockTimeoutInMs) {
		PollEvents pollEvents = PollEvents.T.create();
		pollEvents.setDomainId(null);
		pollEvents.setLastEventId(lastEventId);
		pollEvents.setBlockTimeoutInMs(blockTimeoutInMs != null ? blockTimeoutInMs : Numbers.MILLISECONDS_PER_SECOND * 5);

		pollEvents.setClientId(clientId);
		pollEvents.setPushChannelId(pushChannelId);

		return pollEvents;
	}
}
