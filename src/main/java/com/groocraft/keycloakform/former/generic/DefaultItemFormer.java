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
import com.groocraft.keycloakform.exception.ModelProxyException;
import com.groocraft.keycloakform.former.FormerContext;
import com.groocraft.keycloakform.former.ItemFormer;
import com.groocraft.keycloakform.former.SyncMode;

import org.jboss.logging.Logger;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;

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

    protected abstract KeycloakT getModel(DefinitionT definition, FormerContext context);

    protected abstract KeycloakT create(DefinitionT definition, FormerContext context);

    protected abstract void update(KeycloakT keycloakResource, DefinitionT definition, FormerContext context);

    protected abstract Class<KeycloakT> getKeycloakResourceClass();

    protected abstract String getLogIdentifier(DefinitionT definition);

    protected void update(KeycloakT keycloakResource, DefinitionT definition, FormerContext context, String logIdentifier) {
        StringBuilder changeLog = new StringBuilder();
        ItemFormerMethodHandler handler = new ItemFormerMethodHandler(keycloakResource,
            (attribute, original, current) -> changeLog.append(attribute)
                .append(": ")
                .append(original)
                .append(" >>> ")
                .append(current)
                .append('\n'));
        KeycloakT proxiedResource = proxyOf(getKeycloakResourceClass(), handler);
        update(proxiedResource, definition, context);
        if (changeLog.isEmpty()) {
            log.infof("%s without changes", logIdentifier);
        } else {
            log.infof("%s updated with the following changes:\n %s",
                logIdentifier,
                changeLog);
        }
    }

    @Override
    public void form(DefinitionT definition, FormerContext context) {
        //FIXME validate(definition);
        String logIdentifier = getLogIdentifier(definition);
        if (definition.getSyncMode() == SyncMode.IGNORE) {
            log.infof("%s sync mode IGNORE, skipping it", logIdentifier);
            return;
        }

        KeycloakT keycloakResource = getModel(definition, context);

        if (keycloakResource == null) {
            log.infof("%s does not exist, will be created and formed", logIdentifier);
            keycloakResource = create(definition, context);
        } else {
            log.infof("%s exits and will be formed", logIdentifier);
        }
        update(keycloakResource, definition, context, logIdentifier);
    }

    @SuppressWarnings("unchecked")
    private <T> T proxyOf(Class<?> clazz, MethodHandler handler) {
        ProxyFactory factory = new ProxyFactory();
        if (clazz.isInterface()) {
            factory.setInterfaces(new Class<?>[] {clazz});
        } else {
            factory.setSuperclass(clazz);
        }
        try {
            return (T) factory.create(new Class<?>[0], new Object[0], handler);
        } catch (Exception e) {
            throw new ModelProxyException(clazz, e);
        }
    }

}
