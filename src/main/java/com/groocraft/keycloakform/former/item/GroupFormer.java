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

import com.groocraft.keycloakform.definition.GroupDefinition;
import com.groocraft.keycloakform.former.FormerContext;
import com.groocraft.keycloakform.former.generic.DefaultItemFormer;
import com.groocraft.keycloakform.updater.GroupUpdater;

import org.keycloak.models.GroupModel;
import org.keycloak.models.utils.KeycloakModelUtils;

import lombok.CustomLog;

@CustomLog
public class GroupFormer extends DefaultItemFormer<GroupModel, GroupDefinition> {

    private final GroupUpdater updater = new GroupUpdater();

    public GroupFormer() {
        super(log);
    }

    @Override
    protected GroupModel getModel(GroupDefinition definition, FormerContext context) {
        return context.getRealm().getGroupById(definition.getId());
    }

    @Override
    protected GroupModel create(GroupDefinition definition, FormerContext context) {
        String id = definition.getId() == null ? KeycloakModelUtils.generateId() : definition.getId();
        return context.getRealm().createGroup(id, "");
    }

    @Override
    protected void update(GroupModel model, GroupDefinition definition, FormerContext context) {
        updater.update(model, definition, context);
    }

    @Override
    protected Class<GroupModel> getKeycloakResourceClass() {
        return GroupModel.class;
    }

    @Override
    protected String getLogIdentifier(GroupDefinition definition) {
        return "Group " + definition.getId();
    }

    @Override
    public Class<GroupDefinition> getDefinitionClass() {
        return GroupDefinition.class;
    }
}
