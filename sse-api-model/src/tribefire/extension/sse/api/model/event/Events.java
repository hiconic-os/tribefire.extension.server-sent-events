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

import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

import tribefire.extension.sse.model.PushEvent;

public interface Events extends GenericEntity {

	EntityType<Events> T = EntityTypes.T(Events.class);

	String events = "events";
	String lastSeenId = "lastSeenId";
	String eventEncoding = "eventEncoding";

	List<PushEvent> getEvents();
	void setEvents(List<PushEvent> events);

	String getLastSeenId();
	void setLastSeenId(String lastSeenId);

	String getEventEncoding();
	void setEventEncoding(String eventEncoding);
}
