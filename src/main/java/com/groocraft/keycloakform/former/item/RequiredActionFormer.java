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

import com.groocraft.keycloakform.definition.RequiredActionDefinition;
import com.groocraft.keycloakform.former.FormerContext;
import com.groocraft.keycloakform.former.generic.DefaultItemFormer;
import com.groocraft.keycloakform.updater.RequiredActionUpdater;

import org.keycloak.models.RequiredActionProviderModel;
import org.keycloak.models.utils.KeycloakModelUtils;

import lombok.CustomLog;

@CustomLog
public class RequiredActionFormer extends DefaultItemFormer<RequiredActionProviderModel, RequiredActionDefinition> {

    private final RequiredActionUpdater updater = new RequiredActionUpdater();

    public RequiredActionFormer() {
        super(log);
    }

    @Override
    protected RequiredActionProviderModel getModel(RequiredActionDefinition definition, FormerContext context) {
        return context.getRealm().getRequiredActionProviderByAlias(definition.getAlias());
    }

    @Override
    protected RequiredActionProviderModel create(RequiredActionDefinition definition, FormerContext context) {
        RequiredActionProviderModel model = new RequiredActionProviderModel();
        model.setId(KeycloakModelUtils.generateId());
        model.setAlias(definition.getAlias());
        return model;
    }

    @Override
    protected void update(RequiredActionProviderModel model, RequiredActionDefinition definition, FormerContext context) {
        updater.update(model, definition, context);

        RequiredActionProviderModel existing = getModel(definition, context);
        if (existing == null) {
            context.getRealm().addRequiredActionProvider(model);
        } else {
            context.getRealm().updateRequiredActionProvider(model);
        }
    }

    @Override
    protected Class<RequiredActionProviderModel> getKeycloakResourceClass() {
        return RequiredActionProviderModel.class;
    }

    @Override
    protected String getLogIdentifier(RequiredActionDefinition definition) {
        return "Required action " + definition.getAlias();
    }

    @Override
    public Class<RequiredActionDefinition> getDefinitionClass() {
        return RequiredActionDefinition.class;
    }
}
