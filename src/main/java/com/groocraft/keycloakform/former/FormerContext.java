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

import com.groocraft.keycloakform.definition.RealmDefinition;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.AuthenticationFlowModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientScopeModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;

import lombok.Getter;
import lombok.Setter;

@Getter
public class FormerContext {

    @Setter
    private ClientScopeModel clientScope;
    @Setter
    private RealmDefinition realmDefinition;
    @Setter
    private ComponentModel component;
    @Setter
    private String providerType;
    @Setter
    private AuthenticationFlowModel authenticationFlow;
    private final KeycloakSession session;

    public FormerContext(KeycloakSession session) {
        this.session = session;
    }

    public ClientModel getClient() {
        return session.getContext().getClient();
    }

    public void setClient(ClientModel client) {
        session.getContext().setClient(client);
    }

    public RealmModel getRealm() {
        return session.getContext().getRealm();
    }

    public void setRealm(RealmModel realm) {
        session.getContext().setRealm(realm);
    }

}
