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
package tribefire.extension.sse.wire.space;

import com.braintribe.model.processing.deployment.api.binding.DenotationBindingBuilder;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.extension.sse.deployment.model.HealthCheckProcessor;
import tribefire.extension.sse.deployment.model.PollEndpoint;
import tribefire.extension.sse.deployment.model.SseProcessor;
import tribefire.module.wire.contract.TribefireModuleContract;
import tribefire.module.wire.contract.TribefireWebPlatformContract;

@Managed
public class SseModuleSpace implements TribefireModuleContract {

	@Import
	private TribefireWebPlatformContract tfPlatform;

	@Import
	private SseDeployablesSpace deployables;

	@Override
	public void bindHardwired() {
		tfPlatform.hardwiredExperts().addPushHandler(deployables.pushProcessor());
	}

	@Override
	public void bindDeployables(DenotationBindingBuilder bindings) {

		//@formatter:off
		
		bindings.bind(SseProcessor.T)
			.component(tfPlatform.binders().serviceProcessor())
			.expertFactory(deployables::sseProcessor);

		bindings.bind(PollEndpoint.T)
			.component(tfPlatform.binders().webTerminal())
			.expertFactory(deployables::pollEndpoint);

		bindings.bind(HealthCheckProcessor.T)
			.component(tfPlatform.binders().checkProcessor())
			.expertFactory(deployables::healthCheckProcessor);			
		//@formatter:on
	}

}
