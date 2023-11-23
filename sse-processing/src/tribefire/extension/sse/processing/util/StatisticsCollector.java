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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.braintribe.common.lcd.Numbers;
import com.braintribe.model.service.api.PushRequest;

import tribefire.extension.sse.api.model.event.ClientCount;
import tribefire.extension.sse.api.model.event.Statistics;

// IDEAS:
// - Top clientIds or clientId patterns
// - Ratio of seen/unseen events

public class StatisticsCollector {

	private AtomicLong totalPushCount = new AtomicLong(0);

	private AtomicInteger currentClientCount = new AtomicInteger(0);
	private AtomicLong totalPingsSentToClients = new AtomicLong();
	private AtomicLong totalEventsSentToClients = new AtomicLong();

	private AtomicLong totalBytesTransferred = new AtomicLong();
	private AtomicLong totalEventBytesTransferred = new AtomicLong();

	private long start = System.currentTimeMillis();

	private ConcurrentHashMap<String, ConnectedClient> clientsPerConnectionId = new ConcurrentHashMap<>();

	public void registerPushRequest(@SuppressWarnings("unused") PushRequest request) {
		totalPushCount.incrementAndGet();
	}

	public void registerPollConnection(String connectionId, String clientId, String lastSeenId, String clientIp, String username, String sessionId) {
		currentClientCount.incrementAndGet();

		ConnectedClient client = new ConnectedClient(connectionId, clientId, lastSeenId, clientIp, username, sessionId);
		clientsPerConnectionId.put(connectionId, client);
	}

	public void unregisterPollConnection(String connectionId) {
		currentClientCount.decrementAndGet();
		clientsPerConnectionId.remove(connectionId);
	}

	public void registerServletPing(@SuppressWarnings("unused") String connectionId, int messageSizeInBytes) {
		totalPingsSentToClients.incrementAndGet();
		totalBytesTransferred.addAndGet(messageSizeInBytes);
	}

	public void registerServletEvents(@SuppressWarnings("unused") String connectionId, int numberOfEvents, int messageSizeInBytes) {
		totalEventsSentToClients.addAndGet(numberOfEvents);
		totalBytesTransferred.addAndGet(messageSizeInBytes);
		totalEventBytesTransferred.addAndGet(messageSizeInBytes);
	}

	public Statistics getStatistics() {
		Statistics result = Statistics.T.create();

		result.setTotalPushRequestsProcessed(totalPushCount.get());

		int count = currentClientCount.get();
		if (count < 0) {
			count = 0;
			currentClientCount.set(0);
		}
		result.setConnectedClients(count);

		result.setNumberOfPingsSent(totalPingsSentToClients.get());

		long sizeInBytes = totalBytesTransferred.get();
		result.setTotalBytesTransferred(sizeInBytes);

		result.setEventBytesTransferred(totalEventBytesTransferred.get());

		long durationInSeconds = (System.currentTimeMillis() - start) / Numbers.MILLISECONDS_PER_SECOND;
		if (durationInSeconds > 0) {
			double bytesPerSecond = ((double) sizeInBytes) / ((double) durationInSeconds);
			result.setAverageThroughputKbPerSecond(bytesPerSecond / Numbers.KILOBYTE);
		} else {
			result.setAverageThroughputKbPerSecond(0d);
		}

		long totalEventsSent = totalEventsSentToClients.get();
		long eventsSizeInBytes = totalEventBytesTransferred.get();
		if (totalEventsSent > 0) {
			double averageSize = ((double) eventsSizeInBytes) / ((double) totalEventsSent);
			result.setAverageEventsMessageSize(averageSize);
		} else {
			result.setAverageEventsMessageSize(0d);
		}

		result.setNumberOfEventsSent(totalEventsSent);

		result.setClientConnectionsPerSessionId(clientsPerIdentifier(ConnectedClient::getSessionId));
		result.setClientConnectionsPerUsername(clientsPerIdentifier(ConnectedClient::getUsername));
		result.setClientConnectionsPerIpAddress(clientsPerIdentifier(ConnectedClient::getClientIp));

		return result;
	}

	private List<ClientCount> clientsPerIdentifier(Function<ConnectedClient, String> identiferGetter) {

		Set<ConnectedClient> clients = new HashSet<>(clientsPerConnectionId.values());

		Map<String, Integer> countMap = new HashMap<>();
		clients.forEach(c -> {
			String identifier = identiferGetter.apply(c);
			if (identifier != null) {
				Integer count = countMap.computeIfAbsent(identifier, __ -> Integer.valueOf(0));
				countMap.put(identifier, count + 1);
			}
		});

		List<ClientCount> result = countMap.entrySet().stream().map(e -> {
			ClientCount cc = ClientCount.T.create();
			cc.setIdentifier(e.getKey());
			cc.setCount(e.getValue());
			return cc;
		}).sorted((cc1, cc2) -> Integer.compare(cc2.getCount(), cc1.getCount())).collect(Collectors.toList());

		return result;
	}
}
