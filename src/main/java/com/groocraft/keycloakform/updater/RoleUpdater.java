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

import com.groocraft.keycloakform.definition.RoleDefinition;
import com.groocraft.keycloakform.former.FormerContext;

import org.keycloak.models.RoleModel;

public class RoleUpdater implements Updater<RoleModel, RoleDefinition> {

    @Override
    public void update(RoleModel model, RoleDefinition definition, FormerContext context) {
        if (definition == null) {
            return;
        }

        //Attributes must go first as attributes are used internally instead of dedicated attributes
        if (definition.getAttributes() != null) {
            if (model.getAttributes() != null) {
                model.getAttributes().keySet().forEach(model::removeAttribute);
            }
            definition.getAttributes().forEach(model::setAttribute);
        }

        if (definition.getDescription() != null) {
            model.setDescription(definition.getDescription());
        }
        if (definition.getName() != null) {
            model.setName(definition.getName());
        }
    }

}
