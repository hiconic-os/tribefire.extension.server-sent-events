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
package tribefire.extension.sse.wire.space;

import com.braintribe.codec.marshaller.api.HasStringCodec;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.deployment.api.ExpertContext;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.extension.sse.deployment.model.PollEndpoint;
import tribefire.extension.sse.processing.data.PushRequestStore;
import tribefire.extension.sse.processing.service.HealthCheckProcessor;
import tribefire.extension.sse.processing.service.SseProcessor;
import tribefire.extension.sse.processing.service.SsePushProcessor;
import tribefire.extension.sse.processing.servlet.SseServer;
import tribefire.extension.sse.processing.util.StatisticsCollector;
import tribefire.module.wire.contract.ModuleReflectionContract;
import tribefire.module.wire.contract.ModuleResourcesContract;
import tribefire.module.wire.contract.TribefireWebPlatformContract;
import tribrefire.extension.sse.common.SseCommons;

@Managed
public class SseDeployablesSpace implements WireSpace, SseCommons {

	@Import
	private TribefireWebPlatformContract tfPlatform;

	@Import
	private ModuleResourcesContract moduleResources;

	@Import
	private ModuleReflectionContract module;

	// ***************************************************************************************************
	// Public Managed Beans
	// ***************************************************************************************************

	@Managed
	public SseProcessor sseProcessor(ExpertContext<tribefire.extension.sse.deployment.model.SseProcessor> context) {
		tribefire.extension.sse.deployment.model.SseProcessor deployable = context.getDeployable();
		SseProcessor bean = new SseProcessor();
		bean.setEventEncoding("application/json");
		bean.setPushRequestStore(pushRequestStore());
		bean.setStatistics(statisticsCollector());
		return bean;
	}

	@Managed
	public SsePushProcessor pushProcessor() {
		SsePushProcessor bean = new SsePushProcessor();

		bean.setPushRequestStore(pushRequestStore());
		bean.setStatistics(statisticsCollector());
		return bean;
	}

	@Managed
	private StatisticsCollector statisticsCollector() {
		StatisticsCollector bean = new StatisticsCollector();
		return bean;
	}

	@Managed
	public SseServer pollEndpoint(ExpertContext<PollEndpoint> context) {
		PollEndpoint deployable = context.getDeployable();

		SseServer bean = new SseServer();
		bean.setMarshallerRegistry(tfPlatform.marshalling().registry());
		bean.setRequestEvaluator(tfPlatform.requestUserRelated().evaluator());
		bean.setDomainId(deployable.getDomainId());
		bean.setRetry(deployable.getRetry());
		bean.setMaxConnectionTtlInMs(deployable.getMaxConnectionTtlInMs());
		bean.setPushRequestStore(pushRequestStore());
		bean.setBlockTimeoutInMs(deployable.getBlockTimeoutInMs());
		bean.setRemoteAddressResolver(tfPlatform.servlets().remoteAddressResolver());
		bean.setStatistics(statisticsCollector());
		bean.setEnforceSingleConnectionPerSessionId(deployable.getEnforceSingleConnectionPerSessionId());

		return bean;
	}

	@Managed
	private PushRequestStore pushRequestStore() {
		PushRequestStore bean = new PushRequestStore();
		bean.setCodec((HasStringCodec) tfPlatform.marshalling().jsonMarshaller());
		String maxSizeString = TribefireRuntime.getProperty("SSE_STORAGE_SIZE", "256");
		bean.setMaxSize(Integer.parseInt(maxSizeString));
		return bean;
	}

	// -----------------------------------------------------------------------
	// HEALTH
	// -----------------------------------------------------------------------

	@Managed
	public HealthCheckProcessor healthCheckProcessor(
			@SuppressWarnings("unused") ExpertContext<tribefire.extension.sse.deployment.model.HealthCheckProcessor> context) {
		HealthCheckProcessor bean = new HealthCheckProcessor();
		bean.setStatistics(statisticsCollector());
		return bean;
	}

}
