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
