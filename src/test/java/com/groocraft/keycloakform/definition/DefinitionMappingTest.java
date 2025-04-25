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

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefinitionMappingTest {

    @Mock ObjectMapper objectMapper;

    @Test
    void testNonDefinitionObjectThrowsExceptionDuringCast() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> DefinitionMapping.cast("test"));
    }

    @Test
    void testNonDefinitionCollectionThrowsExceptionDuringCast() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> DefinitionMapping.cast(List.of("test")));
    }

    @Test
    void testAllDefinitionMappingsAreRegistered() {
        ArgumentCaptor<Class<Object>> classCaptor = ArgumentCaptor.forClass(Class.class);
        ArgumentCaptor<JsonDeserializer<Object>> deserializerCaptor = ArgumentCaptor.forClass(JsonDeserializer.class);
        try (MockedConstruction<SimpleModule> smm = mockConstruction(SimpleModule.class, (mock, context) -> {
            when(mock.addDeserializer(classCaptor.capture(), deserializerCaptor.capture())).thenReturn(mock);
        })) {
            DefinitionMapping.init(objectMapper);

            assertThat(smm.constructed()).hasSize(1);
            verify(objectMapper).registerModule(smm.constructed().getFirst());
        }

        assertThat(classCaptor.getAllValues()).hasSize(DefinitionMapping.values().length);
        assertThat(deserializerCaptor.getAllValues()).hasSize(DefinitionMapping.values().length);

    }

}