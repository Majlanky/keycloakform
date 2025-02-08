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

package com.groocraft.keycloakform.updater;

import com.groocraft.keycloakform.definition.ClientDefinition;

import org.keycloak.models.ClientModel;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class ClientUpdater implements Updater<ClientModel, ClientDefinition> {

    public void update(ClientModel clientModel, ClientDefinition definition) {
        if (definition == null) {
            return;
        }

        if (definition.getClientId() != null) {
            clientModel.setClientId(definition.getClientId());
        }
        if (definition.getName() != null) {
            clientModel.setName(definition.getName());
        }
        if (definition.getDescription() != null) {
            clientModel.setDescription(definition.getDescription());
        }
        if (definition.getType() != null) {
            clientModel.setType(definition.getType());
        }
        if (definition.isEnabled() != null) {
            clientModel.setEnabled(definition.isEnabled());
        }
        if (definition.isAlwaysDisplayInConsole() != null) {
            clientModel.setAlwaysDisplayInConsole(definition.isAlwaysDisplayInConsole());
        }
        if (definition.isSurrogateAuthRequired() != null) {
            clientModel.setSurrogateAuthRequired(definition.isSurrogateAuthRequired());
        }
        if (clientModel.getWebOrigins() != null) {
            List<String> webOrigins = definition.getWebOrigins();
            if (webOrigins != null) {
                clientModel.getWebOrigins().clear();
                clientModel.getWebOrigins().addAll(webOrigins);
            }
        } else {
            List<String> webOrigins = definition.getWebOrigins();
            if (webOrigins != null) {
                clientModel.setWebOrigins(new LinkedHashSet<>(webOrigins));
            }
        }
        if (clientModel.getRedirectUris() != null) {
            List<String> redirectUris = definition.getRedirectUris();
            if (redirectUris != null) {
                clientModel.getRedirectUris().clear();
                clientModel.getRedirectUris().addAll(redirectUris);
            }
        } else {
            List<String> redirectUris = definition.getRedirectUris();
            if (redirectUris != null) {
                clientModel.setRedirectUris(new LinkedHashSet<>(redirectUris));
            }
        }
        if (definition.getRootUrl() != null) {
            clientModel.setRootUrl(definition.getRootUrl());
        }
        if (definition.getBaseUrl() != null) {
            clientModel.setBaseUrl(definition.getBaseUrl());
        }
        if (definition.isBearerOnly() != null) {
            clientModel.setBearerOnly(definition.isBearerOnly());
        }
        if (definition.getNodeReRegistrationTimeout() != null) {
            clientModel.setNodeReRegistrationTimeout(definition.getNodeReRegistrationTimeout());
        }
        if (definition.getClientAuthenticatorType() != null) {
            clientModel.setClientAuthenticatorType(definition.getClientAuthenticatorType());
        }
        if (definition.getSecret() != null) {
            clientModel.setSecret(definition.getSecret());
        }
        if (definition.getProtocol() != null) {
            clientModel.setProtocol(definition.getProtocol());
        }
        if (definition.isFrontchannelLogout() != null) {
            clientModel.setFrontchannelLogout(definition.isFrontchannelLogout());
        }
        if (definition.isFullScopeAllowed() != null) {
            clientModel.setFullScopeAllowed(definition.isFullScopeAllowed());
        }
        if (definition.isPublicClient() != null) {
            clientModel.setPublicClient(definition.isPublicClient());
        }
        if (definition.isConsentRequired() != null) {
            clientModel.setConsentRequired(definition.isConsentRequired());
        }
        if (definition.isStandardFlowEnabled() != null) {
            clientModel.setStandardFlowEnabled(definition.isStandardFlowEnabled());
        }
        if (definition.isImplicitFlowEnabled() != null) {
            clientModel.setImplicitFlowEnabled(definition.isImplicitFlowEnabled());
        }
        if (definition.isDirectAccessGrantsEnabled() != null) {
            clientModel.setDirectAccessGrantsEnabled(definition.isDirectAccessGrantsEnabled());
        }
        if (definition.isServiceAccountsEnabled() != null) {
            clientModel.setServiceAccountsEnabled(definition.isServiceAccountsEnabled());
        }
        if (definition.getNotBefore() != null) {
            clientModel.setNotBefore(definition.getNotBefore());
        }
        if (clientModel.getAttributes() != null) {
            Map<String, String> attributes = definition.getAttributes();
            if (attributes != null) {
                clientModel.getAttributes().clear();
                clientModel.getAttributes().putAll(attributes);
            }
        }
        if (clientModel.getAuthenticationFlowBindingOverrides() != null) {

            Map<String, String> overrides = definition.getAuthenticationFlowBindingOverrides();
            if (overrides != null) {
                clientModel.getAuthenticationFlowBindingOverrides().clear();
                clientModel.getAuthenticationFlowBindingOverrides().putAll(overrides);
            }
        }
        if (clientModel.getRegisteredNodes() != null) {
            Map<String, Integer> registeredNodes = definition.getRegisteredNodes();
            if (registeredNodes != null) {
                clientModel.getRegisteredNodes().clear();
                clientModel.getRegisteredNodes().putAll(registeredNodes);
            }
        }
    }
}
