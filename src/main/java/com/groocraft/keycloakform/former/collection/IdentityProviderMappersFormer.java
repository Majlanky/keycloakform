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

import com.groocraft.keycloakform.definition.IdentityProviderMapperDefinition;
import com.groocraft.keycloakform.former.FormerContext;
import com.groocraft.keycloakform.former.FormersFactory;
import com.groocraft.keycloakform.former.generic.DefaultCollectionFormer;

import org.keycloak.models.IdentityProviderMapperModel;
import org.keycloak.models.RealmModel;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.CustomLog;

@CustomLog
public class IdentityProviderMappersFormer extends DefaultCollectionFormer<IdentityProviderMapperDefinition> {

    public IdentityProviderMappersFormer(FormersFactory formersFactory) {
        super(formersFactory);
    }

    @Override
    protected void deleteUndeclaredKeycloakResources(Collection<IdentityProviderMapperDefinition> definitions, FormerContext context) {
        Set<String> defined = definitions.stream().map(IdentityProviderMapperDefinition::getId).collect(Collectors.toSet());
        Set<IdentityProviderMapperModel> toBeRemoved = context.getRealm().getIdentityProviderMappersStream()
            .filter(m -> !defined.contains(m.getId())).collect(Collectors.toSet());

        toBeRemoved.forEach(m -> remove(m, context.getRealm()));
    }

    private void remove(IdentityProviderMapperModel mapper, RealmModel realm) {
        log.infof("Identity provider mapper %s is present but not defined, deleting it", mapper.getId());
        realm.removeIdentityProviderMapper(mapper);
    }

    @Override
    public Class<IdentityProviderMapperDefinition> getDefinitionClass() {
        return IdentityProviderMapperDefinition.class;
    }
}
