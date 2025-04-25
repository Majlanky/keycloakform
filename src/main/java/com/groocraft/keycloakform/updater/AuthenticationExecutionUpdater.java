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

import com.groocraft.keycloakform.definition.AuthenticationExecutionDefinition;
import com.groocraft.keycloakform.former.FormerContext;

import org.apache.http.util.Asserts;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.AuthenticationFlowModel;
import org.keycloak.models.AuthenticatorConfigModel;

public class AuthenticationExecutionUpdater implements Updater<AuthenticationExecutionModel, AuthenticationExecutionDefinition> {

    @Override
    public void update(AuthenticationExecutionModel model, AuthenticationExecutionDefinition definition, FormerContext context) {
        if (definition == null) {
            return;
        }

        if (definition.getFlowAlias() != null) {
            AuthenticationFlowModel aliased = context.getRealm().getAuthenticationFlowsStream()
                .filter(af -> definition.getFlowAlias().equals(af.getAlias())).findFirst().orElse(null);
            Asserts.check(aliased != null,
                "Authenticator execution refers to flow with alias %s that does not exist", definition.getFlowAlias());
            model.setFlowId(aliased.getId());
        }

        if (definition.getAuthenticatorConfig() != null) {
            AuthenticatorConfigModel configModel = context.getRealm().getAuthenticatorConfigByAlias(definition.getAuthenticatorConfig());
            Asserts.check(configModel != null,
                "Authenticator config of alias %s does not exist", definition.getAuthenticatorConfig());
            model.setAuthenticatorConfig(configModel.getId());
        }
        if (definition.getAuthenticator() != null) {
            model.setAuthenticator(definition.getAuthenticator());
        }
        if (definition.getRequirement() != null) {
            model.setRequirement(AuthenticationExecutionModel.Requirement.valueOf(definition.getRequirement()));
        }
        if (definition.getPriority() != null) {
            model.setPriority(definition.getPriority());
        }
        model.setAuthenticatorFlow(definition.isAuthenticatorFlow());
    }

}
