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

package com.groocraft.keycloakform.former;

/**
 * Former implementation cares about forming Keycloak internal state to the desired state following the definition.
 * One former is designed to form only one type of resource
 *
 * @param <DefinitionT> defines type of definition of resource state
 */
public interface ItemFormer<DefinitionT> {

    /**
     * Form the Keycloak internal state to the desired state following the provided definition.
     *
     * @param definition the definition of the resource state
     * @param context    context of forming
     */
    void form(DefinitionT definition, FormerContext context);

    /**
     * Retrieves the class object that represents the type of the definition associated with the implementation.
     *
     * @return the {@code Class} object of the type parameter {@code DefinitionT}.
     */
    Class<DefinitionT> getDefinitionClass();

}
