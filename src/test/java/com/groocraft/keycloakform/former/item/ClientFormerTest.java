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
import com.groocraft.keycloakform.definition.RoleDefinition;
import com.groocraft.keycloakform.definition.deserialization.Deserialization;
import com.groocraft.keycloakform.former.FormerContext;
import com.groocraft.keycloakform.former.SyncMode;
import com.groocraft.keycloakform.former.collection.ProtocolMappersFormer;
import com.groocraft.keycloakform.former.collection.RolesFormer;
import com.groocraft.keycloakform.utils.TestFormersFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.common.Profile;
import org.keycloak.models.ClientModel;
import org.keycloak.models.RealmModel;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientFormerTest {

    TestFormersFactory formersFactory = new TestFormersFactory();

    @Mock(answer = Answers.RETURNS_DEEP_STUBS) FormerContext context;
    @Mock RealmModel realmModel;
    @Mock ClientModel clientModel;
    @Mock ProtocolMappersFormer protocolMappersFormer;
    @Mock RolesFormer rolesFormer;

    ClientFormer former;
    RealmDefinition realmDefinition;
    ClientDefinition clientDefinition;
    ClientDefinition unmanagedClientDefinition;

    @BeforeEach
    void setUp() throws IOException {
        former = new ClientFormer(formersFactory);
        formersFactory.registerCollectionMock(ProtocolMapperDefinition.class, protocolMappersFormer);
        formersFactory.registerCollectionMock(RoleDefinition.class, rolesFormer);
        URL definitionUrl = getClass().getClassLoader().getResource("realms.json");
        List<RealmDefinition> definitions = Deserialization.getRealmsFromStream(definitionUrl.openStream());
        realmDefinition = definitions.stream().filter(rd -> rd.getRealm().equals("test")).findFirst().orElseThrow();
        clientDefinition = DefinitionMapping.cast(realmDefinition
            .getClients()
            .stream().filter(c -> c.getName().equals("${client_account-console}")).findFirst().orElseThrow());
        unmanagedClientDefinition = DefinitionMapping.cast(realmDefinition
            .getClients()
            .stream().filter(c -> c.getName().equals("Test")).findFirst().orElseThrow());

        when(context.getRealm()).thenReturn(realmModel);
        when(context.getRealmDefinition()).thenReturn(realmDefinition);
    }

    @Test
    void testSubFormersAreCalledPassingProperValues() {
        when(realmModel.getClientByClientId(any())).thenReturn(clientModel);

        try (MockedStatic<Profile> profileMock = mockStatic(Profile.class)) {
            profileMock.when(() -> Profile.isFeatureEnabled(any())).thenReturn(false);
            former.form(clientDefinition, context);
        }

        verify(protocolMappersFormer).form(any(), eq(context), eq(SyncMode.FULL));
        verify(rolesFormer).form(any(), eq(context), eq(SyncMode.FULL));
    }

    @Test
    void testKeycloakSessionContextIsSetAndUnset() {
        when(context.getRealm().getClientByClientId(any())).thenReturn(clientModel);

        try (MockedStatic<Profile> profileMock = mockStatic(Profile.class)) {
            profileMock.when(() -> Profile.isFeatureEnabled(any())).thenReturn(false);
            former.form(clientDefinition, context);
        }

        verify(context).setClient(any(ClientModel.class));
        verify(context).setClient(null);
    }

    @Test
    void testFormerTakesCreatesWhenMissingAndNotDryRun() {
        when(realmModel.getClientByClientId(any())).thenReturn(null);
        ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);

        when(realmModel.addClient(any(), idCaptor.capture())).thenReturn(clientModel);
        try (MockedStatic<Profile> profileMock = mockStatic(Profile.class)) {
            profileMock.when(() -> Profile.isFeatureEnabled(any())).thenReturn(false);
            former.form(clientDefinition, context);
        }

        assertThat(idCaptor.getAllValues()).containsExactly("account-console");
    }

    @Test
    void testFormerNotCreatesMissingClientWhenNotManaged() {
        former.form(unmanagedClientDefinition, context);

        verify(realmModel, Mockito.never()).addClient(any(), any());
    }

    @Test
    void testFormerUpdatesExistingClient() {
        when(realmModel.getClientByClientId(any())).thenReturn(clientModel);
        when(clientModel.getName()).thenReturn("oldName");

        try (MockedStatic<Profile> profileMock = mockStatic(Profile.class)) {
            profileMock.when(() -> Profile.isFeatureEnabled(any())).thenReturn(false);
            former.form(clientDefinition, context);
        }

        verify(clientModel).setName("${client_account-console}");
    }

}