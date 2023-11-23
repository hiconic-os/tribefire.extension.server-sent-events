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
package tribefire.extension.sse.processing.util;

public class ConnectedClient {

	private String connectionId;
	private String clientId;
	private String lastSeenId;
	private String clientIp;
	private String username;
	private String sessionId;

	public ConnectedClient(String connectionId, String clientId, String lastSeenId, String clientIp, String username, String sessionId) {
		this.connectionId = connectionId;
		this.clientId = clientId;
		this.lastSeenId = lastSeenId;
		this.clientIp = clientIp;
		this.username = username;
		this.sessionId = sessionId;
	}

	public String getConnectionId() {
		return connectionId;
	}

	public String getClientId() {
		return clientId;
	}

	public String getLastSeenId() {
		return lastSeenId;
	}

	public String getClientIp() {
		return clientIp;
	}

	public String getUsername() {
		return username;
	}

	public String getSessionId() {
		return sessionId;
	}

}
