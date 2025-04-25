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

import com.groocraft.keycloakform.definition.ScopeDefinitionHelper;
import com.groocraft.keycloakform.former.FormerContext;
import com.groocraft.keycloakform.former.ItemFormer;
import com.groocraft.keycloakform.former.SyncMode;

import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientScopeModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleContainerModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.ScopeContainerModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.representations.idm.ScopeMappingRepresentation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.CustomLog;

@CustomLog
public class ScopeMappingsFormer implements ItemFormer<ScopeDefinitionHelper> {

    @Override
    public void form(ScopeDefinitionHelper definition, FormerContext context) {
        if(definition.getSyncMode() != SyncMode.IGNORE) {
            if (definition.getClientScopeMappings() != null) {
                for (Map.Entry<String, List<ScopeMappingRepresentation>> entry : definition.getClientScopeMappings().entrySet()) {
                    ClientModel client = context.getRealm().getClientByClientId(entry.getKey());
                    if (client == null) {
                        throw new RuntimeException("Unable to find client role mappings for client: " + entry.getKey());
                    }
                    processScopeMapping(entry.getValue(), client, context, definition.getSyncMode());
                }
            }

            if (definition.getScopeMappings() != null) {
                processScopeMapping(definition.getScopeMappings(), context.getRealm(), context, definition.getSyncMode());
            }
        }
    }

    private void processScopeMapping(List<ScopeMappingRepresentation> mappings, RoleContainerModel roleContainer, FormerContext context,
                                     SyncMode syncMode) {

        for (ScopeMappingRepresentation mapping : mappings) {
            ScopeContainerModel scopeContainer = getScopeContainerHavingScope(context.getRealm(), mapping);

            StringBuilder changeLog = new StringBuilder("Scope mapping for ")
                .append(mapping.getClient() == null ? "client scope " : "client ")
                .append(mapping.getClientScope() == null ? mapping.getClientScope() : mapping.getClient())
                .append(":\nAdded: ");

            List<String> added = new ArrayList<>();
            Map<String, RoleModel> existing = scopeContainer.getScopeMappingsStream()
                .collect(Collectors.toMap(RoleModel::getName, Function.identity()));
            List<RoleModel> shouldBeRemoved = scopeContainer.getScopeMappingsStream()
                .filter(r -> !mapping.getRoles().contains(r.getName()))
                .toList();

            for (String roleName : mapping.getRoles()) {
                RoleModel role = roleContainer.getRole(roleName.trim());
                if (role == null) {
                    role = roleContainer.addRole(roleName);
                }
                if (!existing.containsKey(roleName)) {
                    scopeContainer.addScopeMapping(role);
                    added.add(roleName);
                }
            }

            changeLog.append(String.join(", ", added));

            if(syncMode == SyncMode.FULL) {
                shouldBeRemoved.forEach(scopeContainer::deleteScopeMapping);

                changeLog
                    .append("\nRemoved: ")
                    .append(String.join(", ", shouldBeRemoved.stream().map(RoleModel::getName).toList()));
            }

            log.info(changeLog);
        }
    }

    private ScopeContainerModel getScopeContainerHavingScope(RealmModel realm, ScopeMappingRepresentation scope) {
        if (scope.getClient() != null) {
            ClientModel client = realm.getClientByClientId(scope.getClient());
            if (client == null) {
                throw new RuntimeException("Unknown client specification in scope mappings: " + scope.getClient());
            }
            return client;
        } else if (scope.getClientScope() != null) {
            ClientScopeModel clientScope = KeycloakModelUtils.getClientScopeByName(realm, scope.getClientScope());
            if (clientScope == null) {
                throw new RuntimeException("Unknown clientScope specification in scope mappings: " + scope.getClientScope());
            }
            return clientScope;
        } else {
            throw new RuntimeException("Either client or clientScope needs to be specified in scope mappings");
        }
    }

    @Override
    public Class<ScopeDefinitionHelper> getDefinitionClass() {
        return ScopeDefinitionHelper.class;
    }

}
