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
