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
import com.groocraft.keycloakform.definition.RoleDefinition;
import com.groocraft.keycloakform.former.FormerContext;
import com.groocraft.keycloakform.former.FormersFactory;
import com.groocraft.keycloakform.former.generic.DefaultItemFormer;

import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientScopeModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.models.utils.RepresentationToModel;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.CustomLog;

@CustomLog
public class ClientFormer extends DefaultItemFormer<ClientModel, ClientDefinition> {

    private final FormersFactory formersFactory;

    public ClientFormer(FormersFactory formersFactory) {
        super(log);
        this.formersFactory = formersFactory;
    }

    @Override
    protected ClientModel getModel(ClientDefinition definition, FormerContext context) {
        return context.getRealm().getClientByClientId(definition.getClientId());
    }

    @Override
    protected ClientModel create(ClientDefinition definition, FormerContext context) {
        String id = definition.getId() == null ? KeycloakModelUtils.generateId() : definition.getId();
        return context.getRealm().addClient(id, definition.getClientId());
    }

    @Override
    protected void update(ClientModel model, ClientDefinition definition, FormerContext context) {
        context.setClient(model);

        processClientScopes(model, definition.getDefaultClientScopes(), context, true);
        processClientScopes(model, definition.getOptionalClientScopes(), context, false);

        formersFactory.getForCollectionOf(RoleDefinition.class)
            .form(DefinitionMapping.cast(context.getRealmDefinition()
                .getRoles().getClient().get(definition.getClientId())), context, definition.getSyncMode());
        formersFactory.getForCollectionOf(ProtocolMapperDefinition.class)
            .form(DefinitionMapping.cast(definition.getProtocolMappers()), context, definition.getSyncMode());

        RepresentationToModel.updateClient(definition, model, context.getSession());

        context.setClient(null);
    }

    private void processClientScopes(ClientModel model, List<String> toAssign, FormerContext context,  boolean defaultScope){
        if(toAssign != null ) {
            Map<String, ClientScopeModel> namedClientScopes = context.getRealm().getClientScopesStream()
                .collect(Collectors.toMap(ClientScopeModel::getName, cs -> cs));

            model.getClientScopes(defaultScope).forEach((name, cs) -> {
                if (!toAssign.contains(name)) {
                    model.removeClientScope(cs);
                } else {
                    toAssign.remove(name);
                }
            });

            toAssign.forEach(name -> model.addClientScope(namedClientScopes.get(name), defaultScope));
        }
    }

    @Override
    protected Class<ClientModel> getKeycloakResourceClass() {
        return ClientModel.class;
    }

    @Override
    protected String getLogIdentifier(ClientDefinition definition) {
        return "Client " + definition.getClientId() + "(" + definition.getId() + ")";
    }

    @Override
    public Class<ClientDefinition> getDefinitionClass() {
        return ClientDefinition.class;
    }
}
