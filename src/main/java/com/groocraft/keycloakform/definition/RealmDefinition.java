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

package com.groocraft.keycloakform.definition;

import com.groocraft.keycloakform.former.SyncMode;

import org.keycloak.representations.idm.RealmRepresentation;

import lombok.Getter;
import lombok.Setter;

/**
 * Realm is identified by id or name when the id unspecified
 */
@Getter
@Setter
public class RealmDefinition extends RealmRepresentation implements Definition {

    private SyncMode syncMode = SyncMode.FULL;

}