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

package com.groocraft.keycloakform.former;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientScopeModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FormerContextTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS) KeycloakSession session;
    @Mock ClientModel client;
    @Mock ClientScopeModel clientScope;
    @Mock RealmModel realm;

    @Test
    void testContextIsProxyingKeycloakSession() {
        FormerContext context = new FormerContext(session);
        context.setClient(client);
        context.setClientScope(clientScope);
        context.setRealm(realm);

        verify(session.getContext()).setClient(client);
        verify(session.getContext()).setRealm(realm);

        assertThat(context.getSession()).isSameAs(session);
        assertThat(context.getClientScope()).isSameAs(clientScope);
    }

}