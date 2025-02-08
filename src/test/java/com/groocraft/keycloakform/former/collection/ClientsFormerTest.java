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

import com.groocraft.keycloakform.definition.ClientDefinition;
import com.groocraft.keycloakform.definition.DefinitionMapping;
import com.groocraft.keycloakform.definition.RealmDefinition;
import com.groocraft.keycloakform.definition.deserialization.Deserialization;
import com.groocraft.keycloakform.former.item.ClientFormer;
import com.groocraft.keycloakform.utils.TestFormersFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
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
class ClientsFormerTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS) KeycloakSession session;
    @Mock ClientFormer clientFormer;

    TestFormersFactory formersFactory = new TestFormersFactory();
    Collection<ClientDefinition> definitions;
    ClientsFormer former;

    @BeforeEach
    void setUp() throws IOException {
        URL definitionUrl = getClass().getClassLoader().getResource("realm-export.json");
        definitions = DefinitionMapping.cast(Deserialization.getRealmsFromStream(definitionUrl.openStream()).getFirst().getClients());
        formersFactory.registerMock(ClientDefinition.class, clientFormer);
        former = new ClientsFormer(formersFactory);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testFormerRespectsDryRun(boolean dryRun) {
        former.form(definitions, session, dryRun);

        verify(clientFormer, times(11)).form(any(), eq(session), eq(dryRun));
        verifyNoMoreInteractions(clientFormer);
    }

    @Test
    void testFormerIsFormingOnlyManagedClients() {
        former.form(definitions, session, false);

        ArgumentCaptor<ClientDefinition> definitionCaptor = ArgumentCaptor.forClass(ClientDefinition.class);

        verify(clientFormer, times(11)).form(definitionCaptor.capture(), eq(session), eq(false));
        verifyNoMoreInteractions(clientFormer);

        assertThat(definitionCaptor.getAllValues()).map(ClientDefinition::getName).containsExactlyInAnyOrder(
            "unmanaged",
            "${client_account}",
            "${client_account-console}",
            "${client_admin-cli}",
            "${client_broker}",
            "",
            "",
            "",
            "",
            "${client_realm-management}",
            "${client_security-admin-console}");
    }

    @Test
    void testFormerIsDeletingNoClientWhenDryRun() {

        ClientModel firstModel = mock(ClientModel.class);
        ClientModel secondModel = mock(ClientModel.class);
        when(firstModel.getName()).thenReturn("first");
        when(secondModel.getName()).thenReturn("second");

        when(session.getContext().getRealm().getClientsStream()).thenReturn(Stream.of(firstModel, secondModel));

        former.form(definitions, session, true);

        verify(session.getContext().getRealm(), never()).removeClient(any());
    }

    @Test
    void testFormerIsDeletingExcessRealmWhenNotDryrun() {
        ClientModel firstModel = mock(ClientModel.class);
        ClientModel secondModel = mock(ClientModel.class);
        when(firstModel.getId()).thenReturn("first");
        when(firstModel.getName()).thenReturn("first");
        when(secondModel.getId()).thenReturn("second");
        when(secondModel.getName()).thenReturn("second");

        ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);

        when(session.getContext().getRealm().getClientsStream()).thenReturn(Stream.of(firstModel, secondModel));
        when(session.getContext().getRealm().removeClient(idCaptor.capture())).thenReturn(true);

        former.form(definitions, session, false);

        assertThat(idCaptor.getAllValues()).containsExactlyInAnyOrder("first", "second");

    }

}