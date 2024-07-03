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
package tribefire.extension.sse.initializer;

import java.util.Set;

import com.braintribe.model.ddra.DdraMapping;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.wire.api.module.WireTerminalModule;

import tribefire.cortex.initializer.support.api.WiredInitializerContext;
import tribefire.cortex.initializer.support.impl.AbstractInitializer;
import tribefire.extension.sse.api.model.SseRequest;
import tribefire.extension.sse.initializer.wire.SseInitializerWireModule;
import tribefire.extension.sse.initializer.wire.contract.ExistingInstancesContract;
import tribefire.extension.sse.initializer.wire.contract.SseContract;
import tribefire.extension.sse.initializer.wire.contract.SseMainContract;

public class SseInitializer extends AbstractInitializer<SseMainContract> {

	@Override
	public WireTerminalModule<SseMainContract> getInitializerWireModule() {
		return SseInitializerWireModule.INSTANCE;
	}

	@Override
	public void initialize(PersistenceInitializationContext context, WiredInitializerContext<SseMainContract> initializerContext,
			SseMainContract mainContract) {

		applyModelMetaData(mainContract);
		applyApiModelMetaData(mainContract);

		SseContract sse = mainContract.sse();

		sse.sseServiceDomain();
		sse.pollEndpoint();

		sse.healthCheckProcessor();
		sse.functionalCheckBundle();

		Set<DdraMapping> ddraMappings = mainContract.existingInstances().ddraConfiguration().getMappings();
		ddraMappings.addAll(sse.ddraMappings());

	}

	private void applyModelMetaData(SseMainContract mainContract) {
		SseContract sse = mainContract.sse();
		ExistingInstancesContract existingInstances = mainContract.existingInstances();
		ModelMetaDataEditor editor = mainContract.modelApi().newMetaDataEditor(existingInstances.sseModel()).done();

		editor.onEntityType(GenericEntity.T).addPropertyMetaData(GenericEntity.id, sse.stringTypeSpecification(), sse.idName());
	}

	private void applyApiModelMetaData(SseMainContract mainContract) {
		SseContract sse = mainContract.sse();
		ModelMetaDataEditor editor = mainContract.modelApi().newMetaDataEditor(sse.sseServiceModel()).done();
		editor.onEntityType(SseRequest.T).addMetaData(sse.processWithProcessor());
	}
}
