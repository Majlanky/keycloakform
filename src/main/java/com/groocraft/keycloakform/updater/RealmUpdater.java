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

import com.groocraft.keycloakform.definition.RealmDefinition;

import org.keycloak.common.enums.SslRequired;
import org.keycloak.models.AuthenticationFlowModel;
import org.keycloak.models.PasswordPolicy;
import org.keycloak.models.RealmModel;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class is responsible for updating a {@link RealmModel} instance with the properties of a {@link RealmDefinition} instance.
 * It ensures that the fields of the {@link RealmModel} are set or modified based on the corresponding values in the {@link RealmDefinition}.
 * The update process skips any properties in the {@link RealmDefinition} that are null, preserving the original
 * values in the {@link RealmModel} for those fields.
 *
 * @author Majlanky
 */
public class RealmUpdater implements Updater<RealmModel, RealmDefinition> {

    @Override
    public void update(RealmModel realmModel, RealmDefinition definition) {
        if (definition == null) {
            return;
        }

        if (definition.getRealm() != null) {
            realmModel.setName(definition.getRealm());
        }
        if (definition.getDisplayName() != null) {
            realmModel.setDisplayName(definition.getDisplayName());
        }
        if (definition.getDisplayNameHtml() != null) {
            realmModel.setDisplayNameHtml(definition.getDisplayNameHtml());
        }
        if (definition.isEnabled() != null) {
            realmModel.setEnabled(definition.isEnabled());
        }
        if (definition.getSslRequired() != null) {
            realmModel.setSslRequired(mapSslRequired(definition.getSslRequired()));
        }
        if (definition.isRegistrationAllowed() != null) {
            realmModel.setRegistrationAllowed(definition.isRegistrationAllowed());
        }
        if (definition.isRegistrationEmailAsUsername() != null) {
            realmModel.setRegistrationEmailAsUsername(definition.isRegistrationEmailAsUsername());
        }
        if (definition.isRememberMe() != null) {
            realmModel.setRememberMe(definition.isRememberMe());
        }
        if (definition.isEditUsernameAllowed() != null) {
            realmModel.setEditUsernameAllowed(definition.isEditUsernameAllowed());
        }
        if (definition.isUserManagedAccessAllowed() != null) {
            realmModel.setUserManagedAccessAllowed(definition.isUserManagedAccessAllowed());
        }
        if (definition.isOrganizationsEnabled() != null) {
            realmModel.setOrganizationsEnabled(definition.isOrganizationsEnabled());
        }
        if (definition.isBruteForceProtected() != null) {
            realmModel.setBruteForceProtected(definition.isBruteForceProtected());
        }
        if (definition.isPermanentLockout() != null) {
            realmModel.setPermanentLockout(definition.isPermanentLockout());
        }
        if (definition.getMaxTemporaryLockouts() != null) {
            realmModel.setMaxTemporaryLockouts(definition.getMaxTemporaryLockouts());
        }
        if (definition.getMaxFailureWaitSeconds() != null) {
            realmModel.setMaxFailureWaitSeconds(definition.getMaxFailureWaitSeconds());
        }
        if (definition.getWaitIncrementSeconds() != null) {
            realmModel.setWaitIncrementSeconds(definition.getWaitIncrementSeconds());
        }
        if (definition.getMinimumQuickLoginWaitSeconds() != null) {
            realmModel.setMinimumQuickLoginWaitSeconds(definition.getMinimumQuickLoginWaitSeconds());
        }
        if (definition.getQuickLoginCheckMilliSeconds() != null) {
            realmModel.setQuickLoginCheckMilliSeconds(definition.getQuickLoginCheckMilliSeconds());
        }
        if (definition.getMaxDeltaTimeSeconds() != null) {
            realmModel.setMaxDeltaTimeSeconds(definition.getMaxDeltaTimeSeconds());
        }
        if (definition.getFailureFactor() != null) {
            realmModel.setFailureFactor(definition.getFailureFactor());
        }
        if (definition.isVerifyEmail() != null) {
            realmModel.setVerifyEmail(definition.isVerifyEmail());
        }
        if (definition.isLoginWithEmailAllowed() != null) {
            realmModel.setLoginWithEmailAllowed(definition.isLoginWithEmailAllowed());
        }
        if (definition.isDuplicateEmailsAllowed() != null) {
            realmModel.setDuplicateEmailsAllowed(definition.isDuplicateEmailsAllowed());
        }
        if (definition.isResetPasswordAllowed() != null) {
            realmModel.setResetPasswordAllowed(definition.isResetPasswordAllowed());
        }
        if (definition.getDefaultSignatureAlgorithm() != null) {
            realmModel.setDefaultSignatureAlgorithm(definition.getDefaultSignatureAlgorithm());
        }
        if (definition.getRevokeRefreshToken() != null) {
            realmModel.setRevokeRefreshToken(definition.getRevokeRefreshToken());
        }
        if (definition.getRefreshTokenMaxReuse() != null) {
            realmModel.setRefreshTokenMaxReuse(definition.getRefreshTokenMaxReuse());
        }
        if (definition.getSsoSessionIdleTimeout() != null) {
            realmModel.setSsoSessionIdleTimeout(definition.getSsoSessionIdleTimeout());
        }
        if (definition.getSsoSessionMaxLifespan() != null) {
            realmModel.setSsoSessionMaxLifespan(definition.getSsoSessionMaxLifespan());
        }
        if (definition.getSsoSessionIdleTimeoutRememberMe() != null) {
            realmModel.setSsoSessionIdleTimeoutRememberMe(definition.getSsoSessionIdleTimeoutRememberMe());
        }
        if (definition.getSsoSessionMaxLifespanRememberMe() != null) {
            realmModel.setSsoSessionMaxLifespanRememberMe(definition.getSsoSessionMaxLifespanRememberMe());
        }
        if (definition.getOfflineSessionIdleTimeout() != null) {
            realmModel.setOfflineSessionIdleTimeout(definition.getOfflineSessionIdleTimeout());
        }
        if (definition.getOfflineSessionMaxLifespanEnabled() != null) {
            realmModel.setOfflineSessionMaxLifespanEnabled(definition.getOfflineSessionMaxLifespanEnabled());
        }
        if (definition.getOfflineSessionMaxLifespan() != null) {
            realmModel.setOfflineSessionMaxLifespan(definition.getOfflineSessionMaxLifespan());
        }
        if (definition.getClientSessionIdleTimeout() != null) {
            realmModel.setClientSessionIdleTimeout(definition.getClientSessionIdleTimeout());
        }
        if (definition.getClientSessionMaxLifespan() != null) {
            realmModel.setClientSessionMaxLifespan(definition.getClientSessionMaxLifespan());
        }
        if (definition.getClientOfflineSessionIdleTimeout() != null) {
            realmModel.setClientOfflineSessionIdleTimeout(definition.getClientOfflineSessionIdleTimeout());
        }
        if (definition.getClientOfflineSessionMaxLifespan() != null) {
            realmModel.setClientOfflineSessionMaxLifespan(definition.getClientOfflineSessionMaxLifespan());
        }
        if (definition.getAccessTokenLifespan() != null) {
            realmModel.setAccessTokenLifespan(definition.getAccessTokenLifespan());
        }
        if (definition.getAccessTokenLifespanForImplicitFlow() != null) {
            realmModel.setAccessTokenLifespanForImplicitFlow(definition.getAccessTokenLifespanForImplicitFlow());
        }
        if (definition.getAccessCodeLifespan() != null) {
            realmModel.setAccessCodeLifespan(definition.getAccessCodeLifespan());
        }
        if (definition.getAccessCodeLifespanUserAction() != null) {
            realmModel.setAccessCodeLifespanUserAction(definition.getAccessCodeLifespanUserAction());
        }
        if (definition.getAccessCodeLifespanLogin() != null) {
            realmModel.setAccessCodeLifespanLogin(definition.getAccessCodeLifespanLogin());
        }
        if (definition.getActionTokenGeneratedByAdminLifespan() != null) {
            realmModel.setActionTokenGeneratedByAdminLifespan(definition.getActionTokenGeneratedByAdminLifespan());
        }
        if (definition.getActionTokenGeneratedByUserLifespan() != null) {
            realmModel.setActionTokenGeneratedByUserLifespan(definition.getActionTokenGeneratedByUserLifespan());
        }
        if (definition.getPasswordPolicy() != null) {
            realmModel.setPasswordPolicy(mapPasswordPolicy(definition.getPasswordPolicy()));
        }
        if (realmModel.getBrowserSecurityHeaders() != null) {
            Map<String, String> securityHeaders = definition.getBrowserSecurityHeaders();
            if (securityHeaders != null) {
                realmModel.getBrowserSecurityHeaders().clear();
                realmModel.getBrowserSecurityHeaders().putAll(securityHeaders);
            }
        } else {
            Map<String, String> securityHeaders = definition.getBrowserSecurityHeaders();
            if (securityHeaders != null) {
                realmModel.setBrowserSecurityHeaders(new LinkedHashMap<>(securityHeaders));
            }
        }
        if (definition.getLoginTheme() != null) {
            realmModel.setLoginTheme(definition.getLoginTheme());
        }
        if (definition.getAccountTheme() != null) {
            realmModel.setAccountTheme(definition.getAccountTheme());
        }
        if (definition.getAdminTheme() != null) {
            realmModel.setAdminTheme(definition.getAdminTheme());
        }
        if (definition.getEmailTheme() != null) {
            realmModel.setEmailTheme(definition.getEmailTheme());
        }
        if (definition.getNotBefore() != null) {
            realmModel.setNotBefore(definition.getNotBefore());
        }
        if (definition.isEventsEnabled() != null) {
            realmModel.setEventsEnabled(definition.isEventsEnabled());
        }
        if (definition.getEventsExpiration() != null) {
            realmModel.setEventsExpiration(definition.getEventsExpiration());
        }
        List<String> eventsListeners = definition.getEventsListeners();
        if (eventsListeners != null) {
            realmModel.setEventsListeners(new LinkedHashSet<>(eventsListeners));
        }
        List<String> enabledEventTypes = definition.getEnabledEventTypes();
        if (enabledEventTypes != null) {
            realmModel.setEnabledEventTypes(new LinkedHashSet<>(enabledEventTypes));
        }
        if (definition.isAdminEventsEnabled() != null) {
            realmModel.setAdminEventsEnabled(definition.isAdminEventsEnabled());
        }
        if (definition.isAdminEventsDetailsEnabled() != null) {
            realmModel.setAdminEventsDetailsEnabled(definition.isAdminEventsDetailsEnabled());
        }
        if (definition.isInternationalizationEnabled() != null) {
            realmModel.setInternationalizationEnabled(definition.isInternationalizationEnabled());
        }
        Set<String> set = definition.getSupportedLocales();
        if (set != null) {
            realmModel.setSupportedLocales(new LinkedHashSet<>(set));
        }
        if (definition.getDefaultLocale() != null) {
            realmModel.setDefaultLocale(definition.getDefaultLocale());
        }
        if (realmModel.getAttributes() != null) {
            Map<String, String> definitionAttributes = definition.getAttributes();
            if (definitionAttributes != null) {
                realmModel.getAttributes().clear();
                realmModel.getAttributes().putAll(definitionAttributes);
            }
        }

        if (definition.getBrowserFlow() != null) {
            realmModel.setBrowserFlow(mapAuthenticationFlowModel(definition.getBrowserFlow(), definition));
        }
        if (definition.getRegistrationFlow() != null) {
            realmModel.setRegistrationFlow(mapAuthenticationFlowModel(definition.getRegistrationFlow(), definition));
        }
        if (definition.getDirectGrantFlow() != null) {
            realmModel.setDirectGrantFlow(mapAuthenticationFlowModel(definition.getDirectGrantFlow(), definition));
        }
        if (definition.getResetCredentialsFlow() != null) {
            realmModel.setResetCredentialsFlow(mapAuthenticationFlowModel(definition.getResetCredentialsFlow(), definition));
        }
        if (definition.getClientAuthenticationFlow() != null) {
            realmModel.setClientAuthenticationFlow(mapAuthenticationFlowModel(definition.getClientAuthenticationFlow(), definition));
        }
        if (definition.getDockerAuthenticationFlow() != null) {
            realmModel.setDockerAuthenticationFlow(mapAuthenticationFlowModel(definition.getDockerAuthenticationFlow(), definition));
        }
        if (definition.getFirstBrokerLoginFlow() != null) {
            realmModel.setFirstBrokerLoginFlow(mapAuthenticationFlowModel(definition.getFirstBrokerLoginFlow(), definition));
        }
    }

