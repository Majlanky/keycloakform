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

package com.groocraft.keycloakform.updater;

import com.groocraft.keycloakform.definition.RequiredActionDefinition;
import com.groocraft.keycloakform.former.FormerContext;

import org.keycloak.models.RequiredActionProviderModel;

import java.util.LinkedHashMap;
import java.util.Map;

public class RequiredActionUpdater implements Updater<RequiredActionProviderModel, RequiredActionDefinition> {

    @Override
    public void update(RequiredActionProviderModel model, RequiredActionDefinition definition, FormerContext context) {
        if (definition == null) {
            return;
        }

        if (definition.getName() != null) {
            model.setName(definition.getName());
        }
        model.setEnabled(definition.isEnabled());
        model.setDefaultAction(definition.isDefaultAction());
        if (definition.getProviderId() != null) {
            model.setProviderId(definition.getProviderId());
        }
        model.setPriority(definition.getPriority());
        if (definition.getConfig() != null) {
            Map<String, String> map = model.getConfig();
            if (map != null) {
                map.clear();
                map.putAll(definition.getConfig());
            } else {
                model.setConfig(new LinkedHashMap<>(definition.getConfig()));
            }
        }
    }

}
