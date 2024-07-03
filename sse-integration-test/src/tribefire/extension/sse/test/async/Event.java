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

public class Event {

	private final String id;
	private final String event;
	private final String data;
	private final int retry;

	public Event(String id, String event, String data, int retry) {
		this.id = id;
		this.event = event;
		this.data = data;
		this.retry = retry;
	}

	public String getId() {
		return id;
	}

	public String getEvent() {
		return event;
	}

	public String getData() {
		return data;
	}

	public int getRetry() {
		return retry;
	}

	@Override
	public String toString() {
		StringBuilder eventString = new StringBuilder();
		if (id != null && id.length() > 0) {
			eventString.append("id: ");
			eventString.append(id);
		}
		if (event != null && event.length() > 0) {
			eventString.append("\nevent: ");
			eventString.append(event);
		}
		if (data != null && data.length() > 0) {
			eventString.append("\ndata: ");
			eventString.append(data);
		}
		if (retry != 0) {
			eventString.append("\nretry: ");
			eventString.append(retry);
		}
		return eventString.toString();
	}

}
