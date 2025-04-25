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

import com.groocraft.keycloakform.definition.AuthenticatorConfigDefinition;
import com.groocraft.keycloakform.former.FormerContext;
import com.groocraft.keycloakform.former.FormersFactory;
import com.groocraft.keycloakform.former.generic.DefaultCollectionFormer;

import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.RealmModel;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import lombok.CustomLog;

@CustomLog
public class AuthenticatorConfigsFormer extends DefaultCollectionFormer<AuthenticatorConfigDefinition> {

    public AuthenticatorConfigsFormer(FormersFactory formersFactory) {
        super(formersFactory);
    }

    @Override
    protected void deleteUndeclaredKeycloakResources(Collection<AuthenticatorConfigDefinition> definitions, FormerContext context) {
        Set<String> defined = new HashSet<>(definitions.stream().map(AuthenticatorConfigDefinition::getId).toList());

        RealmModel realm = context.getRealm();

        realm.getAuthenticatorConfigsStream()
            .filter(c -> !defined.contains(c.getId()))
            .forEach(c -> remove(c, realm));
    }

    private void remove(AuthenticatorConfigModel model, RealmModel realm) {
        log.infof("Authenticator config %s (%s) of realm %s is present but not defined, deleting it",
            model.getAlias(), model.getId(), realm.getName());
        realm.removeAuthenticatorConfig(model);
    }

    @Override
    public Class<AuthenticatorConfigDefinition> getDefinitionClass() {
        return AuthenticatorConfigDefinition.class;
    }
}
