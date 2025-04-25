# Keycloakform

## Overview

This project aims to provide a library that enables **declarative configuration of Keycloak**.
While Keycloak includes functionality for importing/exporting realms and their submodels,
it lacks a way to declare the desired state of an instance in a manageable and repeatable way.
This library try to solve this gap, facilitating developers and administrators to define desired
configurations and synchronize them with Keycloak in an automated manner.

### Key Features

- **Declarative Configuration**: Define and manage your Keycloak instance using configuration files, avoiding manual UI operations.
- **Synchronization**: Automatically ensure that the actual Keycloak environment matches the declared configuration.
- **State Validation**: Validate the current Keycloak instance against the desired state to detect deviations.
- **Easy Integration**: Seamlessly integrate with CI/CD pipelines to standardize Keycloak configurations across environments.

---

## User Guide

### Installation

To use the project you can use the artefact and extends your Keycloak image by it. Artefact can be found
under the following coordinates.

```xml

<dependency>
    <groupId>com.groocraft</groupId>
    <artifactId>keycloakform</artifactId>
    <version>${keycloakform.version}</version>
</dependency>
```

You can also use the prepared Docker image of Keycloak that already contains Keycloakform directly via run
or as builder.

### Idea

As Keycloak already provides export/import feature, we can create basic configuration of desired state by
exporting manually created configuration. Export is documented [here](https://www.keycloak.org/server/importExport)
Keycloak knows both export types (one or multiple files).

### Usage

1. Read [behavior & hints](#behavior--hints) for understanding
2. Define your Keycloak configuration in a declarative format or use realm export as baseline.
3. [Configure](#configuration) Keycloakform

### Configuration

Possible configuration of Keycloakform:

* --spi-import-keycloakform-source-file=#pathToFile with declaration
* --spi-import-keycloakform-dry-run=true/false

#### Original JSON extension

For every object in Keycloak export JSON you can specify `syncMode` attribute with values:  
* IGNORE - object will not be processed and is used just to access its sub-object. 
* MERGE - object will be only created or updated but nothing will be removed.
* FULL - object id fully managed so it is created, updated or deleted when exist but definition is not present anymore.

**Sync mode is transitive to sub-object means it overrides the default FULL for the whole tree under the object.**

### Behavior & hints

#### Realms

There is always the special realm which is `master` realm. This realm is exceptional because it is not removed by Keycloakform even
when it is missing in definition.

#### Clients

In every realm there are special clients, that are created as the part of creation process of realm. It causes clash of potentially
specified ids of clients

## Developer Guide

### Helpers

How to export current setting of prepared realm in evn/local env:
```shell
docker exec -it #containerName /opt/keycloak/bin/kc.sh export --file /opt/keycloak/data/export/realm.json --realm #realmName
```

### Local development

If you want to have blank Keycloak with possibility of export configuration you will manually create, go to `env/blank` and run
the docker compose here.

If you want to run current local code with a definition, you can go to `env/dev`. There is a definition you can change or extend,
and by executing `run.sh` project 

### Architecture

Current implementation is kinda messy but the basic idea is to:
* parse possibly enhanced export json of Keycloak to so-called Definitions
* Each definition has assigned so-called Former retrievable via FormersFactory based on definition class
* Formers manages changes needed in the way only necessary changes are done

Some construction in Keycloak export are difficult to process so there are also exception in the atomicity of definition processing
which were kept at needed minimum.

#### Definitions

Definitions are POJO classes what extends original Keycloak Representation classes. The basic extension is `syncMode` that adds
possibility to define, if definition should be ignored, merged or fully process ([full explanation here](#original-json-extension))
Definition is interface enforcing the `syncMode`, otherwise it is free form.

Definition must be registered manually in DefinitionMapping.

#### Former

Formers are divides to 2 groups
* Collection - ensures that Item former for all definitions is called and deletes existing object that are missing in definitions
* Item - handles one item of a type, creates or updates dependently on the current state
  * **Important class in DefaultItem is `ItemFormerMethodHandler`. This class uses reflection to updated only values that differs** 
  it means you do not need to check it in former or updater directly.

Former must be registered manually in FormersFactory

#### Updater

Helper classes that extends Keycloak RepresentationToModel processor functions. The basic rule is to update only specified
attributes in definition, otherwise keep state untouched.

### Tests

Currently unit tests are not ideal at all, but it was more effective to use FormingIntegrationTest which is taking a definition from
resources, starts Keycloak, apply the definition, do export of the Keycloak state and then compare to JSONs for discrepancies.
