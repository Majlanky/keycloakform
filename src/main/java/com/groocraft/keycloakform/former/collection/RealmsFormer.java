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

import com.groocraft.keycloakform.definition.RealmDefinition;
import com.groocraft.keycloakform.former.FormerContext;
import com.groocraft.keycloakform.former.FormersFactory;
import com.groocraft.keycloakform.former.ItemFormer;
import com.groocraft.keycloakform.former.generic.DefaultCollectionFormer;

import org.keycloak.Config;
import org.keycloak.models.RealmModel;
import org.keycloak.services.managers.RealmManager;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import lombok.CustomLog;

/**
 * RealmsFormer is responsible for forming the state of Keycloak realms based on a given list of {@link RealmDefinition}.
 * It implements the {@link ItemFormer} interface specific to the lsit of {@link RealmDefinition} type and orchestrates the
 * creation, update, or removal of realms to match the desired configuration.
 * The formation process includes:
 * - Iterating over and processing the {@link RealmDefinition} entries in the provided the list.
 * - Handling unmanaged realms by skipping their formation.
 * - Delegating realm formation tasks to other {@link ItemFormer} implementations through the {@link FormersFactory}.
 * - Identifying and removing realms not defined in the configuration but present in Keycloak, unless it is the admin realm.
 * This former can operate in two modes:
 * - Dry run: Where changes are identified and logged without being applied.
 * - Execute: Where changes are applied directly to the Keycloak environment.
 * Thread safety: This class is not thread-safe due to the lack of synchronization in stateful operations.
 *
 * @author Majlanky
 */
@CustomLog
public class RealmsFormer extends DefaultCollectionFormer<RealmDefinition> {

    public RealmsFormer(FormersFactory formersFactory) {
        super(formersFactory);
    }

    protected void deleteUndeclaredKeycloakResources(Collection<RealmDefinition> definitions, FormerContext context) {
        Set<String> definedRealms = new HashSet<>(definitions.stream().map(RealmDefinition::getRealm).toList());
        //to be sure that admin realm is untouched
        definedRealms.add(Config.getAdminRealm());
        context.getSession().realms().getRealmsStream()
            .filter(m -> !definedRealms.contains(m.getName()))
            .forEach(m -> remove(context, m));
    }

    private void remove(FormerContext context, RealmModel model) {
        log.infof("Realm %s is present but not defined, deleting it", model.getName());
        boolean removed = new RealmManager(context.getSession()).removeRealm(model);
        if (!removed) {
            log.infof("Realm %s was not removed for unknown reason", model.getName());
        }
    }

    @Override
    public Class<RealmDefinition> getDefinitionClass() {
        return RealmDefinition.class;
    }
}
