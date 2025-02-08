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

import com.groocraft.keycloakform.definition.ClientDefinition;
import com.groocraft.keycloakform.definition.DefinitionMapping;
import com.groocraft.keycloakform.definition.ProtocolMapperDefinition;
import com.groocraft.keycloakform.former.FormersFactory;
import com.groocraft.keycloakform.former.generic.DefaultItemFormer;
import com.groocraft.keycloakform.updater.ClientUpdater;

import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;

import lombok.CustomLog;

@CustomLog
public class ClientFormer extends DefaultItemFormer<ClientModel, ClientDefinition> {

    private final ClientUpdater updater = new ClientUpdater();
    private final FormersFactory formersFactory;

    public ClientFormer(FormersFactory formersFactory) {
        super(log);
        this.formersFactory = formersFactory;
    }

    @Override
    public void form(ClientDefinition definition, KeycloakSession session, boolean dryRun) {
        super.form(definition, session, dryRun);

        formersFactory.getForCollectionOf(ProtocolMapperDefinition.class)
            .form(DefinitionMapping.cast(definition.getProtocolMappers()), session, dryRun);

        session.getContext().setClient(null);
    }

    @Override
    protected void update(ClientModel clientModel, ClientDefinition definition, KeycloakSession session, boolean dryRun,
                          String logIdentifier) {
        super.update(clientModel, definition, session, dryRun, logIdentifier);
        session.getContext().setClient(clientModel);
    }

    @Override
    protected ClientModel getKeycloakResource(ClientDefinition definition, KeycloakSession session) {
        if (definition.getId() != null) {
            return session.getContext().getRealm().getClientById(definition.getId());
        }
        return session.getContext().getRealm().getClientsStream().filter(clientModel -> clientModel.getName().equals(definition.getName()))
            .findFirst().orElse(null);
    }

    @Override
    protected ClientModel create(ClientDefinition definition, KeycloakSession session) {
        if (definition.getId() != null) {
            return session.getContext().getRealm().addClient(definition.getId(), definition.getName());
        }

        return session.getContext().getRealm().addClient(definition.getName());
    }

    @Override
    protected void update(ClientModel clientModel, ClientDefinition definition) {
        updater.update(clientModel, definition);
    }

    @Override
    protected void updateCommit(ClientModel clientModel, KeycloakSession session) {
        //Nothing to do here as client model is auto-commit
    }

    @Override
    protected Class<ClientModel> getKeycloakResourceClass() {
        return ClientModel.class;
    }

    @Override
    protected String getLogIdentifier(ClientDefinition definition) {
        return "Client " + definition.getName();
    }

    @Override
    public Class<ClientDefinition> getDefinitionClass() {
        return ClientDefinition.class;
    }
}
