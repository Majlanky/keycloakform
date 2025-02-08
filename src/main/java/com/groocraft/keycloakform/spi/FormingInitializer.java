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

package com.groocraft.keycloakform.spi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.groocraft.keycloakform.config.FormerConfig;
import com.groocraft.keycloakform.definition.RealmDefinition;
import com.groocraft.keycloakform.definition.deserialization.Deserialization;
import com.groocraft.keycloakform.exception.DefinitionFileDeserializationException;
import com.groocraft.keycloakform.exception.DefinitionFileReadingException;
import com.groocraft.keycloakform.former.FormersFactory;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.models.utils.PostMigrationEvent;
import org.keycloak.provider.ProviderEvent;
import org.keycloak.provider.ProviderEventListener;
import org.keycloak.util.JsonSerialization;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

import lombok.CustomLog;

/**
 * A class responsible for initializing and orchestrating the formation process of Keycloak instances
 * based on a provided realms definition file. The class implements the {@link ProviderEventListener}
 * interface to respond to provider events and performs actions as part of the post-migration event
 * lifecycle of Keycloak.
 * The {@code FormingInitializer} is designed to:
 * - Read a realms definition file specified in the {@code FormerConfig} configuration.
 * - Deserialize the realms definition into internal data structures.
 * - Use a {@link FormersFactory} to apply the defined state to Keycloak, either in dry-run mode
 * (generating a plan without applying changes) or in standard mode (executing the formation).
 * Key methods and behaviors:
 * - {@code onEvent(ProviderEvent)}: This method listens for {@code PostMigrationEvent} events and
 * triggers the formation process.
 * Logging:
 * - Informational logging is included to provide visibility into the dry-run status
 * and the source file being used for formation.
 *
 * @author Majlanky
 */
@CustomLog
public class FormingInitializer implements ProviderEventListener {

    private final FormerConfig config;
    private final List<RealmDefinition> definitions;
    private final FormersFactory formersFactory;
    private final ObjectMapper mapper = Deserialization.getObjectMapper(JsonSerialization.mapper);

    public FormingInitializer(FormerConfig config, FormersFactory formersFactory) {
        this.config = config;
        this.formersFactory = formersFactory;
        //reading the definition now to fail earlier when deserialization issues
        definitions = readRealmsDefinition(readDefinitionFile());
    }

    @Override
    public void onEvent(ProviderEvent event) {
        if (event instanceof PostMigrationEvent postMigrationEvent) {
            KeycloakModelUtils.runJobInTransaction(postMigrationEvent.getFactory(),
                s -> process(s, definitions));
        }
    }

    private void process(KeycloakSession session, List<RealmDefinition> definitions) {
        if (config.isDryRun()) {
            log.info("Keycloakform is running in dry run mode so it will not do any changes, just share its plans");
        }
        formersFactory.getForCollectionOf(RealmDefinition.class).form(definitions, session, config.isDryRun());
    }

    private List<RealmDefinition> readRealmsDefinition(InputStream inputStream) {
        try {
            return Deserialization.getRealmsFromStream(inputStream);
        } catch (IOException e) {
            throw new DefinitionFileDeserializationException("Unable to deserialize input stream", e);
        }
    }

    private InputStream readDefinitionFile() {
        File definitionFile = new File(config.getSourceFile());
        log.infof("This instance will be formed following %s", definitionFile.getAbsolutePath());
        try {
            return Files.newInputStream(definitionFile.toPath());
        } catch (IOException e) {
            throw new DefinitionFileReadingException("Unable open stream to " + definitionFile.getAbsolutePath(), e);
        }
    }
}
