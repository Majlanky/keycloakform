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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSessionFactory;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemFormerRegistrarTest {

    @Mock Config.Scope scope;

    private FormerRegistrar registrar;
    private String sourceFilePath;

    @BeforeEach
    void setUp() {
        registrar = new FormerRegistrar();
        sourceFilePath = getClass().getClassLoader().getResource("realm-export.json").getPath();
        scope = Mockito.mock(Config.Scope.class);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testInitIsPassingConfigurationProperly(boolean dryRun) {
        AtomicReference<FormerConfig> config = new AtomicReference<>();
        when(scope.get("sourceFile", "")).thenReturn(sourceFilePath);
        when(scope.getBoolean("dryRun", false)).thenReturn(dryRun);
        try (MockedConstruction<FormingInitializer> mc = Mockito.mockConstruction(FormingInitializer.class,
            (m, c) -> config.set((FormerConfig) c.arguments().get(0)))) {
            registrar.init(scope);
            assertThat(config.get()).isNotNull();
            assertThat(config.get().getSourceFile()).isEqualTo(sourceFilePath);
            assertThat(config.get().isDryRun()).isEqualTo(dryRun);
        }
    }

    @Test
    void testInitIsThrowingWhenNoSourceFileConfigured() {
        when(scope.get("sourceFile", "")).thenReturn("");
        when(scope.getBoolean("dryRun", false)).thenReturn(false);
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> registrar.init(scope));
    }

    @Test
    void testPostInit() {
        KeycloakSessionFactory mockFactory = Mockito.mock(KeycloakSessionFactory.class);
        when(scope.get("sourceFile", "")).thenReturn(sourceFilePath);
        when(scope.getBoolean("dryRun", false)).thenReturn(false);
        registrar.init(scope);
        registrar.postInit(mockFactory);
        Mockito.verify(mockFactory, Mockito.times(1)).register(any(FormingInitializer.class));
    }

    @Test
    void testClose() {
        // No exceptional behavior to be tested for this method as it's empty.
        assertDoesNotThrow(() -> registrar.close());
    }

    @Test
    void testGetId() {
        assertThat(registrar.getId()).isEqualTo("keycloakform");
    }
}
