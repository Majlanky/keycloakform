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

import com.groocraft.keycloakform.definition.ProtocolMapperDefinition;
import com.groocraft.keycloakform.former.generic.DefaultItemFormer;
import com.groocraft.keycloakform.updater.ProtocolMapperUpdater;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;

import lombok.CustomLog;

@CustomLog
public class ProtocolMapperFormer extends DefaultItemFormer<ProtocolMapperModel, ProtocolMapperDefinition> {

    private final ProtocolMapperUpdater updater = new ProtocolMapperUpdater();

    public ProtocolMapperFormer() {
        super(log);
    }

    @Override
    protected ProtocolMapperModel getKeycloakResource(ProtocolMapperDefinition definition, KeycloakSession session) {
        if (definition.getId() != null) {
            return session.getContext().getClient().getProtocolMapperById(definition.getId());
        }

        return session.getContext()
            .getClient()
            .getProtocolMappersStream()
            .filter(protocolMapperModel -> protocolMapperModel.getName().equals(definition.getName()))
            .findFirst()
            .orElse(null);
    }

    @Override
    protected ProtocolMapperModel create(ProtocolMapperDefinition definition, KeycloakSession session) {
        ProtocolMapperModel protocolMapperModel = new ProtocolMapperModel();
        protocolMapperModel.setName(definition.getName());
        protocolMapperModel.setId(definition.getId());
        session.getContext().getClient().addProtocolMapper(protocolMapperModel);
        return protocolMapperModel;
    }

    @Override
    protected void update(ProtocolMapperModel protocolMapperModel, ProtocolMapperDefinition definition) {
        updater.update(protocolMapperModel, definition);
    }

    @Override
    protected void updateCommit(ProtocolMapperModel protocolMapperModel, KeycloakSession session) {
        session.getContext().getClient().updateProtocolMapper(protocolMapperModel);
    }

    @Override
    protected Class<ProtocolMapperModel> getKeycloakResourceClass() {
        return ProtocolMapperModel.class;
    }

    @Override
    protected String getLogIdentifier(ProtocolMapperDefinition definition) {
        return "ProtocolMapper " + definition.getName();
    }

    @Override
    public Class<ProtocolMapperDefinition> getDefinitionClass() {
        return ProtocolMapperDefinition.class;
    }
}
