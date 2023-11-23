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
package tribefire.extension.sse.processing.service;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.service.api.InternalPushRequest;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.PushResponse;
import com.braintribe.model.service.api.result.PushResponseMessage;

import tribefire.extension.sse.processing.data.PushRequestStore;
import tribefire.extension.sse.processing.util.StatisticsCollector;
import tribrefire.extension.sse.common.SseCommons;

public class SsePushProcessor implements ServiceProcessor<InternalPushRequest, PushResponse>, SseCommons {

	private PushRequestStore pushRequestStore;
	private StatisticsCollector statistics;

	public static final DateTimeFormatter DATETIME_FORMAT = new DateTimeFormatterBuilder().optionalStart().appendPattern("yyyyMMddHHmmssSSS")
			.toFormatter();

	// ***************************************************************************************************
	// Setters
	// ***************************************************************************************************

	@Required
	@Configurable
	public void setPushRequestStore(PushRequestStore pushRequestStore) {
		this.pushRequestStore = pushRequestStore;
	}

	@Required
	@Configurable
	public void setStatistics(StatisticsCollector statistics) {
		this.statistics = statistics;
	}

	// ***************************************************************************************************
	// Processing
	// ***************************************************************************************************

	@Override
	public PushResponse process(ServiceRequestContext requestContext, InternalPushRequest request) {
		ServiceRequest payload = request.getServiceRequest();

		PushResponse result = PushResponse.T.create();
		
		if (payload != null) {
			pushRequestStore.addPushRequest(request);
			statistics.registerPushRequest(request);
			PushResponseMessage msg = PushResponseMessage.T.create();
			msg.setMessage("Stored PushRequest");
			msg.setSuccessful(true);
			
			result.getResponseMessages().add(msg);
		}

		return result;
	}

}
