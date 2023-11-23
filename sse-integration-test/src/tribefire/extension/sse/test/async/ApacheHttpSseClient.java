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
package tribefire.extension.sse.test.async;

import java.io.IOException;
import java.nio.CharBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.client.methods.AsyncCharConsumer;
import org.apache.http.nio.client.methods.HttpAsyncMethods;
import org.apache.http.protocol.HttpContext;

public class ApacheHttpSseClient {

	private final CloseableHttpAsyncClient httpAsyncClient;
	private final ExecutorService executorService;

	public ApacheHttpSseClient(CloseableHttpAsyncClient httpAsyncClient, ExecutorService executorService) {
		this.httpAsyncClient = httpAsyncClient;
		this.executorService = executorService;
	}

	public Future<SseResponse> execute(HttpUriRequest request) {

		CompletableFuture<SseResponse> futureResp = new CompletableFuture<>();
		AsyncCharConsumer<SseResponse> charConsumer = new AsyncCharConsumer<SseResponse>() {
			private SseResponse response;

			@Override
			protected void onCharReceived(CharBuffer buf, IOControl ioctrl) throws IOException {
				// Push chars buffer to entity for parsing and storage
				response.getEntity().pushBuffer(buf, ioctrl);
			}

			@Override
			protected void onResponseReceived(HttpResponse response) {
				this.response = new SseResponse(response);
			}

			@Override
			protected SseResponse buildResult(HttpContext context) throws Exception {
				return response;
			}

			@Override
			protected void releaseResources() {
				futureResp.complete(response);
			}
		};

		executorService.submit(() -> httpAsyncClient.execute(HttpAsyncMethods.create(request), charConsumer, new FutureCallback<SseResponse>() {
			@Override
			public void completed(SseResponse result) {
				futureResp.complete(result);
			}

			@Override
			public void failed(Exception excObj) {
				futureResp.completeExceptionally(excObj);
			}

			@Override
			public void cancelled() {
				futureResp.cancel(true);
			}
		}));
		return futureResp;
	}

}