    public static AuthenticationFlowModel mapAuthenticationFlowModel(String alias, RealmDefinition definition) {
        if (alias == null || alias.isBlank()) {
            return null;
        }
        AuthenticationFlowModel model = new AuthenticationFlowModel();
        definition.getAuthenticationFlows().stream().filter(flow -> flow.getAlias().equals(alias)).findFirst()
            .ifPresent(flow -> model.setId(flow.getId()));
        model.setAlias(alias);
        return model;
    }

    public static SslRequired mapSslRequired(String sslRequired) {
        if (sslRequired == null) {
            return null;
        }

        SslRequired sslRequired1;

        switch (sslRequired) {
            case "all":
                sslRequired1 = SslRequired.ALL;
                break;
            case "external":
                sslRequired1 = SslRequired.EXTERNAL;
                break;
            case "none":
                sslRequired1 = SslRequired.NONE;
                break;
            default:
                throw new IllegalArgumentException("Unexpected enum constant: " + sslRequired);
        }

        return sslRequired1;
    }

    public static PasswordPolicy mapPasswordPolicy(String passwordPolicy) {
        if (passwordPolicy == null || passwordPolicy.isBlank()) {
            return PasswordPolicy.empty();
        }
        return PasswordPolicy.empty(); //FIXME to parse
    }
}
