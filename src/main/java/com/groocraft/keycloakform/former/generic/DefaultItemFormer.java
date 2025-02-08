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

package com.groocraft.keycloakform.former.generic;

import com.groocraft.keycloakform.definition.Definition;
import com.groocraft.keycloakform.former.ItemFormer;

import org.jboss.logging.Logger;
import org.keycloak.models.KeycloakSession;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * The ItemFormer class is an abstract implementation of the {@link ItemFormer} interface,
 * representing a template for forming Keycloak resources from a given definition. It provides
 * a basic life cycle for resource retrieval, creation, and update operations.
 * Key responsibilities include:
 * 1. Retrieving a resource from Keycloak based on its definition.
 * 2. Creating a new resource in Keycloak if it does not already exist.
 * 3. Updating an existing resource in Keycloak based on its definition.
 * 4. Logging the actions and changes made to the resource during the forming process.
 * 5. Supporting dry-run operations to simulate changes without applying them.
 *
 * @param <KeycloakT>   the type representing the Keycloak resource.
 * @param <DefinitionT> the type representing the definition of the resource state.
 * @author Majlanky
 */
public abstract class DefaultItemFormer<KeycloakT, DefinitionT extends Definition>
    implements ItemFormer<DefinitionT> {

    private final Logger log;

    public DefaultItemFormer(Logger log) {
        this.log = log;
    }

    protected abstract KeycloakT getKeycloakResource(DefinitionT definition, KeycloakSession session);

    protected abstract KeycloakT create(DefinitionT definition, KeycloakSession session);

    protected abstract void update(KeycloakT keycloakResource, DefinitionT definition);

    protected abstract void updateCommit(KeycloakT keycloakResource, KeycloakSession session);

    protected abstract Class<KeycloakT> getKeycloakResourceClass();

    protected abstract String getLogIdentifier(DefinitionT definition);

    protected void update(KeycloakT keycloakResource, DefinitionT definition, KeycloakSession session,
                          boolean dryRun, String logIdentifier) {
        ItemFormerInvocationHandler handler = new ItemFormerInvocationHandler(keycloakResource, dryRun);
        KeycloakT proxiedResource = proxyOf(getKeycloakResourceClass(), handler);
        update(proxiedResource, definition);
        String changeLog = String.join(",\n ", handler.getChangeLog());
        if (changeLog.isEmpty()) {
            log.infof("%s %s because of no changes", logIdentifier, dryRun ? "would not be updated" : " not updated");
        } else {
            if(!dryRun){
                updateCommit(keycloakResource, session);
            }
            log.infof("%s %s with the following changes:\n %s",
                logIdentifier,
                dryRun ? "would be updated" : "updated",
                changeLog);
        }
    }

    public void form(DefinitionT definition, KeycloakSession session, boolean dryRun) {
        String logIdentifier = getLogIdentifier(definition);
        if (!definition.isManaged()) {
            log.infof("%s is marked as unmanaged, skipping it", logIdentifier);
            return;
        }

        KeycloakT keycloakResource = getKeycloakResource(definition, session);

        if (keycloakResource == null) {
            if (dryRun) {
                log.infof("%s does not exist, will be mocked hence in change log it will report original values as null",
                    logIdentifier);
            } else {
                log.infof("%s does not exist, will be created right now", logIdentifier);
                keycloakResource = create(definition, session);
            }
        } else {
            log.infof("%s exits and %s", logIdentifier, dryRun ? "would be updated" : "will be updated");
        }
        update(keycloakResource, definition, session, dryRun, logIdentifier);
    }

    private <T> T proxyOf(Class<?> clazz, InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(),
            new Class<?>[] {clazz},
            handler);
    }

}
