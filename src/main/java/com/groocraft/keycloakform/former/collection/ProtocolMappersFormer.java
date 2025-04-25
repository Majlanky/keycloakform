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

import com.groocraft.keycloakform.definition.ProtocolMapperDefinition;
import com.groocraft.keycloakform.former.FormerContext;
import com.groocraft.keycloakform.former.FormersFactory;
import com.groocraft.keycloakform.former.generic.DefaultCollectionFormer;

import org.keycloak.models.ProtocolMapperContainerModel;
import org.keycloak.models.ProtocolMapperModel;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.CustomLog;

@CustomLog
public class ProtocolMappersFormer extends DefaultCollectionFormer<ProtocolMapperDefinition> {

    public ProtocolMappersFormer(FormersFactory formersFactory) {
        super(formersFactory);
    }

    @Override
    protected void deleteUndeclaredKeycloakResources(Collection<ProtocolMapperDefinition> definitions, FormerContext context) {
        Set<String> definedMappers = definitions.stream().map(ProtocolMapperDefinition::getName).collect(Collectors.toSet());

        ProtocolMapperContainerModel container = getCurrentlyManagedContainer(context);

        container.getProtocolMappersStream()
            .filter(m -> !definedMappers.contains(m.getName()))
            .forEach(m -> remove(m, container));
    }

    private void remove(ProtocolMapperModel mapper, ProtocolMapperContainerModel container) {
        log.infof("Protocol mapper %s is present but not defined, deleting it", mapper.getName());
        container.removeProtocolMapper(mapper);
    }

    @Override
    public Class<ProtocolMapperDefinition> getDefinitionClass() {
        return ProtocolMapperDefinition.class;
    }

    private ProtocolMapperContainerModel getCurrentlyManagedContainer(FormerContext context) {
        if (context.getClient() != null) {
            return context.getClient();
        }
        return context.getClientScope();
    }
}
