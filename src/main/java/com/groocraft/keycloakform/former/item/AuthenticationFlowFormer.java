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

package com.groocraft.keycloakform.former.item;

import com.groocraft.keycloakform.definition.AuthenticationFlowDefinition;
import com.groocraft.keycloakform.former.FormerContext;
import com.groocraft.keycloakform.former.generic.DefaultItemFormer;
import com.groocraft.keycloakform.updater.AuthenticationFlowUpdater;

import org.keycloak.models.AuthenticationFlowModel;
import org.keycloak.models.utils.KeycloakModelUtils;

import lombok.CustomLog;

@CustomLog
public class AuthenticationFlowFormer extends DefaultItemFormer<AuthenticationFlowModel, AuthenticationFlowDefinition> {

    private final AuthenticationFlowUpdater updater = new AuthenticationFlowUpdater();

    public AuthenticationFlowFormer() {
        super(log);
    }

    @Override
    protected AuthenticationFlowModel getModel(AuthenticationFlowDefinition definition, FormerContext context) {
        return context.getRealm().getAuthenticationFlowById(definition.getId());
    }

    @Override
    protected AuthenticationFlowModel create(AuthenticationFlowDefinition definition, FormerContext context) {
        String id = definition.getId() == null ? KeycloakModelUtils.generateId() : definition.getId();
        AuthenticationFlowModel model = new AuthenticationFlowModel();
        model.setId(id);
        return model;
    }

    @Override
    protected void update(AuthenticationFlowModel model, AuthenticationFlowDefinition definition, FormerContext context) {
        updater.update(model, definition, context);

        AuthenticationFlowModel existing = getModel(definition, context);
        if (existing == null) {
            context.getRealm().addAuthenticationFlow(model);
        } else {
            context.getRealm().updateAuthenticationFlow(model);
        }
    }

    @Override
    protected Class<AuthenticationFlowModel> getKeycloakResourceClass() {
        return AuthenticationFlowModel.class;
    }

    @Override
    protected String getLogIdentifier(AuthenticationFlowDefinition definition) {
        return "Authentication flow " + definition.getId();
    }

    @Override
    public Class<AuthenticationFlowDefinition> getDefinitionClass() {
        return AuthenticationFlowDefinition.class;
    }

}
