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
package tribefire.extension.sse.initializer.wire.contract;

import java.util.Set;

import com.braintribe.model.ddra.DdraMapping;
import com.braintribe.model.extensiondeployment.check.CheckBundle;
import com.braintribe.model.extensiondeployment.meta.ProcessWith;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.service.domain.ServiceDomain;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.extension.sse.deployment.model.HealthCheckProcessor;
import tribefire.extension.sse.deployment.model.PollEndpoint;
import tribefire.extension.sse.deployment.model.SseProcessor;

public interface SseContract extends WireSpace {

	SseProcessor sseProcessor();

	ProcessWith processWithProcessor();

	PollEndpoint pollEndpoint();

	MetaData stringTypeSpecification();

	MetaData idName();

	ServiceDomain sseServiceDomain();

	Set<DdraMapping> ddraMappings();

	GmMetaModel sseServiceModel();

	CheckBundle functionalCheckBundle();

	HealthCheckProcessor healthCheckProcessor();

}
