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

import com.groocraft.keycloakform.definition.AuthenticationExecutionDefinition;
import com.groocraft.keycloakform.former.CollectionFormer;
import com.groocraft.keycloakform.former.FormerContext;
import com.groocraft.keycloakform.former.FormersFactory;
import com.groocraft.keycloakform.former.SyncMode;

import java.util.Collection;

import lombok.CustomLog;

@CustomLog
public class AuthenticationExecutionsFormer implements CollectionFormer<AuthenticationExecutionDefinition> {

    final FormersFactory formersFactory;

    public AuthenticationExecutionsFormer(FormersFactory formersFactory) {
        this.formersFactory = formersFactory;
    }

    @Override
    public void form(Collection<AuthenticationExecutionDefinition> definitions, FormerContext context, SyncMode syncMode) {
        if (syncMode == SyncMode.FULL) {
            log.infof("Authentication execution are not identified in any way, hence all existing will be removed for %s and recreated",
                context.getAuthenticationFlow().getId());
            context.getRealm().getAuthenticationExecutionsStream(context.getAuthenticationFlow().getId())
                .forEach(ae -> context.getRealm().removeAuthenticatorExecution(ae));
        }

        definitions.forEach(d -> formersFactory.getFor(d).form(d, context));
    }

    @Override
    public Class<AuthenticationExecutionDefinition> getDefinitionClass() {
        return AuthenticationExecutionDefinition.class;
    }
}
