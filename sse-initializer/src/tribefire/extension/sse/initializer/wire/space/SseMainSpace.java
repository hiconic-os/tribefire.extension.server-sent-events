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

import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.initializer.support.integrity.wire.contract.CoreInstancesContract;
import tribefire.extension.sse.initializer.wire.contract.ExistingInstancesContract;
import tribefire.extension.sse.initializer.wire.contract.RuntimePropertiesContract;
import tribefire.extension.sse.initializer.wire.contract.SseContract;
import tribefire.extension.sse.initializer.wire.contract.SseMainContract;
import tribefire.module.wire.contract.ModelApiContract;

@Managed
public class SseMainSpace implements SseMainContract {

	@Import
	private ExistingInstancesContract existingInstances;

	@Import
	private CoreInstancesContract coreInstances;

	@Import
	private RuntimePropertiesContract runtime;

	@Import
	private SseContract sse;

	@Import
	private ModelApiContract modelApi;

	@Override
	public ExistingInstancesContract existingInstances() {
		return existingInstances;
	}

	@Override
	public CoreInstancesContract coreInstances() {
		return coreInstances;
	}

	@Override
	public RuntimePropertiesContract runtime() {
		return runtime;
	}

	@Override
	public SseContract sse() {
		return sse;
	}

	@Override
	public ModelApiContract modelApi() {
		return modelApi;
	}

}
