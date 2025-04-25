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

import com.groocraft.keycloakform.definition.GroupDefinition;
import com.groocraft.keycloakform.former.FormerContext;
import com.groocraft.keycloakform.former.FormersFactory;
import com.groocraft.keycloakform.former.SyncMode;
import com.groocraft.keycloakform.former.generic.DefaultCollectionFormer;

import org.keycloak.models.GroupModel;
import org.keycloak.models.RealmModel;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.CustomLog;

@CustomLog
public class GroupsFormer extends DefaultCollectionFormer<GroupDefinition> {

    public GroupsFormer(FormersFactory formersFactory) {
        super(formersFactory);
    }

    @Override
    public void form(Collection<GroupDefinition> definitions, FormerContext context, SyncMode syncMode) {
        super.form(definitions, context, syncMode);

        //FIXME link subgroups
    }

    @Override
    protected void deleteUndeclaredKeycloakResources(Collection<GroupDefinition> definitions, FormerContext context) {
        Set<String> defined = definitions.stream().map(GroupDefinition::getId).collect(Collectors.toSet());
        Set<GroupModel> toBeRemoved = context.getRealm().getGroupsStream()
            .filter(m -> !defined.contains(m.getId())).collect(Collectors.toSet());

        toBeRemoved.forEach(m -> remove(m, context.getRealm()));
    }

    private void remove(GroupModel group, RealmModel realm) {
        log.infof("Group %s is present but not defined, deleting it", group.getId());
        realm.removeGroup(group);
    }

    @Override
    public Class<GroupDefinition> getDefinitionClass() {
        return GroupDefinition.class;
    }
}
