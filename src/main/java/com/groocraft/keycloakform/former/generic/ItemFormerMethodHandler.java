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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

import io.smallrye.mutiny.tuples.Functions;
import javassist.util.proxy.MethodHandler;
import lombok.AllArgsConstructor;
import lombok.CustomLog;

@CustomLog
@AllArgsConstructor
public class ItemFormerMethodHandler implements MethodHandler {

    private final Object keyloakResource;
    private final Functions.TriConsumer<String, Object, Object> changeLogger;

    @Override
    public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
        if (thisMethod.getName().startsWith("set") && args.length == 1) {
            String attributeName = thisMethod.getName().substring(3);
            Method getter = getGetterFor(attributeName);
            Object originalValue = getter != null ? getter.invoke(keyloakResource) : null;
            if (!Objects.equals(originalValue, args[0])) {
                thisMethod.invoke(keyloakResource, args);
                changeLogger.accept(attributeName, originalValue, args[0]);
            }
            return null;
        } else {
            return thisMethod.invoke(keyloakResource, args);
        }
    }

    private Method getGetterFor(String name) {
        Method getter = Arrays.stream(keyloakResource.getClass().getMethods())
            .filter(m -> m.getParameterCount() == 0 && (m.getName().equals("get" + name) || m.getName().equals("is" + name)))
            .findFirst().orElse(null);
        if (getter == null) {
            log.infof("Unable to obtain getter for %s, original value will be reported as null", name);
        }
        return getter;
    }
}
