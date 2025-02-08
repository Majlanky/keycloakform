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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.CustomLog;
import lombok.Getter;

/**
 * A dynamic proxy invocation handler designed to monitor and optionally apply changes to a Keycloak resource.
 * The handler tracks modifications to resource properties and maintains a change log of detected updates.
 * Changes are applied or logged based on the specified dryRun parameter.
 *
 * @author Majlanky
 */
@CustomLog
@AllArgsConstructor
public class ItemFormerInvocationHandler implements InvocationHandler {

    private final Object keyloakResource;
    private final boolean dryRun;

    @Getter
    private final List<String> changeLog = new ArrayList<>();

    /**
     * Handles the invocation of a dynamic proxy's method. Tracks changes made to
     * setter methods by comparing the new value to the current value retrieved via
     * the corresponding getter. Maintains a change log if differences are detected
     * and applies or logs changes based on the dryRun mode.
     *
     * @param proxy  the proxy instance on which the method is invoked
     * @param method the method being invoked
     * @param args   the arguments passed to the method
     * @return the result of the method invocation on the target object if conditions are met,
     * otherwise returns null
     * @throws Throwable exceptions thrown by the invoked method or reflection errors
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        boolean differentValue = false;
        if (method.getName().startsWith("set") && args.length == 1) {
            String attributeName = method.getName().substring(3);
            Method getter = getGetterFor(attributeName);
            Object originalValue = getter != null ? getter.invoke(keyloakResource) : null;
            if (!Objects.equals(originalValue, args[0])) {
                differentValue = true;
                changeLog.add(attributeName + ": original=" + originalValue + " new=" + args[0]);
            }
        }
        if (!dryRun && differentValue) {
            return method.invoke(keyloakResource, args);
        } else {
            return null;
        }
    }

    private Method getGetterFor(String name) {
        if (keyloakResource != null) {
            Method getter = Arrays.stream(keyloakResource.getClass().getMethods())
                .filter(m -> m.getParameterCount() == 0 && (m.getName().equals("get" + name) || m.getName().equals("is" + name)))
                .findFirst().orElse(null);
            if (getter == null) {
                log.infof("Unable to obtain getter for %s, original value will be reported as null", name);
            }
            return getter;
        }
        return null;
    }

}
