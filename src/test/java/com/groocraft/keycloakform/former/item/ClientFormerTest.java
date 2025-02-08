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

import com.groocraft.keycloakform.definition.ClientDefinition;
import com.groocraft.keycloakform.definition.DefinitionMapping;
import com.groocraft.keycloakform.definition.ProtocolMapperDefinition;
import com.groocraft.keycloakform.definition.RealmDefinition;
import com.groocraft.keycloakform.definition.deserialization.Deserialization;
import com.groocraft.keycloakform.former.collection.ProtocolMappersFormer;
import com.groocraft.keycloakform.utils.TestFormersFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.services.managers.RealmManager;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientFormerTest {

    TestFormersFactory formersFactory = new TestFormersFactory();

    @Mock(answer = Answers.RETURNS_DEEP_STUBS) KeycloakSession session;
    @Mock RealmModel realmModel;
    @Mock ClientModel clientModel;
    @Mock ProtocolMappersFormer protocolMappersFormer;

    ClientFormer former;
    ClientDefinition clientDefinition;
    ClientDefinition unmanagedClientDefinition;

    @BeforeEach
    void setUp() throws IOException {
        formersFactory.registerCollectionMock(ProtocolMapperDefinition.class, protocolMappersFormer);
        former = new ClientFormer(formersFactory);
        URL definitionUrl = getClass().getClassLoader().getResource("realm-export.json");
        List<RealmDefinition> definitions = Deserialization.getRealmsFromStream(definitionUrl.openStream());
        clientDefinition = DefinitionMapping.cast(definitions
            .stream().filter(rd -> rd.getRealm().equals("test")).findFirst().orElseThrow()
            .getClients()
            .stream().filter(c -> c.getName().equals("${client_account-console}")).findFirst().orElseThrow());
        unmanagedClientDefinition = DefinitionMapping.cast(definitions
            .stream().filter(rd -> rd.getRealm().equals("test")).findFirst().orElseThrow()
            .getClients()
            .stream().filter(c -> c.getName().equals("unmanaged")).findFirst().orElseThrow());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testSubFormersAreCalledPassingProperValues(boolean dryRun) {
        when(session.getContext().getRealm()).thenReturn(realmModel);
        when(realmModel.getClientById(any())).thenReturn(clientModel);

        former.form(clientDefinition, session, dryRun);

        verify(protocolMappersFormer).form(any(), eq(session), eq(dryRun));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testKeycloakSessionContextIsSetAndUnset(boolean dryRun) {
        when(session.getContext().getRealm()).thenReturn(realmModel);
        when(session.getContext().getRealm().getClientById(any())).thenReturn(clientModel);

        try (MockedConstruction<RealmManager> mrm = mockConstruction(RealmManager.class)) {
            former.form(clientDefinition, session, dryRun);
            assertThat(mrm.constructed()).isEmpty();
        }

        verify(session.getContext()).setClient(clientModel);
        verify(session.getContext()).setClient(null);
    }

    @Test
    void testFormerTakesClientIdPriorToNameWhenSearch() {
        former.form(clientDefinition, session, true);

        verify(session.getContext().getRealm(), Mockito.times(1)).getClientById(anyString());
        verifyNoMoreInteractions(session.getContext().getRealm());
    }

    @Test
    void testFormerTakesClientIdPriorToNameWhenCreate() {
        when(session.getContext().getRealm()).thenReturn(realmModel);
        when(realmModel.getClientById(any())).thenReturn(null);
        ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);

        when(realmModel.addClient(idCaptor.capture(), nameCaptor.capture())).thenReturn(clientModel);
        former.form(clientDefinition, session, false);

        assertThat(idCaptor.getAllValues()).containsExactly("b6c7e2c9-133c-4f75-bc93-6a1d838b4f6c");
        assertThat(nameCaptor.getAllValues()).containsExactly("${client_account-console}");
    }

    @Test
    void testFormerDoesNotUpdateNorCreateWhenDryRun() {
        when(session.getContext().getRealm()).thenReturn(realmModel);
        when(realmModel.getClientById(any())).thenReturn(clientModel);

        former.form(clientDefinition, session, true);

        assertThat(mockingDetails(clientModel).getInvocations())
            .map(i -> i.getMethod().getName().substring(0, 3)).doesNotContain("set");

    }

    @Test
    void testFormerCreatesMissingClient() {
        when(session.getContext().getRealm()).thenReturn(realmModel);
        when(realmModel.getClientById(any())).thenReturn(null);

        ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
        when(realmModel.addClient(any(), nameCaptor.capture())).thenReturn(clientModel);

        former.form(clientDefinition, session, false);

        assertThat(nameCaptor.getAllValues()).containsExactly("${client_account-console}");
    }

    @Test
    void testFormerNotCreatesMissingClientWhenNotManaged() {
        when(session.getContext().getRealm()).thenReturn(realmModel);

        former.form(unmanagedClientDefinition, session, false);

        verify(realmModel, Mockito.never()).addClient(any(), any());
    }

    @Test
    void testFormerUpdatesExistingClient() {
        when(session.getContext().getRealm()).thenReturn(realmModel);
        when(realmModel.getClientById(any())).thenReturn(clientModel);
        when(clientModel.getName()).thenReturn("oldName");

        former.form(clientDefinition, session, false);

        verify(clientModel).setName("${client_account-console}");
    }

}