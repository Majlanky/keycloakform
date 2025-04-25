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

package com.groocraft.keycloakform.former.collection;

import com.groocraft.keycloakform.definition.AuthenticationExecutionDefinition;
import com.groocraft.keycloakform.definition.AuthenticationFlowDefinition;
import com.groocraft.keycloakform.definition.DefinitionMapping;
import com.groocraft.keycloakform.former.FormerContext;
import com.groocraft.keycloakform.former.FormersFactory;
import com.groocraft.keycloakform.former.SyncMode;
import com.groocraft.keycloakform.former.generic.DefaultCollectionFormer;

import org.keycloak.models.AuthenticationFlowModel;
import org.keycloak.models.RealmModel;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import lombok.CustomLog;

@CustomLog
public class AuthenticationFlowsFormer extends DefaultCollectionFormer<AuthenticationFlowDefinition> {

    public AuthenticationFlowsFormer(FormersFactory formersFactory) {
        super(formersFactory);
    }

    @Override
    public void form(Collection<AuthenticationFlowDefinition> definitions, FormerContext context, SyncMode syncMode) {
        super.form(definitions, context, syncMode);

        definitions.forEach(d -> formAuthenticationExecutions(d, context, syncMode));
    }

    private void formAuthenticationExecutions(AuthenticationFlowDefinition definition, FormerContext context, SyncMode syncMode) {
        AuthenticationFlowModel flow = context.getRealm().getAuthenticationFlowById(definition.getId());
        context.setAuthenticationFlow(flow);
        getFormersFactory().getForCollectionOf(AuthenticationExecutionDefinition.class)
                .form(DefinitionMapping.cast(definition.getAuthenticationExecutions()), context, syncMode);
        context.setAuthenticationFlow(null);
    }

    @Override
    protected void deleteUndeclaredKeycloakResources(Collection<AuthenticationFlowDefinition> definitions, FormerContext context) {
        Set<String> defined = new HashSet<>(definitions.stream().map(AuthenticationFlowDefinition::getId).toList());

        RealmModel realm = context.getRealm();

        realm.getAuthenticationFlowsStream()
            .filter(f -> !defined.contains(f.getId()))
            .forEach(f -> remove(f, realm));
    }

    private void remove(AuthenticationFlowModel authenticationFlowModel, RealmModel realm) {
        log.infof("Authentication flow %s (%s) of realm %s is present but not defined, deleting it",
            authenticationFlowModel.getDescription(), authenticationFlowModel.getId(), realm.getName());
        realm.removeAuthenticationFlow(authenticationFlowModel);
    }

    @Override
    public Class<AuthenticationFlowDefinition> getDefinitionClass() {
        return AuthenticationFlowDefinition.class;
    }
}
