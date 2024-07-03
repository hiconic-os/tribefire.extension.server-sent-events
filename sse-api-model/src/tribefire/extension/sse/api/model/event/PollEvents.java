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

import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.ServiceRequest;

import tribefire.extension.sse.api.model.SseRequest;

public interface PollEvents extends SseRequest {

	EntityType<PollEvents> T = EntityTypes.T(PollEvents.class);

	String lastEventId = "lastEventId";
	String clientId = "clientId";
	String pushChannelId = "pushChannelId";
	String blockTimeoutInMs = "blockTimeoutInMs";

	String getLastEventId();
	void setLastEventId(String lastEventId);

	String getClientId();
	void setClientId(String clientId);

	String getPushChannelId();
	void setPushChannelId(String pushChannelId);

	Long getBlockTimeoutInMs();
	void setBlockTimeoutInMs(Long blockTimeoutInMs);

	@Override
	EvalContext<Events> eval(Evaluator<ServiceRequest> evaluator);

}
