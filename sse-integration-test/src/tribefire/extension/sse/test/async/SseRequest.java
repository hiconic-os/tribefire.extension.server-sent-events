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
