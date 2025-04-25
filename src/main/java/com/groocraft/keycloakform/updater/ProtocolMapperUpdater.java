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

import com.groocraft.keycloakform.definition.ProtocolMapperDefinition;
import com.groocraft.keycloakform.former.FormerContext;

import org.keycloak.models.ProtocolMapperModel;

import java.util.LinkedHashMap;
import java.util.Map;

public class ProtocolMapperUpdater implements Updater<ProtocolMapperModel, ProtocolMapperDefinition> {

    @Override
    public void update(ProtocolMapperModel model, ProtocolMapperDefinition definition, FormerContext context) {
        if (definition == null) {
            return;
        }

        if (definition.getName() != null) {
            model.setName(definition.getName());
        }
        if (definition.getProtocol() != null) {
            model.setProtocol(definition.getProtocol());
        }
        if (definition.getProtocolMapper() != null) {
            model.setProtocolMapper(definition.getProtocolMapper());
        }
        if (definition.getConfig() != null) {
            Map<String, String> config = model.getConfig();
            if (config != null) {
                config.clear();
                config.putAll(definition.getConfig());
            } else {
                model.setConfig(new LinkedHashMap<>(definition.getConfig()));
            }
        }
    }

}
