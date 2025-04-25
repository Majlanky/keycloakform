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

package com.groocraft.keycloakform.former.collection;

import com.groocraft.keycloakform.definition.ClientScopeDefinition;
import com.groocraft.keycloakform.definition.DefinitionMapping;
import com.groocraft.keycloakform.definition.deserialization.Deserialization;
import com.groocraft.keycloakform.former.FormerContext;
import com.groocraft.keycloakform.former.SyncMode;
import com.groocraft.keycloakform.former.item.ClientScopeFormer;
import com.groocraft.keycloakform.utils.TestFormersFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.models.ClientScopeModel;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientScopesFormerTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS) FormerContext context;
    @Mock ClientScopeFormer clientScopeFormer;

    TestFormersFactory formersFactory = new TestFormersFactory();
    Collection<ClientScopeDefinition> definitions;
    ClientScopesFormer former;

    @BeforeEach
    void setUp() throws IOException {
        URL definitionUrl = getClass().getClassLoader().getResource("realm.json");
        definitions = DefinitionMapping.cast(Deserialization.getRealmsFromStream(definitionUrl.openStream()).getFirst().getClientScopes());
        formersFactory.registerMock(ClientScopeDefinition.class, clientScopeFormer);
        former = new ClientScopesFormer(formersFactory);
    }

    @Test
    void testFormerIsFormingOnlyManagedClients() {
        former.form(definitions, context, SyncMode.FULL);

        ArgumentCaptor<ClientScopeDefinition> definitionCaptor = ArgumentCaptor.forClass(ClientScopeDefinition.class);

        verify(clientScopeFormer, times(13)).form(definitionCaptor.capture(), eq(context));
        verifyNoMoreInteractions(clientScopeFormer);

        assertThat(definitionCaptor.getAllValues()).map(ClientScopeDefinition::getName).containsExactlyInAnyOrder(
            "basic",
            "microprofile-jwt",
            "web-origins",
            "test-oidc-client-scope",
            "phone",
            "roles",
            "acr",
            "test-saml-client-scope",
            "email",
            "role_list",
            "offline_access",
            "profile",
            "address");
    }

    @Test
    void testFormerIsDeletingNoClientWhenSyncModeIgnore() {
        ClientScopeModel firstModel = mock(ClientScopeModel.class);
        ClientScopeModel secondModel = mock(ClientScopeModel.class);

        when(context.getRealm().getClientScopesStream()).thenReturn(Stream.of(firstModel, secondModel));

        former.form(definitions, context, SyncMode.IGNORE);

        verify(context.getRealm(), never()).removeClientScope(any());
    }

    @Test
    void testFormerIsDeletingExcessRealmWhenSyncModeFull() {
        ClientScopeModel firstModel = mock(ClientScopeModel.class);
        ClientScopeModel secondModel = mock(ClientScopeModel.class);
        when(firstModel.getName()).thenReturn("first");
        when(firstModel.getId()).thenReturn("first-id");
        when(secondModel.getName()).thenReturn("second");
        when(secondModel.getId()).thenReturn("second-id");

        ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);

        when(context.getRealm().getClientScopesStream()).thenReturn(Stream.of(firstModel, secondModel));
        when(context.getRealm().removeClientScope(idCaptor.capture())).thenReturn(true);

        former.form(definitions, context, SyncMode.FULL);

        assertThat(idCaptor.getAllValues()).containsExactlyInAnyOrder("first-id", "second-id");

    }

}