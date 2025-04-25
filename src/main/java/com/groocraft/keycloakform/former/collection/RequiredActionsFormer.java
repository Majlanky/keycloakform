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

import com.groocraft.keycloakform.definition.RequiredActionDefinition;
import com.groocraft.keycloakform.former.FormerContext;
import com.groocraft.keycloakform.former.FormersFactory;
import com.groocraft.keycloakform.former.generic.DefaultCollectionFormer;

import org.keycloak.models.RealmModel;
import org.keycloak.models.RequiredActionProviderModel;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.CustomLog;

@CustomLog
public class RequiredActionsFormer extends DefaultCollectionFormer<RequiredActionDefinition> {

    public RequiredActionsFormer(FormersFactory formersFactory) {
        super(formersFactory);
    }

    @Override
    protected void deleteUndeclaredKeycloakResources(Collection<RequiredActionDefinition> definitions, FormerContext context) {
        Set<String> defined = definitions.stream().map(RequiredActionDefinition::getAlias).collect(Collectors.toSet());
        Set<RequiredActionProviderModel> toBeRemoved = context.getRealm().getRequiredActionProvidersStream()
            .filter(m -> !defined.contains(m.getAlias())).collect(Collectors.toSet());

        toBeRemoved.forEach(m -> remove(m, context.getRealm()));
    }

    private void remove(RequiredActionProviderModel model, RealmModel realm) {
        log.infof("Required action %s is present but not defined, deleting it", model.getAlias());
        realm.removeRequiredActionProvider(model);
    }

    @Override
    public Class<RequiredActionDefinition> getDefinitionClass() {
        return RequiredActionDefinition.class;
    }
}
