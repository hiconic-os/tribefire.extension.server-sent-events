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
