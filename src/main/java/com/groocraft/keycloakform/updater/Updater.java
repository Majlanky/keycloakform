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

import com.groocraft.keycloakform.former.FormerContext;
import com.groocraft.keycloakform.former.ItemFormer;

/**
 * The interface is support interface that defines method for updating Keycloak resource, usually a model by
 * data from definition. Every Keycloak resource does not necessarily need the updater, it is just for cases
 * when updating logic of the resource is non-trivial and would swole {@link ItemFormer}
 *
 * @param <KeycloakT>   type of Keycloak resource that will be updated
 * @param <DefinitionT> pojo that hold new data
 * @author Majlanky
 */
public interface Updater<KeycloakT, DefinitionT> {

    /**
     * Method must follow the logic of updating only NON-NULL attributes of definition are used, hence no
     * null will be set. {@link ItemFormer} ensures that setter called during
     * mapping are ignored when dry run or if the set value is the same as current one.
     *
     * @param keycloakResource that will be updated when not dry run and only different values are really set
     * @param definition       used as source of wanted state
     * @param context          context of the forming providing sources outside of model and definition if needed
     */
    void update(KeycloakT keycloakResource, DefinitionT definition, FormerContext context);

}
