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

import com.groocraft.keycloakform.definition.RoleDefinition;
import com.groocraft.keycloakform.former.FormerContext;
import com.groocraft.keycloakform.former.generic.DefaultItemFormer;
import com.groocraft.keycloakform.updater.RoleUpdater;

import org.keycloak.models.RoleContainerModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.utils.KeycloakModelUtils;

import lombok.CustomLog;

@CustomLog
public class RoleFormer extends DefaultItemFormer<RoleModel, RoleDefinition> {

    private final RoleUpdater updater = new RoleUpdater();

    public RoleFormer() {
        super(log);
    }

    @Override
    protected RoleModel getModel(RoleDefinition definition, FormerContext context) {
        return getCurrentlyManagedContainer(context).getRole(definition.getName());
    }

    @Override
    protected RoleModel create(RoleDefinition definition, FormerContext context) {
        String id = definition.getId() == null ? KeycloakModelUtils.generateId() : definition.getId();
        return getCurrentlyManagedContainer(context).addRole(id, definition.getName());
    }

    @Override
    protected void update(RoleModel role, RoleDefinition definition, FormerContext context) {
        updater.update(role, definition, context);
    }

    @Override
    protected Class<RoleModel> getKeycloakResourceClass() {
        return RoleModel.class;
    }

    @Override
    protected String getLogIdentifier(RoleDefinition definition) {
        return "Role " + definition.getName();
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
