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
import com.groocraft.keycloakform.definition.RealmDefinition;
import com.groocraft.keycloakform.former.FormersFactory;
import com.groocraft.keycloakform.former.generic.DefaultItemFormer;
import com.groocraft.keycloakform.updater.RealmUpdater;

import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.services.managers.RealmManager;

import lombok.CustomLog;

/**
 * RealmFormer is responsible for managing the forming lifecycle of realms in a Keycloak instance.
 * It provides functionality to create, retrieve, update, and manage Realm resources
 * following their definitions.
 * This class inherits from {@link DefaultItemFormer} and extends
 * its behavior specifically for {@link RealmModel} (Keycloak realm representation) and
 * {@link RealmDefinition} (realm configuration definition).
 * During the formation of realms, the following operations are handled:
 * - Retrieval: Fetches a realm by its ID or name from the Keycloak session.
 * - Creation: Creates a new realm when it does not already exist.
 * - Update: Updates an existing realm's configuration based on the provided definition.
 * Key responsibilities:
 * - Implements the abstract methods of {@code ItemFormer} specific to realms.
 * - Handles logging and dry-run operations for safer updates.
 * Attributes:
 * - {@code updater}: Instance of {@code RealmUpdater} responsible for updating realm properties.
 *
 * @author Majlanky
 */
@CustomLog
public class RealmFormer extends DefaultItemFormer<RealmModel, RealmDefinition> {

    private final RealmUpdater updater = new RealmUpdater();
    private final FormersFactory formersFactory;

    public RealmFormer(FormersFactory formersFactory) {
        super(log);
        this.formersFactory = formersFactory;
    }

    @Override
    public void form(RealmDefinition definition, KeycloakSession session, boolean dryRun) {
        super.form(definition, session, dryRun);

        formersFactory.getForCollectionOf(ClientDefinition.class).form(DefinitionMapping.cast(definition.getClients()), session, dryRun);

        session.getContext().setRealm(null);
    }

    @Override
    protected void update(RealmModel realmModel, RealmDefinition definition, KeycloakSession session, boolean dryRun,
                          String logIdentifier) {
        super.update(realmModel, definition, session, dryRun, logIdentifier);
        session.getContext().setRealm(realmModel);
    }

    @Override
    protected RealmModel getKeycloakResource(RealmDefinition definition, KeycloakSession session) {
        if (definition.getId() != null) {
            return session.realms().getRealm(definition.getId());
        }
        return session.realms().getRealmByName(definition.getRealm());
    }

    @Override
    protected RealmModel create(RealmDefinition definition, KeycloakSession session) {
        if (definition.getId() != null) {
            return new RealmManager(session).createRealm(definition.getId(), definition.getRealm());
        }
        return new RealmManager(session).createRealm(definition.getRealm());
    }

    @Override
    protected void update(RealmModel resource, RealmDefinition definition) {
        updater.update(resource, definition);
    }

    @Override
    protected void updateCommit(RealmModel clientModel, KeycloakSession session) {
        //Nothing to do here as realm model is auto-commit
    }

    @Override
    protected Class<RealmModel> getKeycloakResourceClass() {
        return RealmModel.class;
    }

    @Override
    protected String getLogIdentifier(RealmDefinition definition) {
        return "Realm " + definition.getRealm();
    }

    @Override
    public Class<RealmDefinition> getDefinitionClass() {
        return RealmDefinition.class;
    }
}
