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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.models.RealmModel;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockingDetails;

@ExtendWith(MockitoExtension.class)
class RealmUpdaterImplTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS) RealmModel realm;

    @Test
    void testNoInteractionWithModelWhenDefinitionEmpty() {
        RealmUpdater updater = new RealmUpdater();
        RealmDefinition definition = new RealmDefinition();

        updater.update(realm, definition);

        assertThat(mockingDetails(realm).getInvocations().stream().map(i -> i.getMethod().getName()).toList())
            .map(name -> name.substring(0, 3))
            .doesNotContain("set");
    }

}