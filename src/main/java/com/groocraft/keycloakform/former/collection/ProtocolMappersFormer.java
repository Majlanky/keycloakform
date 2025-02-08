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

import com.groocraft.keycloakform.definition.ProtocolMapperDefinition;
import com.groocraft.keycloakform.former.FormersFactory;
import com.groocraft.keycloakform.former.generic.DefaultCollectionFormer;

import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.RealmModel;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import lombok.CustomLog;

@CustomLog
public class ProtocolMappersFormer extends DefaultCollectionFormer<ProtocolMapperDefinition> {

    public ProtocolMappersFormer(FormersFactory formersFactory) {
        super(formersFactory);
    }

    @Override
    protected void deleteUndeclaredKeycloakResources(Collection<ProtocolMapperDefinition> definitions, KeycloakSession session,
                                                     boolean dryRun) {
        Set<String> definedMappers = new HashSet<>(definitions.stream().map(ProtocolMapperDefinition::getName).toList());

        ClientModel client = session.getContext().getClient();
        RealmModel realm = session.getContext().getRealm();

        client.getProtocolMappersStream()
            .filter(m -> !definedMappers.contains(m.getName()))
            .forEach(m -> remove(m, client, realm, dryRun));
    }

    private void remove(ProtocolMapperModel mapper, ClientModel client, RealmModel realm, boolean dryRun) {
        if (dryRun) {
            log.infof("Protocol mapper %s of client %s of realm %s is present but not in definition, would be deleted",
                mapper.getName(), client.getName(), realm.getName());
        } else {
            log.infof("Protocol mapper %s of client %s of realm %s is present but not in definition, deleting it",
                mapper.getName(), client.getName(), realm.getName());
            client.removeProtocolMapper(mapper);
        }
    }

    @Override
    public Class<ProtocolMapperDefinition> getDefinitionClass() {
        return ProtocolMapperDefinition.class;
    }
}
