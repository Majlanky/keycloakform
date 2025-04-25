/*
 * Copyright 2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.groocraft.keycloakform.updater;

import com.groocraft.keycloakform.definition.IdentityProviderDefinition;
import com.groocraft.keycloakform.former.FormerContext;

import org.keycloak.models.AuthenticationFlowModel;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.ModelException;

import java.util.LinkedHashMap;
import java.util.Map;

public class IdentityProviderUpdater implements Updater<IdentityProviderModel, IdentityProviderDefinition> {

    @Override
    public void update(IdentityProviderModel model, IdentityProviderDefinition definition, FormerContext context) {
        if (definition == null) {
            return;
        }

        if (definition.getInternalId() != null) {
            model.setInternalId(definition.getInternalId());
        }
        if (definition.getProviderId() != null) {
            model.setProviderId(definition.getProviderId());
        }
        if (definition.getDisplayName() != null) {
            model.setDisplayName(definition.getDisplayName());
        }

        model.setEnabled(definition.isEnabled());
        model.setStoreToken(definition.isStoreToken());
        model.setLinkOnly(definition.isLinkOnly());
        model.setAuthenticateByDefault(definition.isAuthenticateByDefault());
        model.setAddReadTokenRoleOnCreate(definition.isAddReadTokenRoleOnCreate());
        model.setTrustEmail(definition.isTrustEmail());

        if (definition.getConfig() != null) {
            Map<String, String> config = model.getConfig();
            if (config != null) {
                config.clear();
                config.putAll(definition.getConfig());
            } else {
                model.setConfig(new LinkedHashMap<>(definition.getConfig()));
            }
        }

        if (definition.getFirstBrokerLoginFlowAlias() != null) {
            String flowAlias = definition.getFirstBrokerLoginFlowAlias();
            if (flowAlias.trim().isEmpty()) {
                model.setFirstBrokerLoginFlowId(null);
            } else {
                AuthenticationFlowModel flowModel = context.getRealm().getFlowByAlias(flowAlias);
                if (flowModel == null) {
                    throw new ModelException("No available authentication flow with alias: " + flowAlias);
                }
                model.setFirstBrokerLoginFlowId(flowModel.getId());
            }
        }

        if (definition.getPostBrokerLoginFlowAlias() != null) {
            String flowAlias = definition.getPostBrokerLoginFlowAlias();
            if (flowAlias.trim().isEmpty()) {
                model.setPostBrokerLoginFlowId(null);
            } else {
                AuthenticationFlowModel flowModel = context.getRealm().getFlowByAlias(flowAlias);
                if (flowModel == null) {
                    throw new ModelException("No available authentication flow with alias: " + flowAlias);
                }
                model.setPostBrokerLoginFlowId(flowModel.getId());
            }
        }


    }

}
