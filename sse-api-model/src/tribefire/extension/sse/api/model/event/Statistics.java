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
package tribefire.extension.sse.api.model.event;

import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface Statistics extends GenericEntity {

	EntityType<Statistics> T = EntityTypes.T(Statistics.class);

	@Name("Total PushRequests processed")
	long getTotalPushRequestsProcessed();
	void setTotalPushRequestsProcessed(long totalPushRequestsProcessed);

	@Name("Currently Connected Clients")
	int getConnectedClients();
	void setConnectedClients(int connectedClients);

	@Name("Average Events Message Size")
	Double getAverageEventsMessageSize();
	void setAverageEventsMessageSize(Double averageEventsMessageSize);

	@Name("Average Throughput kB/s")
	Double getAverageThroughputKbPerSecond();
	void setAverageThroughputKbPerSecond(Double averageThroughputKbPerSecond);

	@Name("Total Bytes transferred")
	long getTotalBytesTransferred();
	void setTotalBytesTransferred(long lotalBytesTransferred);

	@Name("Total Bytes of Events transferred")
	long getEventBytesTransferred();
	void setEventBytesTransferred(long eventBytesTransferred);

	@Name("Number of Pings sent")
	long getNumberOfPingsSent();
	void setNumberOfPingsSent(long numberOfPingsSent);

	@Name("Number of Events sent")
	long getNumberOfEventsSent();
	void setNumberOfEventsSent(long numberOfEventsSent);

	List<ClientCount> getClientConnectionsPerSessionId();
	void setClientConnectionsPerSessionId(List<ClientCount> clientConnectionsPerSessionId);

	List<ClientCount> getClientConnectionsPerUsername();
	void setClientConnectionsPerUsername(List<ClientCount> clientConnectionsPerUsername);

	List<ClientCount> getClientConnectionsPerIpAddress();
	void setClientConnectionsPerIpAddress(List<ClientCount> clientConnectionsPerIpAddress);
}
