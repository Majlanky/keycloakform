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

import com.groocraft.keycloakform.definition.AuthenticationExecutionDefinition;
import com.groocraft.keycloakform.former.FormerContext;
import com.groocraft.keycloakform.former.generic.DefaultItemFormer;
import com.groocraft.keycloakform.updater.AuthenticationExecutionUpdater;

import org.apache.http.util.Asserts;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.utils.KeycloakModelUtils;

import java.util.Objects;

import lombok.CustomLog;

@CustomLog
public class AuthenticationExecutionFormer extends DefaultItemFormer<AuthenticationExecutionModel, AuthenticationExecutionDefinition> {

    private final AuthenticationExecutionUpdater updater = new AuthenticationExecutionUpdater();

    public AuthenticationExecutionFormer() {
        super(log);
    }

    @Override
    protected AuthenticationExecutionModel getModel(AuthenticationExecutionDefinition definition, FormerContext context) {
        return context.getRealm().getAuthenticationExecutionsStream(context.getAuthenticationFlow().getId())
            .filter(ae -> isEqual(ae, definition, context))
            .findFirst().orElse(null);
    }

    private boolean isEqual(AuthenticationExecutionModel model, AuthenticationExecutionDefinition definition, FormerContext context) {
        return Objects.equals(model.getAuthenticator(), definition.getAuthenticator())
                       && model.getPriority() == definition.getPriority()
                       && model.getRequirement() == AuthenticationExecutionModel.Requirement.valueOf(definition.getRequirement())
                       && model.isAuthenticatorFlow() == definition.isAuthenticatorFlow()
                       && isAuthenticatorEqual(model.getAuthenticatorConfig(), definition.getAuthenticatorConfig(), context);
    }

    private boolean isAuthenticatorEqual(String id, String alias, FormerContext context) {
        if(alias != null) {
            AuthenticatorConfigModel config = context.getRealm().getAuthenticatorConfigByAlias(alias);
            Asserts.check(config != null,
                "Authenticator config of alias %s does not exist", alias);
            return Objects.equals(id, config.getId());
        }
        return false;
    }

    @Override
    protected AuthenticationExecutionModel create(AuthenticationExecutionDefinition definition, FormerContext context) {
        AuthenticationExecutionModel model = new AuthenticationExecutionModel();
        model.setId(KeycloakModelUtils.generateId());
        model.setParentFlow(context.getAuthenticationFlow().getId()); //UGH?!
        return model;
    }

    @Override
    protected void update(AuthenticationExecutionModel model, AuthenticationExecutionDefinition definition, FormerContext context) {
        updater.update(model, definition, context);

        AuthenticationExecutionModel existing = getModel(definition, context);
        if (existing == null) {
            context.getRealm().addAuthenticatorExecution(model);
        }
    }

    @Override
    protected Class<AuthenticationExecutionModel> getKeycloakResourceClass() {
        return AuthenticationExecutionModel.class;
    }

    @Override
    protected String getLogIdentifier(AuthenticationExecutionDefinition definition) {
        return "Authentication execution " + definition.getAuthenticator() + " " + definition.getPriority();
    }

    @Override
    public Class<AuthenticationExecutionDefinition> getDefinitionClass() {
        return AuthenticationExecutionDefinition.class;
    }
}
