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

import com.groocraft.keycloakform.former.collection.ClientsFormer;
import com.groocraft.keycloakform.former.collection.ProtocolMappersFormer;
import com.groocraft.keycloakform.former.collection.RealmsFormer;
import com.groocraft.keycloakform.former.item.ClientFormer;
import com.groocraft.keycloakform.former.item.ProtocolMapperFormer;
import com.groocraft.keycloakform.former.item.RealmFormer;

import java.util.HashMap;
import java.util.Map;

/**
 * The {@code Formers} class is an implementation of the {@link FormersFactory} interface,
 * providing a mechanism for managing and retrieving {@link ItemFormer} implementations based on the types of resource definitions.
 * It maintains a cache of {@link ItemFormer} instances, each associated with a specific type of definition class,
 * to facilitate efficient retrieval and reuse of formers during the formation process.
 * The {@code Formers} class initializes itself by registering predefined formers such as {@code RealmsFormer} and {@code RealmFormer}.
 * These formers handle the formation of specific resource categories, ensuring their desired state aligns with the defined configuration.
 *
 * @author Majlanky
 */
public class Formers implements FormersFactory {

    private final Map<Class<?>, ItemFormer<?>> cache = new HashMap<>();
    private final Map<Class<?>, CollectionFormer<?>> collectionCache = new HashMap<>();

    public Formers() {
        addForCollection(new RealmsFormer(this));
        addForCollection(new ClientsFormer(this));
        addForCollection(new ProtocolMappersFormer(this));
        add(new RealmFormer(this));
        add(new ClientFormer(this));
        add(new ProtocolMapperFormer());
    }

    @Override
    public <DefinitionT> ItemFormer<DefinitionT> getFor(Class<DefinitionT> definitionClass) {
        return (ItemFormer<DefinitionT>) cache.get(definitionClass);
    }

    @Override
    public <DefinitionT> CollectionFormer<DefinitionT> getForCollectionOf(Class<DefinitionT> definitionClass) {
        return (CollectionFormer<DefinitionT>) collectionCache.get(definitionClass);
    }

    private void add(ItemFormer<?> itemFormer) {
        cache.put(itemFormer.getDefinitionClass(), itemFormer);
    }

    private void addForCollection(CollectionFormer<?> collectionFormer) {
        collectionCache.put(collectionFormer.getDefinitionClass(), collectionFormer);
    }

}
