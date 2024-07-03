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
