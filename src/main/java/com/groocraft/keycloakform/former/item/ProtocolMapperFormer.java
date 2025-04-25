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
import com.groocraft.keycloakform.former.FormerContext;
import com.groocraft.keycloakform.former.generic.DefaultItemFormer;
import com.groocraft.keycloakform.updater.ProtocolMapperUpdater;

import org.keycloak.models.ProtocolMapperContainerModel;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.utils.KeycloakModelUtils;

import lombok.CustomLog;

@CustomLog
public class ProtocolMapperFormer extends DefaultItemFormer<ProtocolMapperModel, ProtocolMapperDefinition> {

    private final ProtocolMapperUpdater updater = new ProtocolMapperUpdater();

    public ProtocolMapperFormer() {
        super(log);
    }

    @Override
    protected ProtocolMapperModel getModel(ProtocolMapperDefinition definition, FormerContext context) {
        return getCurrentlyManagedContainer(context).getProtocolMappersStream()
            .filter(pm -> pm.getName().equals(definition.getName()))
            .findFirst().orElse(null);
    }

    @Override
    protected ProtocolMapperModel create(ProtocolMapperDefinition definition, FormerContext context) {
        String id = definition.getId() == null ? KeycloakModelUtils.generateId() : definition.getId();
        ProtocolMapperModel protocolMapperModel = new ProtocolMapperModel();
        protocolMapperModel.setName(definition.getName());
        protocolMapperModel.setId(id);
        return protocolMapperModel;
    }

    @Override
    protected void update(ProtocolMapperModel protocolMapperModel, ProtocolMapperDefinition definition, FormerContext context) {
        updater.update(protocolMapperModel, definition, context);

        ProtocolMapperModel existing = getModel(definition, context);
        if (existing == null) {
            getCurrentlyManagedContainer(context).addProtocolMapper(protocolMapperModel);
        } else {
            getCurrentlyManagedContainer(context).updateProtocolMapper(protocolMapperModel);
        }
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

    private ProtocolMapperContainerModel getCurrentlyManagedContainer(FormerContext context) {
        if (context.getClient() != null) {
            return context.getClient();
        }
        return context.getClientScope();
    }

}
