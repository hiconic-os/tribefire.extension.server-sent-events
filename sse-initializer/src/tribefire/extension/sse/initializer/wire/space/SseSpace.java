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
package tribefire.extension.sse.initializer.wire.space;

import java.util.Set;

import com.braintribe.logging.Logger;
import com.braintribe.model.ddra.DdraMapping;
import com.braintribe.model.extensiondeployment.check.CheckBundle;
import com.braintribe.model.extensiondeployment.meta.ProcessWith;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.constraint.TypeSpecification;
import com.braintribe.model.meta.data.prompt.Name;
import com.braintribe.model.service.domain.ServiceDomain;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.context.WireContext;

import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;
import tribefire.cortex.model.check.CheckCoverage;
import tribefire.cortex.model.check.CheckWeight;
import tribefire.extension.sse.deployment.model.HealthCheckProcessor;
import tribefire.extension.sse.deployment.model.PollEndpoint;
import tribefire.extension.sse.deployment.model.SseProcessor;
import tribefire.extension.sse.initializer.DdraMappingsBuilder;
import tribefire.extension.sse.initializer.wire.contract.ExistingInstancesContract;
import tribefire.extension.sse.initializer.wire.contract.RuntimePropertiesContract;
import tribefire.extension.sse.initializer.wire.contract.SseContract;
import tribrefire.extension.sse.common.SseCommons;

@Managed
public class SseSpace extends AbstractInitializerSpace implements SseContract, SseCommons {

	private static final Logger logger = Logger.getLogger(SseSpace.class);

	@Import
	WireContext<?> wireContext;

	@Import
	RuntimePropertiesContract runtime;

	@Import
	ExistingInstancesContract existing;

	// ***************************************************************************************************
	// Contract
	// ***************************************************************************************************

	@Override
	@Managed
	public PollEndpoint pollEndpoint() {
		PollEndpoint bean = create(PollEndpoint.T);
		bean.setExternalId(SSE_POLL_ENDPOINT_EXTERNALID);
		bean.setName(SSE_POLL_ENDPOINT_NAME);
		bean.setDomainId(DEFAULT_SSE_SERVICE_DOMAIN_ID);
		bean.setPathIdentifier("sse");
		bean.setRetry(runtime.SSE_RETRY_S());
		bean.setMaxConnectionTtlInMs(runtime.SSE_MAX_CONNECTION_TTL_MS());
		bean.setBlockTimeoutInMs(runtime.SSE_BLOCK_TIMEOUT_MS());
		bean.setEnforceSingleConnectionPerSessionId(runtime.SSE_ENFORCE_SINGLE_CONNECTION_PER_SESSION_ID());
		return bean;
	}

	@Override
	@Managed
	public SseProcessor sseProcessor() {
		SseProcessor bean = create(SseProcessor.T);
		bean.setExternalId(SSE_PROCESSOR_EXTERNALID);
		bean.setName(SSE_PROCESSOR_NAME);
		return bean;
	}

	@Managed
	@Override
	public ServiceDomain sseServiceDomain() {
		ServiceDomain bean = create(ServiceDomain.T);
		bean.setExternalId(DEFAULT_SSE_SERVICE_DOMAIN_ID);
		bean.setName(DEFAULT_SSE_SERVICE_DOMAIN_NAME);
		bean.setServiceModel(sseServiceModel());
		return bean;
	}

	@Managed
	@Override
	public GmMetaModel sseServiceModel() {
		GmMetaModel bean = create(GmMetaModel.T);
		bean.setName(SSE_CONFIGURED_API_MODEL_NAME);
		bean.setGlobalId("model:" + SSE_CONFIGURED_API_MODEL_NAME);
		bean.getDependencies().add(existing.sseApiModel());
		return bean;
	}

	@Override
	public ProcessWith processWithProcessor() {
		ProcessWith bean = create(ProcessWith.T);
		bean.setProcessor(sseProcessor());
		return bean;
	}

	@Override
	@Managed
	public TypeSpecification stringTypeSpecification() {
		TypeSpecification bean = create(TypeSpecification.T);
		bean.setType(existing.stringType());
		return bean;
	}

	@Override
	@Managed
	public Name idName() {
		Name bean = create(Name.T);
		bean.setName(LocalizedString.create("Id"));
		return bean;
	}

	@Override
	@Managed
	public Set<DdraMapping> ddraMappings() {
		//@formatter:off
			Set<DdraMapping> bean =
						new DdraMappingsBuilder(
							DEFAULT_SSE_SERVICE_DOMAIN_ID,
							this::lookup,
							this::create)
						.build();
			//@formatter:on
		return bean;
	}

	// -----------------------------------------------------------------------
	// HEALTH
	// -----------------------------------------------------------------------

	@Override
	@Managed
	public CheckBundle functionalCheckBundle() {
		CheckBundle bean = create(CheckBundle.T);
		bean.setModule(existing.module());
		bean.getChecks().add(healthCheckProcessor());
		bean.setName(DEFAULT_SSE_HEALTHZ_BUNDLE_NAME);
		bean.setWeight(CheckWeight.under1s);
		bean.setCoverage(CheckCoverage.functional);
		bean.setIsPlatformRelevant(false);

		return bean;
	}

	@Managed
	@Override
	public HealthCheckProcessor healthCheckProcessor() {
		HealthCheckProcessor bean = create(HealthCheckProcessor.T);
		bean.setModule(existing.module());
		bean.setAutoDeploy(true);
		bean.setName(DEFAULT_SSE_HEALTHZ_NAME);
		bean.setExternalId(DEFAULT_SSE_HEALTHZ_EXTERNALID);
		return bean;
	}
}
