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

1. Define your Keycloak configuration in a declarative format or use realm export as baseline.
2. [Configure](#configuration) Keycloakform

#### Configuration

Possible configuration of Keycloakform:

* --spi-import-keycloakform-registrar-source-file=#pathToFile with declaration
* --spi-import-keycloakform-dry-run=true/false

### Limitation

Keycloakform is forming the following only (by now):

* Realm attributes, **not** sub-entities like clients, roles, etc.

## Developer Guide

