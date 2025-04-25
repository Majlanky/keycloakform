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

package com.groocraft.keycloakform.spi;

import com.groocraft.keycloakform.config.FormerConfig;
import com.groocraft.keycloakform.definition.RealmDefinition;
import com.groocraft.keycloakform.definition.deserialization.Deserialization;
import com.groocraft.keycloakform.exception.DefinitionFileDeserializationException;
import com.groocraft.keycloakform.exception.DefinitionFileReadingException;
import com.groocraft.keycloakform.former.SyncMode;
import com.groocraft.keycloakform.former.collection.RealmsFormer;
import com.groocraft.keycloakform.utils.TestFormersFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.models.KeycloakSessionTask;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.models.utils.PostMigrationEvent;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FormingInitializerTest {

    @Mock
    FormerConfig config;
    @Mock
    RealmsFormer realmsFormer;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    PostMigrationEvent event;

    String sourceFilePath;
    TestFormersFactory formersFactory = new TestFormersFactory();

    @BeforeEach
    void setUp() {
        sourceFilePath = getClass().getClassLoader().getResource("realm.json").getPath();
        formersFactory.registerCollectionMock(RealmDefinition.class, realmsFormer);
    }

    @Test
    void testInitIsThrowingWhenSourceFileNotExist() {
        when(config.getSourceFile()).thenReturn("realms.json");
        assertThatExceptionOfType(DefinitionFileReadingException.class).isThrownBy(() -> new FormingInitializer(config, formersFactory));
    }

    @Test
    void testOnEventIsStartingProcessingOfRealmsWithProperParameters() {
        when(config.getSourceFile()).thenReturn(sourceFilePath);
        when(config.isDryRun()).thenReturn(false);

        FormingInitializer initializer = new FormingInitializer(config, formersFactory);
        ArgumentCaptor<KeycloakSessionTask> task = ArgumentCaptor.forClass(KeycloakSessionTask.class);

        try (MockedStatic<KeycloakModelUtils> ms = mockStatic(KeycloakModelUtils.class)) {
            initializer.onEvent(event);
            ms.verify(() -> KeycloakModelUtils.runJobInTransaction(any(), task.capture()));
        }

        ArgumentCaptor<List<RealmDefinition>> definition = ArgumentCaptor.forClass(List.class);

        task.getValue().run(event.getFactory().create());

        verify(realmsFormer).form(definition.capture(), any(), eq(SyncMode.FULL));

        assertThat(definition.getValue()).isNotNull();
        assertThat(definition.getValue())
            .hasSize(1)
            .map(RealmDefinition::getRealm).contains("test");
    }

    @Test
    void testOnEventIsThrowingWhenUnmappableDefinition() {
        when(config.getSourceFile()).thenReturn(sourceFilePath);

        try (MockedStatic<Deserialization> pum = mockStatic(Deserialization.class)) {
            pum.when(() -> Deserialization.getRealmsFromStream(any())).thenThrow(IOException.class);
            assertThatExceptionOfType(DefinitionFileDeserializationException.class)
                .isThrownBy(() -> new FormingInitializer(config, formersFactory));
        }

    }

}