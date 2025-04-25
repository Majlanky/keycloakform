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

package com.groocraft.keycloakform.former.item;

import com.groocraft.keycloakform.definition.ClientScopeDefinition;
import com.groocraft.keycloakform.definition.DefinitionMapping;
import com.groocraft.keycloakform.definition.ProtocolMapperDefinition;
import com.groocraft.keycloakform.definition.RealmDefinition;
import com.groocraft.keycloakform.definition.deserialization.Deserialization;
import com.groocraft.keycloakform.former.FormerContext;
import com.groocraft.keycloakform.former.SyncMode;
import com.groocraft.keycloakform.former.collection.ProtocolMappersFormer;
import com.groocraft.keycloakform.utils.TestFormersFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.models.ClientScopeModel;
import org.keycloak.models.RealmModel;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientScopeFormerTest {

    TestFormersFactory formersFactory = new TestFormersFactory();

    @Mock(answer = Answers.RETURNS_DEEP_STUBS) FormerContext context;
    @Mock RealmModel realmModel;
    @Mock ClientScopeModel clientScopeModel;
    @Mock ProtocolMappersFormer protocolMappersFormer;

    ClientScopeFormer former;
    ClientScopeDefinition definition;
    ClientScopeDefinition unmanagedDefinition;

    @BeforeEach
    void setUp() throws IOException {
        former = new ClientScopeFormer(formersFactory);
        formersFactory.registerCollectionMock(ProtocolMapperDefinition.class, protocolMappersFormer);
        URL definitionUrl = getClass().getClassLoader().getResource("realm.json");
        List<RealmDefinition> definitions = Deserialization.getRealmsFromStream(definitionUrl.openStream());
        definition = DefinitionMapping.cast(definitions
            .stream().filter(rd -> rd.getRealm().equals("test")).findFirst().orElseThrow()
            .getClientScopes()
            .stream().filter(c -> c.getName().equals("email")).findFirst().orElseThrow());
        unmanagedDefinition = DefinitionMapping.cast(definitions
            .stream().filter(rd -> rd.getRealm().equals("test")).findFirst().orElseThrow()
            .getClientScopes()
            .stream().filter(c -> c.getName().equals("address")).findFirst().orElseThrow());
    }

    @Test
    void testSubFormersAreCalledPassingProperValues() {
        when(context.getRealm()).thenReturn(realmModel);
        when(realmModel.getClientScopesStream()).thenReturn(Stream.of(clientScopeModel));
        when(clientScopeModel.getName()).thenReturn("email");

        former.form(definition, context);

        verify(protocolMappersFormer).form(any(), eq(context), eq(SyncMode.FULL));
    }

    @Test
    void testKeycloakSessionContextIsSetAndUnset() {
        when(context.getRealm()).thenReturn(realmModel);
        when(realmModel.getClientScopesStream()).thenReturn(Stream.of(clientScopeModel));
        when(clientScopeModel.getName()).thenReturn("email");

        former.form(definition, context);

        verify(context).setClientScope(any(ClientScopeModel.class));
        verify(context).setClientScope(null);
    }

    @Test
    void testFormerTakesCreatesWhenMissingAndNotDryRun() {
        when(context.getRealm()).thenReturn(realmModel);
        when(realmModel.getClientScopesStream()).thenReturn(Stream.of());
        ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);

        when(realmModel.addClientScope(idCaptor.capture(), any())).thenReturn(clientScopeModel);
        former.form(definition, context);

        assertThat(idCaptor.getAllValues()).containsExactly("03d2af9e-74c5-4ea1-af9c-a899cb7633aa");
    }

    @Test
    void testFormerNotCreatesMissingClientScopeWhenNotManaged() {
        when(context.getRealm()).thenReturn(realmModel);

        former.form(unmanagedDefinition, context);

        verify(realmModel, Mockito.never()).addClientScope(any(), any());
    }

    @Test
    void testFormerUpdatesExistingClientScope() {
        when(context.getRealm()).thenReturn(realmModel);
        when(realmModel.getClientScopesStream()).thenReturn(Stream.of(clientScopeModel));
        when(clientScopeModel.getName()).thenReturn("email");
        when(clientScopeModel.getDescription()).thenReturn("old description");

        former.form(definition, context);

        verify(clientScopeModel).setDescription("OpenID Connect built-in scope: email");
    }

}