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
import com.groocraft.keycloakform.definition.RealmDefinition;
import com.groocraft.keycloakform.definition.deserialization.Deserialization;
import com.groocraft.keycloakform.former.collection.ClientsFormer;
import com.groocraft.keycloakform.utils.TestFormersFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
class RealmFormerTest {

    TestFormersFactory formersFactory = new TestFormersFactory();

    @Mock(answer = Answers.RETURNS_DEEP_STUBS) KeycloakSession session;
    @Mock RealmModel realmModel;
    @Mock ClientsFormer clientsFormer;

    RealmFormer former;
    RealmDefinition testDefinition;
    RealmDefinition masterDefinition;

    @BeforeEach
    void setUp() throws IOException {
        formersFactory.registerCollectionMock(ClientDefinition.class, clientsFormer);
        former = new RealmFormer(formersFactory);
        URL definitionUrl = getClass().getClassLoader().getResource("multi-realm-export.json");
        List<RealmDefinition> definitions = Deserialization.getRealmsFromStream(definitionUrl.openStream());
        testDefinition = definitions
            .stream().filter(rd -> rd.getRealm().equals("test")).findFirst().orElseThrow();
        masterDefinition = definitions
            .stream().filter(rd -> rd.getRealm().equals("master")).findFirst().orElseThrow();
    }


    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testSubFormersAreCalledPassingProperValues(boolean dryRun) {
        when(session.realms().getRealm(anyString())).thenReturn(realmModel);

        try (MockedConstruction<RealmManager> mrm = mockConstruction(RealmManager.class)) {
            former.form(testDefinition, session, dryRun);
        }

        verify(clientsFormer).form(any(), eq(session), eq(dryRun));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testKeycloakSessionContextIsSetAndUnset(boolean dryRun) {
        when(session.realms().getRealm(anyString())).thenReturn(realmModel);

        try (MockedConstruction<RealmManager> mrm = mockConstruction(RealmManager.class)) {
            former.form(testDefinition, session, dryRun);
            assertThat(mrm.constructed()).isEmpty();
        }

        verify(session.getContext()).setRealm(realmModel);
        verify(session.getContext()).setRealm(null);
    }

    //FIXME
    //    @Test
    //    void testUpdateCommitIsCalledWhenNotDryRun() {
    //
    //    }
    //
    //    @Test
    //    void testUpdateCommitIsNotCalledWhenDryRun() {
    //
    //    }

    @Test
    void testFormerTakesRealmIdPriorToNameWhenSearch() {
        former.form(testDefinition, session, true);

        verify(session.realms(), Mockito.times(1)).getRealm(anyString());
        verifyNoMoreInteractions(session.realms());
    }

    @Test
    void testFormerTakesRealmIdPriorToNameWhenCreate() {
        when(session.realms().getRealm(anyString())).thenReturn(null);
        ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
        try (MockedConstruction<RealmManager> mrm = mockConstruction(RealmManager.class,
            (m, c) -> when(m.createRealm(idCaptor.capture(), nameCaptor.capture())).thenReturn(realmModel))) {
            former.form(testDefinition, session, false);
        }

        assertThat(idCaptor.getAllValues()).containsExactly("3a98f829-fd9c-4366-bf08-003370120f11");
        assertThat(nameCaptor.getAllValues()).containsExactly("test");
    }

    @Test
    void testFormerDoesNotUpdateNorCreateWhenDryRun() {
        when(session.realms().getRealm(anyString())).thenReturn(realmModel);

        try (MockedConstruction<RealmManager> mrm = mockConstruction(RealmManager.class)) {
            former.form(testDefinition, session, true);
            assertThat(mrm.constructed()).isEmpty();
        }

        assertThat(mockingDetails(realmModel).getInvocations())
            .map(i -> i.getMethod().getName().substring(0, 3)).doesNotContain("set");

    }

    @Test
    void testFormerCreatesMissingRealm() {
        when(session.realms().getRealm(anyString())).thenReturn(null);

        ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
        try (MockedConstruction<RealmManager> mrm = mockConstruction(RealmManager.class,
            (m, c) -> when(m.createRealm(any(), nameCaptor.capture())).thenReturn(realmModel))) {
            former.form(testDefinition, session, false);
        }

        assertThat(nameCaptor.getAllValues()).containsExactly("test");
    }

    @Test
    void testFormerNotCreatesMissingRealmWhenNotManaged() {
        when(session.realms().getRealmByName(anyString())).thenReturn(null);

        ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
        try (MockedConstruction<RealmManager> mrm = mockConstruction(RealmManager.class,
            (m, c) -> when(m.createRealm(nameCaptor.capture())).thenReturn(realmModel))) {
            former.form(masterDefinition, session, false);
        }

        assertThat(nameCaptor.getAllValues()).isEmpty();
    }

    @Test
    void testFormerUpdatesExistingRealm() {
        when(session.realms().getRealm(anyString())).thenReturn(realmModel);
        when(realmModel.getName()).thenReturn("test2");

        try (MockedConstruction<RealmManager> mrm = mockConstruction(RealmManager.class)) {
            former.form(testDefinition, session, false);
            assertThat(mrm.constructed()).isEmpty();
        }

        verify(realmModel).setName("test");
    }

}