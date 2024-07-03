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
