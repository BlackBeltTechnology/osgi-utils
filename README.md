# OSGi Utils

[![Build](https://github.com/BlackBeltTechnology/osgi-utils/actions/workflows/build.yml/badge.svg?branch=develop)](https://github.com/BlackBeltTechnology/osgi-utils/actions/workflows/build.yml)

## Introduction

OSGi Utils is a utility library by [BlackBelt Technology](http://www.blackbelt.hu) that simplifies common tasks in OSGi-based applications. Rather than dealing with low-level OSGi APIs directly, this library provides higher-level abstractions for:

1. **Bundle lifecycle tracking** — register callbacks that fire when bundles start or stop, with optional filtering
2. **Configuration tracking** — monitor Configuration Admin changes (create/update/delete) with predicate-based filtering
3. **Service caching** — maintain ranked caches of OSGi services, indexed by class or custom properties
4. **Bundle utilities** — parse manifest headers, extract bundle resources to persistent storage, delegate class loading to bundles
5. **Testing support** — mock OSGi component lifecycle (activate/deactivate/bind/unbind) without a running container

## Module Overview

```mermaid
graph TD
    API[osgi-api<br/>Public interfaces & utilities]
    IMPL[osgi-impl<br/>OSGi component implementations]
    TEST[osgi-test<br/>Mock OSGi for testing]
    FEAT[features<br/>Karaf feature descriptor]
    KAR[kar<br/>Karaf KAR archive]
    RPT[reports<br/>JaCoCo coverage aggregation]

    IMPL --> API
    TEST -.->|test dependency| IMPL
    FEAT --> API
    FEAT --> IMPL
    KAR --> FEAT
    RPT --> API
    RPT --> IMPL
    RPT --> TEST
```

| Module | Packaging | Description |
|--------|-----------|-------------|
| `osgi-api` | bundle | Public API: interfaces (`BundleTrackerManager`, `ConfigurationTrackerManager`, `ClassBasedCache`), utilities (`BundleUtil`, `PropertiesUtil`), service caching (`ServiceCache`, `ServiceCacheByProperty`), and data types (`ConfigurationInfo`) |
| `osgi-impl` | bundle | OSGi DS components: `BundleTrackerManagerImpl` (synchronous bundle listener), `ConfigurationTrackerManagerImpl` (synchronous configuration listener), `OsgiUtil` |
| `osgi-test` | bundle | `MockOsgi` — activates/deactivates components, binds/unbinds `@Reference` fields via reflection. `BeanUtil` — reflection helpers for test setup |
| `features` | feature | Karaf feature descriptor pulling in `osgi-api`, `osgi-impl`, `guava-30`, and `scr` |
| `kar` | kar | Packages the Karaf feature into a deployable KAR archive |
| `reports` | pom | Aggregates JaCoCo code coverage across all modules |

## Key API Components

The following diagram shows the primary public interfaces and classes, and how the implementation module connects to them:

```mermaid
classDiagram
    class BundleTrackerManager {
        <<interface>>
        +registerBundleCallback(key, register, unregister, filter)
        +unregisterBundleCallback(key)
    }
    class ConfigurationTrackerManager {
        <<interface>>
        +registerConfigurationCallback(key, create, update, delete, filter)
        +unregisterConfigurationCallback(key)
    }
    class BundleCallback {
        <<interface>>
        +accept(Bundle)
        +process(Bundle) Thread
    }
    class ConfigurationCallback {
        <<interface>>
        +accept(ConfigurationInfo)
        +process(ConfigurationInfo) Thread
    }
    class ConfigurationInfo {
        +configurationPid: String
        +configurationFactoryPid: String
        +properties: Dictionary
        +type: ConfigEventType
    }
    class ServiceCache~O~ {
        +find(Class) O
        +openTracker(BundleContext, Class)
        +closeTracker()
    }
    class ServiceCacheByProperty~O,K~ {
        +find(K) O
        +openTracker(BundleContext, Class)
        +closeTracker()
    }
    class BundleTrackerManagerImpl {
        +activate(BundleContext)
        +deactivate(BundleContext)
    }
    class ConfigurationTrackerManagerImpl {
        +activate(BundleContext)
        +deactivate()
    }

    BundleTrackerManager <|.. BundleTrackerManagerImpl
    ConfigurationTrackerManager <|.. ConfigurationTrackerManagerImpl
    BundleTrackerManager --> BundleCallback : uses
    ConfigurationTrackerManager --> ConfigurationCallback : uses
    ConfigurationCallback --> ConfigurationInfo : receives
```

## Runtime Interaction

A typical flow when a component registers for bundle tracking:

```mermaid
sequenceDiagram
    participant Client as Client Component
    participant BTM as BundleTrackerManagerImpl
    participant OSGi as OSGi Framework

    Client->>BTM: registerBundleCallback(key, onRegister, onUnregister, filter)
    BTM->>BTM: Apply callback to existing ACTIVE bundles
    OSGi->>BTM: SynchronousBundleListener.bundleChanged(STARTED)
    BTM->>BTM: Check filter predicate
    BTM->>Client: onRegister.accept(bundle)
    OSGi->>BTM: SynchronousBundleListener.bundleChanged(STOPPED)
    BTM->>Client: onUnregister.accept(bundle)
```

## External Dependencies

```mermaid
graph LR
    subgraph External
        OSGiCore[OSGi Core 6.0]
        OSGiCmpn[OSGi Compendium 6.0]
        Guava[Guava 30.0-jre]
        Lombok[Lombok 1.18]
        Karaf[Apache Karaf 4.4.7]
    end
    subgraph "osgi-utils"
        API[osgi-api]
        IMPL[osgi-impl]
        TEST[osgi-test]
    end
    API --> OSGiCore
    API --> Guava
    IMPL --> OSGiCmpn
    IMPL --> Lombok
    TEST --> OSGiCore
    IMPL -.-> Karaf
```

## Build

The project uses Maven with a wrapper script. Java 21 is required to compile (targets Java 17 runtime).

```bash
# Full build
./mvnw clean install

# Tests only
./mvnw clean test

# Single test class
./mvnw test -pl osgi-impl -Dtest=BundleTrackerManagerImplTest

# Skip tests
./mvnw clean install -DskipTests
```

### Maven Profiles

| Profile | Purpose |
|---------|---------|
| `modules` | Active by default — includes all submodules |
| `sign-artifacts` | GPG-sign artifacts for release |
| `release-dummy` | Deploy to local filesystem (for testing) |
| `release-judong` | Deploy to JuDong Nexus repository |
| `release-central` | Deploy to Maven Central via OSSRH |
| `generate-github-asciidoc-diagrams` | Generate PNG diagrams from AsciiDoc/PlantUML |
| `update-source-code-license` | Update Apache 2.0 license headers in source files |

## Contributing

Everyone is welcome to contribute. Please see [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines on submitting issues and pull requests.

## License

This project is licensed under the [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).
