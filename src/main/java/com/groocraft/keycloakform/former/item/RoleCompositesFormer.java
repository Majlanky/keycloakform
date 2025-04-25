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

import com.groocraft.keycloakform.definition.DefinitionMapping;
import com.groocraft.keycloakform.definition.RoleDefinition;
import com.groocraft.keycloakform.definition.RolesDefinition;
import com.groocraft.keycloakform.former.FormerContext;
import com.groocraft.keycloakform.former.ItemFormer;
import com.groocraft.keycloakform.former.SyncMode;

import org.keycloak.models.ClientModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleContainerModel;
import org.keycloak.models.RoleModel;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.CustomLog;

@CustomLog
public class RoleCompositesFormer implements ItemFormer<RolesDefinition> {

    @Override
    public void form(RolesDefinition definition, FormerContext context) {
        RealmModel realm = context.getRealm();
        form(DefinitionMapping.cast(definition.getRealm()), realm, realm, context.getRealmDefinition().getSyncMode());

        definition.getClient().forEach((key, value) -> form(DefinitionMapping.cast(value), realm.getClientByClientId(key), realm,
            context.getRealmDefinition().getSyncMode()));
    }

    private void form(Collection<RoleDefinition> roles, RoleContainerModel container, RealmModel realm, SyncMode syncMode) {
        for (RoleDefinition roleDefinition : roles) {
            assignComposites(container == null ? null : container.getRole(roleDefinition.getName()), realm, roleDefinition, syncMode);
        }
    }

    private void assignComposites(RoleModel role, RealmModel realm, RoleDefinition definition, SyncMode syncMode) {
        StringBuilder changeLog = new StringBuilder("Composite roles for role ")
            .append(definition.getName())
            .append(":\nAdded: ");

        Set<String> added = new HashSet<>();
        Set<String> formed = new HashSet<>();

        if (definition.getComposites() != null && definition.getComposites().getRealm() != null) {
            for (String composite : definition.getComposites().getRealm()) {
                addComposite(realm.getRole(composite), role, added, formed);
            }
        }

        if (definition.getComposites() != null && definition.getComposites().getClient() != null) {
            for (Map.Entry<String, List<String>> e : definition.getComposites().getClient().entrySet()) {

                ClientModel client = realm.getClientByClientId(e.getKey());
                for (String composite : e.getValue()) {
                    addComposite(client.getRole(composite), role, added, formed);
                }
            }
        }

        changeLog.append(String.join(", ", added));

        if(syncMode == SyncMode.FULL) {

            Set<String> toBeRemoved = new HashSet<>();
            if (role != null) {
                toBeRemoved.addAll(role.getCompositesStream().map(RoleModel::getName).toList());
            }
            toBeRemoved.removeAll(formed);

            if (role != null) {
                role.getCompositesStream()
                    .filter(r -> toBeRemoved.contains(r.getName()))
                    .forEach(role::removeCompositeRole);
            }

            changeLog
                .append("\nRemoved: ")
                .append(String.join(", ", toBeRemoved));
        }

        log.info(changeLog);

    }

    private void addComposite(RoleModel composite, RoleModel role, Set<String> added, Set<String> formed) {
        if (role != null) {
            if (!role.hasRole(composite)) {
                role.addCompositeRole(composite);
                added.add(composite.getName());
            }
        } else {
            added.add(composite.getName());
        }
        formed.add(composite.getName());
    }

    @Override
    public Class<RolesDefinition> getDefinitionClass() {
        return RolesDefinition.class;
    }
}
