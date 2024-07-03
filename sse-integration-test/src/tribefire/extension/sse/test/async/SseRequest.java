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

import java.net.URI;

import org.apache.http.client.methods.HttpGet;

/**
 * Allows us to set the correct Accept header automatically and always use HTTP GET.
 */
public class SseRequest extends HttpGet {

	public SseRequest() {
		addHeader("Accept", "text/event-stream");
	}

	public SseRequest(URI uri) {
		super(uri);
		addHeader("Accept", "text/event-stream");
	}

	public SseRequest(String uri) {
		super(uri);
		addHeader("Accept", "text/event-stream");
	}

}
