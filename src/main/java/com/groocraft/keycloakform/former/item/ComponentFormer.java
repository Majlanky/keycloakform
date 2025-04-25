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

import com.groocraft.keycloakform.definition.ComponentDefinition;
import com.groocraft.keycloakform.former.FormerContext;
import com.groocraft.keycloakform.former.generic.DefaultItemFormer;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.utils.RepresentationToModel;
import org.keycloak.representations.idm.ComponentRepresentation;

import lombok.CustomLog;

@CustomLog
public class ComponentFormer extends DefaultItemFormer<ComponentModel, ComponentDefinition> {

    public ComponentFormer() {
        super(log);
    }

    @Override
    protected ComponentModel getModel(ComponentDefinition definition, FormerContext context) {
        return context.getRealm().getComponent(definition.getId());
    }

    @Override
    protected ComponentModel create(ComponentDefinition definition, FormerContext context) {
        ComponentModel model = new ComponentModel();
        model.setId(definition.getId());
        return model;
    }

    @Override
    protected void update(ComponentModel model, ComponentDefinition definition, FormerContext context) {
        RepresentationToModel.updateComponent(context.getSession(), definitionToRepresentation(definition), model, false);
        ComponentModel existing = getModel(definition, context);
        if(existing == null){
            context.getRealm().addComponentModel(model);
        } else {
            context.getRealm().updateComponent(model);
        }
    }

    private ComponentRepresentation definitionToRepresentation(ComponentDefinition definition){
        ComponentRepresentation representation = new ComponentRepresentation();
        representation.setId(definition.getId());
        representation.setName(definition.getName());
        representation.setConfig(definition.getConfig());
        representation.setProviderId(definition.getProviderId());
        representation.setSubType(definition.getSubType());
        representation.setParentId(definition.getParentId());
        representation.setProviderType(definition.getProviderType());
        return representation;
    }

    @Override
    protected Class<ComponentModel> getKeycloakResourceClass() {
        return ComponentModel.class;
    }

    @Override
    protected String getLogIdentifier(ComponentDefinition definition) {
        return "Component " + definition.getId();
    }

    @Override
    public Class<ComponentDefinition> getDefinitionClass() {
        return ComponentDefinition.class;
    }
}
