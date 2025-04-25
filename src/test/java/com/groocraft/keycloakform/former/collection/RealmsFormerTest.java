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

import com.groocraft.keycloakform.definition.RealmDefinition;
import com.groocraft.keycloakform.definition.deserialization.Deserialization;
import com.groocraft.keycloakform.former.FormerContext;
import com.groocraft.keycloakform.former.SyncMode;
import com.groocraft.keycloakform.former.item.RealmFormer;
import com.groocraft.keycloakform.utils.TestFormersFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.models.RealmModel;
import org.keycloak.services.managers.RealmManager;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RealmsFormerTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS) FormerContext context;
    @Mock RealmFormer realmFormer;
    @Mock RealmModel realmModel;

    TestFormersFactory formersFactory = new TestFormersFactory();
    List<RealmDefinition> definitions;
    RealmsFormer former;

    @BeforeEach
    void setUp() throws IOException {
        URL definitionUrl = getClass().getClassLoader().getResource("realms.json");
        definitions = Deserialization.getRealmsFromStream(definitionUrl.openStream());
        formersFactory.registerMock(RealmDefinition.class, realmFormer);
        former = new RealmsFormer(formersFactory);
    }

    @Test
    void testFormerIsFormingOnlyManagedRealms() {
        former.form(definitions, context, SyncMode.FULL);

        ArgumentCaptor<RealmDefinition> definitionCaptor = ArgumentCaptor.forClass(RealmDefinition.class);

        verify(realmFormer, times(2)).form(definitionCaptor.capture(), eq(context));
        verifyNoMoreInteractions(realmFormer);

        assertThat(definitionCaptor.getAllValues()).map(RealmDefinition::getRealm).containsExactlyInAnyOrder("test", "master");
    }

    @Test
    void testFormerIsDeletingNoRealmWhenSyncModeIgnore() {

        RealmModel masterModel = mock(RealmModel.class);
        RealmModel test2Model = mock(RealmModel.class);

        when(context.getSession().realms().getRealmsStream()).thenReturn(Stream.of(masterModel, test2Model));

        ArgumentCaptor<RealmModel> modelCaptor = ArgumentCaptor.forClass(RealmModel.class);
        try (MockedConstruction<RealmManager> mrm = mockConstruction(RealmManager.class,
            (mock, context) -> when(mock.removeRealm(modelCaptor.capture())).thenReturn(true))) {
            former.form(definitions, context, SyncMode.IGNORE);
        }

        assertThat(modelCaptor.getAllValues()).isEmpty();

    }

    @Test
    void testFormerIsDeletingExcessRealmWhenSyncModeFull() {

        RealmModel masterModel = mock(RealmModel.class);
        RealmModel test2Model = mock(RealmModel.class);

        when(context.getSession().realms().getRealmsStream()).thenReturn(Stream.of(masterModel, test2Model));

        ArgumentCaptor<RealmModel> modelCaptor = ArgumentCaptor.forClass(RealmModel.class);
        try (MockedConstruction<RealmManager> mrm = mockConstruction(RealmManager.class,
            (mock, context) -> when(mock.removeRealm(modelCaptor.capture())).thenReturn(true))) {
            former.form(definitions, context, SyncMode.FULL);
        }

        assertThat(modelCaptor.getValue()).isEqualTo(test2Model);

    }

}