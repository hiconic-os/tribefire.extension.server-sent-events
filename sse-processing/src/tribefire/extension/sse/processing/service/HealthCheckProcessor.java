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
package tribefire.extension.sse.processing.service;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.check.service.CheckRequest;
import com.braintribe.model.check.service.CheckResult;
import com.braintribe.model.check.service.CheckResultEntry;
import com.braintribe.model.check.service.CheckStatus;
import com.braintribe.model.processing.check.api.CheckProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;

import tribefire.extension.sse.api.model.event.Statistics;
import tribefire.extension.sse.processing.util.StatisticsCollector;

public class HealthCheckProcessor implements CheckProcessor {

	private static final Logger logger = Logger.getLogger(HealthCheckProcessor.class);

	private StatisticsCollector statistics;

	@Override
	public CheckResult check(ServiceRequestContext requestContext, CheckRequest request) {
		CheckResult response = CheckResult.T.create();

		Statistics _statistics = statistics.getStatistics();

		Double averageEventsMessageSize = _statistics.getAverageEventsMessageSize();
		Double averageThroughputKbPerSecond = _statistics.getAverageThroughputKbPerSecond();
		int connectedClients = _statistics.getConnectedClients();
		long eventBytesTransferred = _statistics.getEventBytesTransferred();
		long numberOfEventsSent = _statistics.getNumberOfEventsSent();
		long numberOfPingsSent = _statistics.getNumberOfPingsSent();
		long totalBytesTransferred = _statistics.getTotalBytesTransferred();
		long totalPushRequestsProcessed = _statistics.getTotalPushRequestsProcessed();

		CheckResultEntry entry = CheckResultEntry.T.create();
		entry.setCheckStatus(CheckStatus.ok);
		entry.setName("SSE Statistics");
		entry.setDetailsAsMarkdown(true);

		StringBuilder sb = new StringBuilder();
		sb.append(
				"Average Events Message Size | Average Throughput Kb Per Second | Connected Clients | Event Bytes Transferred | Number Of Events Sent | Number Of Pings Sent | Total Bytes Transferred | Total Push Requests Processed \n");
		sb.append("--- | --- | --- | --- | --- | --- | --- | --- \n");
		sb.append(String.format("%f", averageEventsMessageSize));
		sb.append("|");
		sb.append(String.format("%f", averageThroughputKbPerSecond));
		sb.append("|");
		sb.append(connectedClients);
		sb.append("|");
		sb.append(eventBytesTransferred);
		sb.append("|");
		sb.append(numberOfEventsSent);
		sb.append("|");
		sb.append(numberOfPingsSent);
		sb.append("|");
		sb.append(totalBytesTransferred);
		sb.append("|");
		sb.append(totalPushRequestsProcessed);

		entry.setDetails(sb.toString());

		logger.debug(() -> "Executed SSE health check");

		response.getEntries().add(entry);
		return response;
	}

	// -----------------------------------------------------------------------
	// GETTER & SETTER
	// -----------------------------------------------------------------------

	@Required
	@Configurable
	public void setStatistics(StatisticsCollector statistics) {
		this.statistics = statistics;
	}

}
