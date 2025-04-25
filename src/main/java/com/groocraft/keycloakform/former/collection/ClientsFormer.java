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

import com.groocraft.keycloakform.definition.ClientDefinition;
import com.groocraft.keycloakform.former.FormerContext;
import com.groocraft.keycloakform.former.FormersFactory;
import com.groocraft.keycloakform.former.generic.DefaultCollectionFormer;

import org.keycloak.models.ClientModel;
import org.keycloak.models.RealmModel;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import lombok.CustomLog;

@CustomLog
public class ClientsFormer extends DefaultCollectionFormer<ClientDefinition> {

    public ClientsFormer(FormersFactory formersFactory) {
        super(formersFactory);
    }

    @Override
    protected void deleteUndeclaredKeycloakResources(Collection<ClientDefinition> definitions, FormerContext context) {
        Set<String> definedClientIds = new HashSet<>(definitions.stream().map(ClientDefinition::getClientId).toList());

        RealmModel realm = context.getRealm();

        realm.getClientsStream()
            .filter(c -> !definedClientIds.contains(c.getClientId()))
            .forEach(m -> remove(m, realm));
    }

    private void remove(ClientModel client, RealmModel realm) {
        log.infof("Client %s of realm %s is present but not defined, deleting it", client.getClientId(), realm.getName());
        boolean removed = realm.removeClient(client.getId());
        if (!removed) {
            log.infof("Client %s of realm %s was not removed for unknown reason", client.getClientId(), realm.getName());
        }
    }

    @Override
    public Class<ClientDefinition> getDefinitionClass() {
        return ClientDefinition.class;
    }
}
