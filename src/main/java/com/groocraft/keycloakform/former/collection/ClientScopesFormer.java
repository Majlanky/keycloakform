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

import com.groocraft.keycloakform.definition.ClientScopeDefinition;
import com.groocraft.keycloakform.former.FormerContext;
import com.groocraft.keycloakform.former.FormersFactory;
import com.groocraft.keycloakform.former.generic.DefaultCollectionFormer;

import org.keycloak.models.ClientScopeModel;
import org.keycloak.models.RealmModel;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import lombok.CustomLog;

@CustomLog
public class ClientScopesFormer extends DefaultCollectionFormer<ClientScopeDefinition> {

    public ClientScopesFormer(FormersFactory formersFactory) {
        super(formersFactory);
    }

    @Override
    protected void deleteUndeclaredKeycloakResources(Collection<ClientScopeDefinition> definitions, FormerContext context) {
        Set<String> defined = new HashSet<>(definitions.stream().map(ClientScopeDefinition::getName).toList());

        RealmModel realm = context.getRealm();

        realm.getClientScopesStream()
            .filter(c -> !defined.contains(c.getName()))
            .forEach(m -> remove(m, realm));
    }

    private void remove(ClientScopeModel scopeModel, RealmModel realm) {
        log.infof("Client scope %s (%s) of realm %s is present but not defined, deleting it",
            scopeModel.getName(), scopeModel.getId(), realm.getName());
        boolean removed = realm.removeClientScope(scopeModel.getId());
        if (!removed) {
            log.infof("Client scope %s (%s) of realm %s was not removed for unknown reason",
                scopeModel.getName(), scopeModel.getId(), realm.getName());
        }
    }

    @Override
    public Class<ClientScopeDefinition> getDefinitionClass() {
        return ClientScopeDefinition.class;
    }

}
