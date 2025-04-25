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

import com.groocraft.keycloakform.definition.AuthenticationFlowDefinition;
import com.groocraft.keycloakform.definition.AuthenticatorConfigDefinition;
import com.groocraft.keycloakform.definition.ClientDefinition;
import com.groocraft.keycloakform.definition.ClientScopeDefinition;
import com.groocraft.keycloakform.definition.ComponentDefinition;
import com.groocraft.keycloakform.definition.GroupDefinition;
import com.groocraft.keycloakform.definition.IdentityProviderDefinition;
import com.groocraft.keycloakform.definition.IdentityProviderMapperDefinition;
import com.groocraft.keycloakform.definition.RealmDefinition;
import com.groocraft.keycloakform.definition.RequiredActionDefinition;
import com.groocraft.keycloakform.definition.RoleDefinition;
import com.groocraft.keycloakform.definition.RolesDefinition;
import com.groocraft.keycloakform.definition.ScopeDefinitionHelper;
import com.groocraft.keycloakform.definition.deserialization.Deserialization;
import com.groocraft.keycloakform.former.FormerContext;
import com.groocraft.keycloakform.former.SyncMode;
import com.groocraft.keycloakform.former.collection.AuthenticationFlowsFormer;
import com.groocraft.keycloakform.former.collection.AuthenticatorConfigsFormer;
import com.groocraft.keycloakform.former.collection.ClientScopesFormer;
import com.groocraft.keycloakform.former.collection.ClientsFormer;
import com.groocraft.keycloakform.former.collection.ComponentsFormer;
import com.groocraft.keycloakform.former.collection.GroupsFormer;
import com.groocraft.keycloakform.former.collection.IdentityProviderMappersFormer;
import com.groocraft.keycloakform.former.collection.IdentityProvidersFormer;
import com.groocraft.keycloakform.former.collection.RequiredActionsFormer;
import com.groocraft.keycloakform.former.collection.RolesFormer;
import com.groocraft.keycloakform.utils.TestFormersFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.models.CibaConfig;
import org.keycloak.models.ClientModel;
import org.keycloak.models.OAuth2DeviceConfig;
import org.keycloak.models.ParConfig;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RealmFormerTest {

    TestFormersFactory formersFactory = new TestFormersFactory();

    @Mock(answer = Answers.RETURNS_DEEP_STUBS) FormerContext context;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS) RealmModel realmModel;
    @Mock RealmModel adminRealmModel;
    @Mock ClientModel clientModel;
    @Mock ClientsFormer clientsFormer;
    @Mock ClientScopesFormer clientScopesFormer;
    @Mock RolesFormer rolesFormer;
    @Mock ComponentsFormer componentsFormer;
    @Mock RoleCompositesFormer roleCompositesFormer;
    @Mock AuthenticatorConfigsFormer authenticatorConfigsFormer;
    @Mock AuthenticationFlowsFormer authenticationFlowsFormer;
    @Mock RequiredActionsFormer requiredActionsFormer;
    @Mock ScopeMappingsFormer scopeMappingsFormer;
    @Mock IdentityProvidersFormer identityProvidersFormer;
    @Mock GroupsFormer groupsFormer;
    @Mock IdentityProviderMappersFormer identityProviderMappersFormer;
    @Mock OAuth2DeviceConfig oAuth2DeviceConfig;
    @Mock CibaConfig cibaConfig;
    @Mock ParConfig parConfig;

    RealmFormer former;
    RealmDefinition testDefinition;
    RealmDefinition masterDefinition;

    @BeforeEach
    void setUp() throws IOException {
        formersFactory.registerCollectionMock(ClientDefinition.class, clientsFormer);
        formersFactory.registerCollectionMock(ClientScopeDefinition.class, clientScopesFormer);
        formersFactory.registerCollectionMock(RoleDefinition.class, rolesFormer);
        formersFactory.registerMock(RolesDefinition.class, roleCompositesFormer);
        formersFactory.registerCollectionMock(AuthenticatorConfigDefinition.class, authenticatorConfigsFormer);
        formersFactory.registerCollectionMock(AuthenticationFlowDefinition.class, authenticationFlowsFormer);
        formersFactory.registerCollectionMock(RequiredActionDefinition.class, requiredActionsFormer);
        formersFactory.registerCollectionMock(ComponentDefinition.class, componentsFormer);
        formersFactory.registerMock(ScopeDefinitionHelper.class, scopeMappingsFormer);
        formersFactory.registerCollectionMock(IdentityProviderDefinition.class, identityProvidersFormer);
        formersFactory.registerCollectionMock(IdentityProviderMapperDefinition.class, identityProviderMappersFormer);
        formersFactory.registerCollectionMock(GroupDefinition.class, groupsFormer);
        former = new RealmFormer(formersFactory);
        URL definitionUrl = getClass().getClassLoader().getResource("realms.json");
        List<RealmDefinition> definitions = Deserialization.getRealmsFromStream(definitionUrl.openStream());
        testDefinition = definitions
            .stream().filter(rd -> rd.getRealm().equals("test")).findFirst().orElseThrow();
        masterDefinition = definitions
            .stream().filter(rd -> rd.getRealm().equals("master")).findFirst().orElseThrow();
    }

    @Test
    void testSubFormersAreCalledPassingProperValues() {
        when(context.getSession().realms().getRealmByName(anyString())).thenReturn(realmModel);
        when(realmModel.getDefaultRole()).thenReturn(null);
        when(realmModel.getName()).thenReturn("test");
        when(realmModel.getOAuth2DeviceConfig()).thenReturn(oAuth2DeviceConfig);
        when(realmModel.getCibaPolicy()).thenReturn(cibaConfig);
        when(realmModel.getParPolicy()).thenReturn(parConfig);
        when(context.getRealm().getClientScopesStream()).thenAnswer(i -> Stream.of());

        try (MockedConstruction<RealmManager> mrm = mockConstruction(RealmManager.class)) {
            former.form(testDefinition, context);
        }

        verify(clientsFormer).form(any(), eq(context), eq(SyncMode.FULL));
    }

    @Test
    void testKeycloakSessionContextIsSetAndUnset() {
        when(context.getSession().realms().getRealmByName(anyString())).thenReturn(realmModel);
        when(realmModel.getDefaultRole()).thenReturn(null);
        when(realmModel.getName()).thenReturn("test");
        when(realmModel.getOAuth2DeviceConfig()).thenReturn(oAuth2DeviceConfig);
        when(realmModel.getCibaPolicy()).thenReturn(cibaConfig);
        when(realmModel.getParPolicy()).thenReturn(parConfig);
        when(context.getRealm().getClientScopesStream()).thenAnswer(i -> Stream.of());

        try (MockedConstruction<RealmManager> mrm = mockConstruction(RealmManager.class)) {
            former.form(testDefinition, context);
            assertThat(mrm.constructed()).isEmpty();
        }

        verify(context).setRealm(any(RealmModel.class));
        verify(context).setRealmDefinition(testDefinition);
        verify(context).setRealm(null);
        verify(context).setRealmDefinition(null);
    }

    @Test
    void testFormerCreatesMissingRealm() {
        when(context.getSession().realms().getRealmByName("test")).thenReturn(null);
        ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
        when(context.getSession().realms().createRealm(idCaptor.capture(), nameCaptor.capture())).thenReturn(realmModel);
        when(realmModel.getDefaultRole()).thenReturn(null);
        when(realmModel.getName()).thenReturn("test");
        when(context.getSession().realms().getRealmByName("master")).thenReturn(adminRealmModel);
        when(realmModel.getOAuth2DeviceConfig()).thenReturn(oAuth2DeviceConfig);
        when(realmModel.getCibaPolicy()).thenReturn(cibaConfig);
        when(realmModel.getParPolicy()).thenReturn(parConfig);
        when(context.getRealm().getClientScopesStream()).thenAnswer(i -> Stream.of());

        former.form(testDefinition, context);

        assertThat(idCaptor.getAllValues()).containsExactly("983dcadf-da2e-45eb-9fe5-1e53475bcbbe");
        assertThat(nameCaptor.getAllValues()).containsExactly("test");
    }

    @Test
    void testFormerNotCreatesMissingRealmWhenSyncModeIgnore() {
        when(context.getSession().realms().getRealmByName("master")).thenReturn(null);
        when(context.getSession().realms().createRealm(any(), any())).thenReturn(realmModel);
        when(realmModel.getName()).thenReturn("master");

        ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);

        former.form(masterDefinition, context);

        assertThat(nameCaptor.getAllValues()).isEmpty();
    }

    @Test
    void testFormerUpdatesExistingRealm() {
        when(context.getSession().realms().getRealmByName(anyString())).thenReturn(realmModel);
        when(realmModel.getDefaultRole()).thenReturn(null);
        when(realmModel.getName()).thenReturn("test");
        when(realmModel.getOAuth2DeviceConfig()).thenReturn(oAuth2DeviceConfig);
        when(realmModel.getCibaPolicy()).thenReturn(cibaConfig);
        when(realmModel.getParPolicy()).thenReturn(parConfig);
        when(context.getRealm().getClientScopesStream()).thenAnswer(i -> Stream.of());

        try (MockedConstruction<RealmManager> mrm = mockConstruction(RealmManager.class)) {
            former.form(testDefinition, context);
            assertThat(mrm.constructed()).isEmpty();
        }

        verify(realmModel).setAccessTokenLifespan(300);
    }

}