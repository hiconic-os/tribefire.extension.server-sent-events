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

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import com.braintribe.model.ddra.DdraMapping;
import com.braintribe.model.ddra.DdraUrlMethod;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.service.api.ServiceRequest;

import tribefire.extension.sse.api.model.event.GetStatistics;
import tribefire.extension.sse.api.model.event.PollEvents;
import tribefire.extension.sse.api.model.event.PushNotification;

public class DdraMappingsBuilder {

	private static final String RESOURCE = "resource";
	private static final String REACHABLE = "reachable";

	public static final String TAG_PUSH_REQUESTS = "Push Requests";

	protected Set<DdraMapping> mappings = new HashSet<>();
	protected Function<String, GmEntityType> typeLookup;
	protected String accessId;
	protected Function<EntityType<?>, GenericEntity> instanceFactory;

	public DdraMappingsBuilder(String domainId, Function<String, GmEntityType> typeLookup, Function<EntityType<?>, GenericEntity> instanceFactory) {
		this.typeLookup = typeLookup;
		this.accessId = domainId;
		this.instanceFactory = instanceFactory;
	}

	public Set<DdraMapping> build() {
		this.configureMappings();
		return mappings;
	}

	// ***************************************************************************************************
	// Helper
	// ***************************************************************************************************

	protected void configureMappings() {
		//@formatter:off

		mappings.add(ddraMapping(
				"events",
				DdraUrlMethod.GET,
				PollEvents.T,
				TAG_PUSH_REQUESTS,
				null,
				REACHABLE));

		mappings.add(ddraMapping(
				"push-notification",
				DdraUrlMethod.POST,
				PushNotification.T,
				TAG_PUSH_REQUESTS,
				null,
				REACHABLE));

		mappings.add(ddraMapping(
				"statistics",
				DdraUrlMethod.GET,
				GetStatistics.T,
				TAG_PUSH_REQUESTS,
				null,
				REACHABLE));

		//@formatter:on
	}

	protected DdraMapping ddraMapping(String relativePath, DdraUrlMethod method, EntityType<? extends ServiceRequest> reflectionType, String tag) {
		return ddraMapping(relativePath, method, reflectionType, tag, 1, null, false, null);
	}

	protected DdraMapping ddraMapping(String relativePath, DdraUrlMethod method, EntityType<? extends ServiceRequest> reflectionType, String tag,
			String projection) {
		return ddraMapping(relativePath, method, reflectionType, tag, projection, null);
	}

	protected DdraMapping ddraMapping(String relativePath, DdraUrlMethod method, EntityType<? extends ServiceRequest> reflectionType, String tag,
			String projection, String depth) {
		return ddraMapping(relativePath, method, reflectionType, tag, 1, projection, method == DdraUrlMethod.POST, depth);
	}

	protected DdraMapping ddraMapping(String relativePath, DdraUrlMethod method, EntityType<? extends ServiceRequest> reflectionType, String tag,
			int version, String projection, boolean announceAsMultipart, String depth) {
		GmEntityType type = this.typeLookup.apply("type:" + reflectionType.getTypeSignature());
		String path = "/" + accessId + "/v" + version + "/" + relativePath;

		DdraMapping bean = (DdraMapping) this.instanceFactory.apply(DdraMapping.T);
		bean.setGlobalId("ddra:/" + method + "/" + path + "/" + accessId);
		bean.setPath(path);
		bean.setRequestType(type);
		bean.setMethod(method);
		bean.setDefaultServiceDomain(accessId);
		bean.setDefaultMimeType("application/json");
		bean.setHideSerializedRequest(true);
		bean.setAnnounceAsMultipart(announceAsMultipart);
		if (projection != null) {
			bean.setDefaultProjection(projection);
		}

		if (tag != null) {
			bean.getTags().add(tag);
		}

		if (depth != null) {
			bean.setDefaultDepth(depth);
		}

		bean.setDefaultUseSessionEvaluation(false);
		bean.setDefaultEntityRecurrenceDepth(-1);

		return bean;
	}

	private DdraMapping ddraDownloadMapping(String relativePath, EntityType<? extends ServiceRequest> reflectionType, String tag, String projection) {
		DdraMapping downloadMapping = ddraMapping(relativePath, DdraUrlMethod.GET, reflectionType, tag);
		downloadMapping.setDefaultProjection(projection);
		downloadMapping.setDefaultDownloadResource(true);
		downloadMapping.setDefaultSaveLocally(true);
		return downloadMapping;
	}

}
