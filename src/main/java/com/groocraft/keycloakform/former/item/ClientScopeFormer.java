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

import com.groocraft.keycloakform.definition.ClientScopeDefinition;
import com.groocraft.keycloakform.definition.DefinitionMapping;
import com.groocraft.keycloakform.definition.ProtocolMapperDefinition;
import com.groocraft.keycloakform.former.FormerContext;
import com.groocraft.keycloakform.former.FormersFactory;
import com.groocraft.keycloakform.former.generic.DefaultItemFormer;

import org.keycloak.models.ClientScopeModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.models.utils.RepresentationToModel;

import lombok.CustomLog;

@CustomLog
public class ClientScopeFormer extends DefaultItemFormer<ClientScopeModel, ClientScopeDefinition> {

    private final FormersFactory formersFactory;

    public ClientScopeFormer(FormersFactory formersFactory) {
        super(log);
        this.formersFactory = formersFactory;
    }

    @Override
    protected ClientScopeModel getModel(ClientScopeDefinition definition, FormerContext context) {
        return context.getRealm().getClientScopesStream()
            .filter(cs -> cs.getName().equals(definition.getName()))
            .findFirst().orElse(null);
    }

    @Override
    protected ClientScopeModel create(ClientScopeDefinition definition, FormerContext context) {
        String id = definition.getId() == null ? KeycloakModelUtils.generateId() : definition.getId();
        return context.getRealm().addClientScope(id, definition.getName());
    }

    @Override
    protected void update(ClientScopeModel model, ClientScopeDefinition definition, FormerContext context) {
        RepresentationToModel.updateClientScope(definition, model);

        context.setClientScope(model);

        formersFactory.getForCollectionOf(ProtocolMapperDefinition.class)
            .form(DefinitionMapping.cast(definition.getProtocolMappers()), context, definition.getSyncMode());

        context.setClientScope(null);
    }

    @Override
    protected Class<ClientScopeModel> getKeycloakResourceClass() {
        return ClientScopeModel.class;
    }

    @Override
    protected String getLogIdentifier(ClientScopeDefinition definition) {
        return "Client scope " + definition.getName() + "(" + definition.getId() + ")";
    }

    @Override
    public Class<ClientScopeDefinition> getDefinitionClass() {
        return ClientScopeDefinition.class;
    }

}
