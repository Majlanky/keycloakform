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

package com.groocraft.keycloakform.former.generic;

import com.groocraft.keycloakform.definition.Definition;
import com.groocraft.keycloakform.former.CollectionFormer;
import com.groocraft.keycloakform.former.FormerContext;
import com.groocraft.keycloakform.former.FormersFactory;
import com.groocraft.keycloakform.former.SyncMode;

import java.util.Collection;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public abstract class DefaultCollectionFormer<DefinitionT extends Definition> implements
                                                                              CollectionFormer<DefinitionT> {

    private final FormersFactory formersFactory;

    @Override
    public void form(Collection<DefinitionT> definitions, FormerContext context, SyncMode syncMode) {
        definitions.forEach(d -> formOne(d, context));

        if(syncMode == SyncMode.FULL) {
            deleteUndeclaredKeycloakResources(definitions, context);
        }
    }

    protected abstract void deleteUndeclaredKeycloakResources(Collection<DefinitionT> definitions, FormerContext context);

    protected void formOne(DefinitionT definition, FormerContext context) {
        formersFactory.getFor(definition).form(definition, context);
    }

    protected FormersFactory getFormersFactory() {
        return formersFactory;
    }

}
