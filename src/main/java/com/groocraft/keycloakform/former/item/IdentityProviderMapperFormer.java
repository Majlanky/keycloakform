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

import com.groocraft.keycloakform.definition.IdentityProviderMapperDefinition;
import com.groocraft.keycloakform.former.FormerContext;
import com.groocraft.keycloakform.former.generic.DefaultItemFormer;
import com.groocraft.keycloakform.updater.IdentityProviderMapperUpdater;

import org.keycloak.models.IdentityProviderMapperModel;

import lombok.CustomLog;

@CustomLog
public class IdentityProviderMapperFormer extends DefaultItemFormer<IdentityProviderMapperModel, IdentityProviderMapperDefinition> {

    private final IdentityProviderMapperUpdater updater = new IdentityProviderMapperUpdater();

    public IdentityProviderMapperFormer() {
        super(log);
    }

    @Override
    protected IdentityProviderMapperModel getModel(IdentityProviderMapperDefinition definition, FormerContext context) {
        return context.getRealm().getIdentityProviderMapperById(definition.getId());
    }

    @Override
    protected IdentityProviderMapperModel create(IdentityProviderMapperDefinition definition, FormerContext context) {
        IdentityProviderMapperModel model = new IdentityProviderMapperModel();
        model.setId(definition.getId());
        return model;
    }

    @Override
    protected void update(IdentityProviderMapperModel mapper, IdentityProviderMapperDefinition definition,
                          FormerContext context) {
        updater.update(mapper, definition, context);

        IdentityProviderMapperModel existing = getModel(definition, context);
        if (existing == null) {
            context.getRealm().addIdentityProviderMapper(mapper);
        } else {
            context.getRealm().updateIdentityProviderMapper(mapper);
        }
    }

    @Override
    protected Class<IdentityProviderMapperModel> getKeycloakResourceClass() {
        return IdentityProviderMapperModel.class;
    }

    @Override
    protected String getLogIdentifier(IdentityProviderMapperDefinition definition) {
        return "Identity provider mapper " + definition.getId();
    }

    @Override
    public Class<IdentityProviderMapperDefinition> getDefinitionClass() {
        return IdentityProviderMapperDefinition.class;
    }
}
