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

import com.groocraft.keycloakform.definition.IdentityProviderDefinition;
import com.groocraft.keycloakform.former.FormerContext;
import com.groocraft.keycloakform.former.FormersFactory;
import com.groocraft.keycloakform.former.generic.DefaultCollectionFormer;

import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.RealmModel;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.CustomLog;

@CustomLog
public class IdentityProvidersFormer extends DefaultCollectionFormer<IdentityProviderDefinition> {

    public IdentityProvidersFormer(FormersFactory formersFactory) {
        super(formersFactory);
    }

    @Override
    protected void deleteUndeclaredKeycloakResources(Collection<IdentityProviderDefinition> definitions, FormerContext context) {
        Set<String> defined = definitions.stream().map(IdentityProviderDefinition::getAlias).collect(Collectors.toSet());
        Set<IdentityProviderModel> toBeRemoved = context.getRealm().getIdentityProvidersStream()
            .filter(m -> !defined.contains(m.getAlias())).collect(Collectors.toSet());

        toBeRemoved.forEach(m -> remove(m, context.getRealm()));
    }

    private void remove(IdentityProviderModel provider, RealmModel realm) {
        log.infof("Identity provider %s is present but not defined, deleting it", provider.getAlias());
        realm.removeIdentityProviderByAlias(provider.getAlias());
    }

    @Override
    public Class<IdentityProviderDefinition> getDefinitionClass() {
        return IdentityProviderDefinition.class;
    }
}
