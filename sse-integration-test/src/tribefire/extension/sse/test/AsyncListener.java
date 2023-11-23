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
