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
package tribrefire.extension.sse.common;

public interface SseCommons {

	String SSE_EXTENSION_GROUP_ID = "tribefire.extension.server-sent-events";

	String SSE_DATA_MODEL_NAME = SSE_EXTENSION_GROUP_ID + ":sse-model";
	String SSE_API_MODEL_NAME = SSE_EXTENSION_GROUP_ID + ":sse-api-model";
	String SSE_CONFIGURED_API_MODEL_NAME = SSE_EXTENSION_GROUP_ID + ":configured-sse-api-model";
	String SSE_DEPLOYMENT_MODEL_NAME = SSE_EXTENSION_GROUP_ID + ":sse-deployment-model";

	String DEFAULT_SSE_SERVICE_DOMAIN_ID = "domain.server-sent-events";
	String DEFAULT_SSE_SERVICE_DOMAIN_NAME = "SSE Service Domain";

	String SSE_PROCESSOR_EXTERNALID = "processor.server-sent-events";
	String SSE_PROCESSOR_NAME = "SSE Processor";

	String SSE_POLL_ENDPOINT_EXTERNALID = "webterminal.around.server-sent-events.poll";
	String SSE_POLL_ENDPOINT_NAME = "Server-Sent Events Endpoint";

	String SSE_PUSH_AROUND_PROCESSOR_EXTERNALID = "processor.around.server-sent-events";
	String SSE_PUSH_AROUND_PROCESSOR_NAME = "SSE Push Request AroundProcessor";

	String DEFAULT_SSE_HEALTHZ_EXTERNALID = "health.processor.server-sent-events";
	String DEFAULT_SSE_HEALTHZ_NAME = "SSE Health Check";
	String DEFAULT_SSE_HEALTHZ_BUNDLE_NAME = "SSE Checks";
}
