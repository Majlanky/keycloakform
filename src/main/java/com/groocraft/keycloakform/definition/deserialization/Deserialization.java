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

package com.groocraft.keycloakform.definition.deserialization;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.groocraft.keycloakform.definition.DefinitionMapping;
import com.groocraft.keycloakform.definition.RealmDefinition;

import org.keycloak.Config;
import org.keycloak.util.JsonSerialization;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Deserialization {

    public static ObjectMapper getObjectMapper(ObjectMapper originalMapper) {
        ObjectMapper mapper = originalMapper.copy();

        DefinitionMapping.init(mapper);

        return mapper;
    }

    public static List<RealmDefinition> getRealmsFromStream(InputStream is) throws IOException {
        List<RealmDefinition> result = new ArrayList<>();

        JsonFactory factory = getObjectMapper(JsonSerialization.mapper).getFactory();
        JsonParser parser = factory.createParser(is);
        try {
            parser.nextToken();

            if (parser.getCurrentToken() == JsonToken.START_ARRAY) {
                // Case with more realms in stream
                parser.nextToken();

                while (parser.getCurrentToken() == JsonToken.START_OBJECT) {
                    RealmDefinition realmRep = parser.readValueAs(RealmDefinition.class);
                    parser.nextToken();

                    // Ensure that master realm is imported first
                    if (Config.getAdminRealm().equals(realmRep.getRealm())) {
                        result.add(0, realmRep);
                    } else {
                        result.add(realmRep);
                    }
                }

            } else if (parser.getCurrentToken() == JsonToken.START_OBJECT) {
                // Case with single realm in stream
                RealmDefinition realmDefinition = parser.readValueAs(RealmDefinition.class);
                result.add(realmDefinition);
            }
        } finally {
            parser.close();
        }

        return result;
    }

}
