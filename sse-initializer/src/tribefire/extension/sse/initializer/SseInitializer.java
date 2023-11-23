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
