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

import com.groocraft.keycloakform.definition.AuthenticatorConfigDefinition;
import com.groocraft.keycloakform.former.FormerContext;
import com.groocraft.keycloakform.former.generic.DefaultItemFormer;
import com.groocraft.keycloakform.updater.AuthenticatorConfigUpdater;

import org.keycloak.models.AuthenticatorConfigModel;

import lombok.CustomLog;

@CustomLog
public class AuthenticatorConfigFormer extends DefaultItemFormer<AuthenticatorConfigModel, AuthenticatorConfigDefinition> {

    private final AuthenticatorConfigUpdater updater = new AuthenticatorConfigUpdater();

    public AuthenticatorConfigFormer() {
        super(log);
    }

    @Override
    protected AuthenticatorConfigModel getModel(AuthenticatorConfigDefinition definition, FormerContext context) {
        return context.getRealm().getAuthenticatorConfigById(definition.getId());
    }

    @Override
    protected AuthenticatorConfigModel create(AuthenticatorConfigDefinition definition, FormerContext context) {
        AuthenticatorConfigModel model = new AuthenticatorConfigModel();
        model.setId(definition.getId());
        return model;
    }

    @Override
    protected void update(AuthenticatorConfigModel model, AuthenticatorConfigDefinition definition, FormerContext context) {
        updater.update(model, definition, context);

        AuthenticatorConfigModel existing = getModel(definition, context);
        if (existing == null) {
            context.getRealm().addAuthenticatorConfig(model);
        } else {
            context.getRealm().updateAuthenticatorConfig(model);
        }
    }

    @Override
    protected Class<AuthenticatorConfigModel> getKeycloakResourceClass() {
        return AuthenticatorConfigModel.class;
    }

    @Override
    protected String getLogIdentifier(AuthenticatorConfigDefinition definition) {
        return "Authenticator config " + definition.getId();
    }

    @Override
    public Class<AuthenticatorConfigDefinition> getDefinitionClass() {
        return AuthenticatorConfigDefinition.class;
    }
}
