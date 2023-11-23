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
