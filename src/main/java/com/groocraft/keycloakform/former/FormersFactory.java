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
 * Interface defining a factory for retrieving {@link ItemFormer} instances based on the class type of the associated resource definition.
 * A {@link ItemFormer} is responsible for forming and managing Keycloak's internal state based on provided definitions of specific resource types.
 * This factory provides a way to retrieve the appropriate {@link ItemFormer} implementation corresponding to a given resource definition
 * class or to a specific instance of a resource definition.
 *
 * @author Majlanky
 */
public interface FormersFactory {

    <DefinitionT> ItemFormer<DefinitionT> getFor(Class<DefinitionT> definitionClass);

    <DefinitionT> CollectionFormer<DefinitionT> getForCollectionOf(Class<DefinitionT> definitionTClass);

    default <DefinitionT> ItemFormer<DefinitionT> getFor(DefinitionT definition) {
        if (definition == null) {
            throw new IllegalArgumentException("Definition object must not be null when asking for former");
        }
        Class<DefinitionT> clazz = (Class<DefinitionT>) definition.getClass();
        return getFor(clazz);
    }

    default <DefinitionT> CollectionFormer<DefinitionT> getForCollectionOf(DefinitionT definition) {
        if (definition == null) {
            throw new IllegalArgumentException("Definition object must not be null when asking for former");
        }
        Class<DefinitionT> clazz = (Class<DefinitionT>) definition.getClass();
        return getForCollectionOf(clazz);
    }

}
