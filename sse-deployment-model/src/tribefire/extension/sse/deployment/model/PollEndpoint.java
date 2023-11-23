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
package tribefire.extension.sse.deployment.model;

import com.braintribe.model.extensiondeployment.AuthorizedWebTerminal;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface PollEndpoint extends AuthorizedWebTerminal {

	final EntityType<PollEndpoint> T = EntityTypes.T(PollEndpoint.class);

	@Mandatory
	@Name("Domain Id")
	String getDomainId();
	void setDomainId(String domainId);

	@Name("Retry (in seconds)")
	Integer getRetry();
	void setRetry(Integer retry);

	@Name("Max Connection TTL (in ms)")
	Long getMaxConnectionTtlInMs();
	void setMaxConnectionTtlInMs(Long maxConnectionTtlInMs);

	@Name("Blocking Timeout (in ms)")
	Long getBlockTimeoutInMs();
	void setBlockTimeoutInMs(Long blockTimeoutInMs);

	Boolean getEnforceSingleConnectionPerSessionId();
	void setEnforceSingleConnectionPerSessionId(Boolean enforceSingleConnectionPerSessionId);
}
