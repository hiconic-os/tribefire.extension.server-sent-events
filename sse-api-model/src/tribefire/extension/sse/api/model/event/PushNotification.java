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
package tribefire.extension.sse.api.model.event;

import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.PushAddressing;
import com.braintribe.model.service.api.ServiceRequest;

import tribefire.extension.sse.api.model.SseRequest;

public interface PushNotification extends SseRequest, PushAddressing {

	EntityType<PushNotification> T = EntityTypes.T(PushNotification.class);

	String message = "message";

	String getMessage();
	void setMessage(String message);

	@Override
	EvalContext<NotificationResult> eval(Evaluator<ServiceRequest> evaluator);

}
