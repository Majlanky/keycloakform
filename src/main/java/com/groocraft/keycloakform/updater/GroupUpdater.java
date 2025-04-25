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

package com.groocraft.keycloakform.updater;

import com.groocraft.keycloakform.definition.GroupDefinition;
import com.groocraft.keycloakform.former.FormerContext;

import org.keycloak.models.ClientModel;
import org.keycloak.models.GroupModel;
import org.keycloak.models.RoleModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupUpdater implements Updater<GroupModel, GroupDefinition> {

    @Override
    public void update(GroupModel model, GroupDefinition definition, FormerContext context) {
        if (definition == null) {
            return;
        }

        if (definition.getAttributes() != null) {
            if (model.getAttributes() != null) {
                model.getAttributes().keySet().forEach(model::removeAttribute);
            }
            definition.getAttributes().forEach(model::setAttribute);
        }

        if (definition.getName() != null) {
            model.setName(definition.getName());
        }

        if (definition.getRealmRoles() != null) {
            List<String> toAssign = new ArrayList<>(definition.getRealmRoles());
            model.getRealmRoleMappingsStream().forEach(role -> {
                if (!toAssign.contains(role.getName())) {
                    model.deleteRoleMapping(role);
                } else {
                    toAssign.remove(role.getName());
                }
            });
            toAssign.forEach(r -> model.grantRole(context.getRealm().getRole(r)));
        }

        if (definition.getClientRoles() != null) {
            Map<String, List<String>> toAssign = new HashMap<>();
            definition.getClientRoles().forEach((clientId, roles) -> toAssign.put(clientId, new ArrayList<>(roles)));
            model.getRoleMappingsStream().filter(RoleModel::isClientRole).forEach(role -> {
                ClientModel client = (ClientModel) role.getContainer();
                if (!toAssign.containsKey(client.getClientId())) {
                    model.deleteRoleMapping(role);
                } else {
                    toAssign.get(client.getClientId()).remove(role.getName());
                }
            });
            toAssign.forEach((clientId, roles) -> {
                ClientModel client = context.getRealm().getClientByClientId(clientId);
                roles.forEach(r -> model.grantRole(client.getRole(r)));
            });
        }

    }

}
