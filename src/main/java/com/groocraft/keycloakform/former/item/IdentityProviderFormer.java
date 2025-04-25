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

import com.groocraft.keycloakform.definition.IdentityProviderDefinition;
import com.groocraft.keycloakform.former.FormerContext;
import com.groocraft.keycloakform.former.generic.DefaultItemFormer;
import com.groocraft.keycloakform.updater.IdentityProviderUpdater;

import org.keycloak.models.IdentityProviderModel;

import lombok.CustomLog;

@CustomLog
public class IdentityProviderFormer extends DefaultItemFormer<IdentityProviderModel, IdentityProviderDefinition> {

    private final IdentityProviderUpdater updater = new IdentityProviderUpdater();

    public IdentityProviderFormer() {
        super(log);
    }

    @Override
    protected IdentityProviderModel getModel(IdentityProviderDefinition definition, FormerContext context) {
        return context.getRealm().getIdentityProviderByAlias(definition.getAlias());
    }

    @Override
    protected IdentityProviderModel create(IdentityProviderDefinition definition, FormerContext context) {
        IdentityProviderModel provider = new IdentityProviderModel();
        provider.setAlias(definition.getAlias());
        return provider;
    }

    @Override
    protected void update(IdentityProviderModel provider, IdentityProviderDefinition definition, FormerContext context) {
        updater.update(provider, definition, context);

        IdentityProviderModel existing = getModel(definition, context);
        if (existing == null) {
            context.getRealm().addIdentityProvider(provider);
        } else {
            context.getRealm().updateIdentityProvider(provider);
        }
    }

    @Override
    protected Class<IdentityProviderModel> getKeycloakResourceClass() {
        return IdentityProviderModel.class;
    }

    @Override
    protected String getLogIdentifier(IdentityProviderDefinition definition) {
        return "Identity provider " + definition.getAlias();
    }

    @Override
    public Class<IdentityProviderDefinition> getDefinitionClass() {
        return IdentityProviderDefinition.class;
    }
}
