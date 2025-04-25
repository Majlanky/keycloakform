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

package com.groocraft.keycloakform.definition;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.groocraft.keycloakform.definition.deserialization.DelegatingDeserializer;

import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.representations.idm.AuthenticationExecutionExportRepresentation;
import org.keycloak.representations.idm.AuthenticationFlowRepresentation;
import org.keycloak.representations.idm.AuthenticatorConfigRepresentation;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.ClientScopeRepresentation;
import org.keycloak.representations.idm.ComponentExportRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.IdentityProviderMapperRepresentation;
import org.keycloak.representations.idm.IdentityProviderRepresentation;
import org.keycloak.representations.idm.ProtocolMapperRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RequiredActionProviderRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.RolesRepresentation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("unchecked")
public enum DefinitionMapping {

    REALM(RealmRepresentation.class, RealmDefinition.class),
    CLIENT_SCOPE(ClientScopeRepresentation.class, ClientScopeDefinition.class),
    CLIENT(ClientRepresentation.class, ClientDefinition.class),
    CLIENT_PROTOCOL_MAPPER(ProtocolMapperRepresentation.class, ProtocolMapperDefinition.class),
    ROLE(RoleRepresentation.class, RoleDefinition.class),
    ROLES(RolesRepresentation.class, RolesDefinition.class),
    AUTHENTICATION_FLOW(AuthenticationFlowRepresentation.class, AuthenticationFlowDefinition.class),
    AUTHENTICATION_EXECUTION(AuthenticationExecutionExportRepresentation.class, AuthenticationExecutionDefinition.class),
    AUTHENTICATOR_CONFIG(AuthenticatorConfigRepresentation.class, AuthenticatorConfigDefinition.class),
    COMPONENT(ComponentExportRepresentation.class, ComponentDefinition.class),
    REQUIRED_ACTION(RequiredActionProviderRepresentation.class, RequiredActionDefinition.class),
    IDENTITY_PROVIDER(IdentityProviderRepresentation.class, IdentityProviderDefinition.class),
    IDENTITY_PROVIDER_MAPPER(IdentityProviderMapperRepresentation.class, IdentityProviderMapperDefinition.class),
    GROUP(GroupRepresentation.class, GroupDefinition.class);

    private static final HashMap<Class<?>, Class<?>> map = new HashMap<>();

    private final Class<?> originalType;
    private final Class<?> definitionType;

    static {
        for (DefinitionMapping definitionMapping : DefinitionMapping.values()) {
            map.put(definitionMapping.definitionType, definitionMapping.originalType);
        }
    }

    <OriginalT, NewT extends OriginalT> DefinitionMapping(Class<OriginalT> originalType, Class<NewT> definitionType) {
        this.originalType = originalType;
        this.definitionType = definitionType;
    }

    public static <T> T cast(Object original) {
        if (original == null) {
            return null;
        }

        if (map.containsKey(original.getClass())) {
            return (T) original;
        } else {
            throw new IllegalArgumentException(
                "Object of class " + original.getClass() + " can not be cast to any definition");
        }
    }

    public static <T> Collection<T> cast(Collection<?> original) {
        if (original == null) {
            return List.of();
        }

        Object first;
        if (original.isEmpty() || (first = original.stream().filter(Objects::nonNull).findFirst().orElse(null)) == null) {
            return (Collection<T>) original;
        }

        if (map.containsKey(first.getClass())) {
            return (List<T>) original.stream().map(DefinitionMapping::cast).toList();
        } else {
            throw new IllegalArgumentException(
                "Object of " + first.getClass() + " can not be cast to any definition");
        }
    }

    public static <T> MultivaluedHashMap<String, T> cast(MultivaluedHashMap<String, ?> original) {
        if (original == null) {
            return new MultivaluedHashMap<>();
        }

        List<?> firstList;
        Object first;
        if (original.isEmpty()
            || (firstList = original.values().iterator().next()) == null
            || (first = firstList.stream().filter(Objects::nonNull).findFirst().orElse(null)) == null) {
            return (MultivaluedHashMap<String, T>) original;
        }

        if (map.containsKey(first.getClass())) {
            return original.entrySet().stream()
                .collect(
                    MultivaluedHashMap::new,
                    (m, e) -> m.put(e.getKey(), new ArrayList<>(DefinitionMapping.cast(e.getValue()))),
                    HashMap::putAll);
        } else {
            throw new IllegalArgumentException(
                "Object of " + first.getClass() + " can not be cast to any definition");
        }
    }

    public static void init(ObjectMapper mapper) {
        SimpleModule module = new SimpleModule("definitionSerializers");
        for (DefinitionMapping definitionMapping : DefinitionMapping.values()) {
            registerDeserializerFor(definitionMapping, module);
        }
        mapper.registerModule(module);
    }

    private static <OriginalT, NewT extends OriginalT> void registerDeserializerFor(DefinitionMapping definitionMapping,
                                                                                    SimpleModule module) {
        Class<OriginalT> originalClass = (Class<OriginalT>) definitionMapping.originalType;
        Class<NewT> definitionClass = (Class<NewT>) definitionMapping.definitionType;
        module.addDeserializer(originalClass, new DelegatingDeserializer<>(definitionClass));
    }

}
