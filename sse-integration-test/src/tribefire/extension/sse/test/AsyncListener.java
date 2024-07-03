// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
//
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
package tribefire.extension.sse.test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;

import tribefire.extension.sse.test.async.ApacheHttpSseClient;
import tribefire.extension.sse.test.async.Event;
import tribefire.extension.sse.test.async.SseEntity;
import tribefire.extension.sse.test.async.SseRequest;
import tribefire.extension.sse.test.async.SseResponse;

public class AsyncListener implements Runnable {

	private long timeout;
	private CountDownLatch startSending = null;

	private URI uri;

	private StringBuilder receiverBuffer = new StringBuilder();
	private Exception exception = null;

	public AsyncListener(String baseUrl, String sessionId, long timeout, CountDownLatch startSending) throws URISyntaxException {
		this.timeout = timeout;
		this.startSending = startSending;

		uri = new URI(baseUrl + "/component/sse?sessionId=" + sessionId + "&clientId=IntegrationTest");
	}

	@Override
	public void run() {

		int threadPoolSize = 2;
		ExecutorService threadPool = null;

		Future<SseResponse> responseFuture = null;

		try (CloseableHttpAsyncClient asyncClient = HttpAsyncClients.createDefault()) {
			asyncClient.start();

			SseRequest request = new SseRequest(uri);
			threadPool = Executors.newFixedThreadPool(threadPoolSize);
			ApacheHttpSseClient sseClient = new ApacheHttpSseClient(asyncClient, threadPool);

			responseFuture = sseClient.execute(request);

			Thread.sleep(timeout / 2);

			startSending.countDown();

			Thread.sleep(timeout / 2 + 1);

		} catch (Exception e) {
			exception = e;
		} finally {
			if (threadPool != null) {
				threadPool.shutdownNow();
			}
		}

		if (responseFuture != null && exception == null) {
			try {
				SseResponse response = responseFuture.get();
				if (response != null) {
					SseEntity responseEntity = response.getEntity();
					BlockingQueue<Event> eventList = responseEntity.getEvents();
					for (Event eachEvent : eventList) {
						receiverBuffer.append(eachEvent);
						receiverBuffer.append("\n");
					}
				}
			} catch (Exception e) {
				exception = e;
			}
		}

	}

	public StringBuilder getReceiverBuffer() {
		return receiverBuffer;
	}

	public Exception getException() {
		return exception;
	}

}
