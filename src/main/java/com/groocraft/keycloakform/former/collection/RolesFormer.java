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

import com.groocraft.keycloakform.definition.RoleDefinition;
import com.groocraft.keycloakform.former.FormerContext;
import com.groocraft.keycloakform.former.FormersFactory;
import com.groocraft.keycloakform.former.generic.DefaultCollectionFormer;

import org.keycloak.models.RoleContainerModel;
import org.keycloak.models.RoleModel;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import lombok.CustomLog;

@CustomLog
public class RolesFormer extends DefaultCollectionFormer<RoleDefinition> {

    public RolesFormer(FormersFactory formersFactory) {
        super(formersFactory);
    }

    @Override
    protected void deleteUndeclaredKeycloakResources(Collection<RoleDefinition> definitions, FormerContext context) {
        Set<String> definedRoles = new HashSet<>(definitions.stream().map(RoleDefinition::getName).toList());

        RoleContainerModel container = getCurrentlyManagedContainer(context);

        container.getRolesStream()
            .filter(m -> !definedRoles.contains(m.getName()))
            .forEach(m -> remove(m, container));
    }

    private void remove(RoleModel role, RoleContainerModel container) {
        log.infof("Role %s is present but not defined, deleting it", role.getName());
        container.removeRole(role);
    }

    @Override
    public Class<RoleDefinition> getDefinitionClass() {
        return RoleDefinition.class;
    }

    private RoleContainerModel getCurrentlyManagedContainer(FormerContext context) {
        if (context.getClient() != null) {
            return context.getClient();
        }
        return context.getRealm();
    }
}
