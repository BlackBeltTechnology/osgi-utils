# OSGi Utils - Project Documentation

## Project Overview


**Repository:** BlackBeltTechnology/osgi-utils
**License:** Apache License 2.0
**Java Version:** 21 (compile) / 17 (runtime target)
**Build System:** Maven with Maven Wrapper (`./mvnw`), OSGi Bundle Plugin 5.1.8, Karaf Maven Plugin 4.4.7

1. Provides bundle lifecycle tracking via `BundleTrackerManager` — register callbacks for bundle start/stop events with optional predicate filtering
2. Provides configuration change tracking via `ConfigurationTrackerManager` — monitor OSGi Configuration Admin create/update/delete events
3. Implements ranked service caching (`ServiceCache`, `ServiceCacheByProperty`) using `ConcurrentSkipListMap` for thread-safe, ranking-aware service lookup
4. Offers bundle utilities (`BundleUtil`, `PropertiesUtil`, `BundleDelegatingClassLoader`, `StaticServiceReference`) for manifest parsing, resource extraction, class loading delegation, and property conversion
5. Provides `MockOsgi` testing utility that activates/deactivates OSGi DS components and binds/unbinds `@Reference` dependencies via reflection without requiring a running OSGi container

## Code Instructions

1. First think through the problem, read the codebase for relevant files.
2. Before you make any major changes, check in with me and I will verify the plan.
3. Please every step of the way just give me a high level explanation of what changes you made.
4. Make every task and code change you do as simple as possible. We want to avoid making any massive or complex changes. Every change should impact as little code as possible. Everything is about simplicity.
5. Maintain a documentation file that describes how the architecture of the app works inside and out.
6. Never speculate about code you have not opened. If the user references a specific file, you MUST read the file before answering. Make sure to investigate and read relevant files BEFORE answering questions about the codebase. Never make any claims about code before investigating unless you are certain of the correct answer - give grounded and hallucination-free answers.
7. For implementation use TDD (Test-Driven Development): write or update tests first to define the expected behaviour, verify they fail, then write the minimal implementation to make them pass.
8. Use DRY (Don't Repeat Yourself): extract reusable logic into separate classes, utilities, or components. If the same pattern appears in multiple places, refactor it into a shared helper.

## Directory Structure

```
osgi-utils/
├── osgi-api/           # Public API — interfaces, utilities, service caching
│   └── src/main/java/hu/blackbelt/osgi/utils/osgi/api/
├── osgi-impl/          # OSGi DS component implementations
│   └── src/main/java/hu/blackbelt/osgi/utils/internal/impl/
├── osgi-test/          # Mock OSGi testing utilities
│   └── src/main/java/hu/blackbelt/osgi/utils/test/
├── features/           # Karaf feature XML descriptor
│   └── src/main/feature/feature.xml
├── kar/                # Karaf KAR archive assembly
├── reports/            # JaCoCo aggregate coverage reports
├── .github/workflows/  # GitHub Actions CI/CD
└── openspec/           # OpenSpec project specs
```

## Core Modules

### API Layer

| Module | Type | Purpose |
|--------|------|---------|
| `osgi-api/` | OSGi bundle | Public interfaces (`BundleTrackerManager`, `ConfigurationTrackerManager`, `BundleCallback`, `ConfigurationCallback`, `ServiceCallback`, `ServiceReady`, `ClassBasedCache`), utility classes (`BundleUtil`, `PropertiesUtil`, `BundleDelegatingClassLoader`, `StaticServiceReference`), service caches (`ServiceCache<O>`, `ServiceCacheByProperty<O,K>`), data types (`ConfigurationInfo`, `DefaultServiceReady`), abstract base (`AbstractOsgiClassBasedCache`) |

### Implementation Layer

| Module | Type | Purpose |
|--------|------|---------|
| `osgi-impl/` | OSGi bundle | `BundleTrackerManagerImpl` — DS component using `SynchronousBundleListener` with thread management for async callbacks. `ConfigurationTrackerManagerImpl` — DS component using `SynchronousConfigurationListener` and `ConfigurationAdmin` reference. `OsgiUtil` — static helper to retrieve services from outside the OSGi container |

### Testing Layer

| Module | Type | Purpose |
|--------|------|---------|
| `osgi-test/` | OSGi bundle | `MockOsgi` — reflection-based mock that handles both Felix SCR and standard OSGi DS annotations; supports field injection and setter injection, activate/deactivate lifecycle, and bind/unbind references. `BeanUtil` — functional reflection helpers (`callMethod`, `setField`) |

### Packaging Layer

| Module | Type | Purpose |
|--------|------|---------|
| `features/` | Karaf feature | Defines `osgi-utils` feature depending on `guava-30` and `scr` features, bundles `osgi-api` and `osgi-impl` |
| `kar/` | Karaf KAR | Packages feature into a deployable KAR archive |
| `reports/` | POM | Aggregates JaCoCo code coverage from all modules |

## Technology Stack

### Core Technologies
- **OSGi Core 6.0** / **OSGi Compendium 6.0** — framework APIs, Configuration Admin, Declarative Services
- **OSGi DS annotations 1.3** — `@Component`, `@Reference`, `@Activate`, `@Deactivate`
- **Google Guava 30.0-jre** — `ImmutableList`, `ImmutableMap`, `Preconditions`, functional utilities
- **Lombok 1.18.34** — `@Slf4j`, `@Getter`, `@Setter`, `@Synchronized`, `@SneakyThrows`
- **SLF4J 1.7.25** / **Logback 1.5.12** — logging
- **Apache Karaf 4.4.7** — runtime container, feature/KAR packaging

### Testing
- **JUnit 4.13** — test framework
- **Mockito 1.10.19** — mocking
- **Hamcrest 2.0.0.0** — test matchers
- **PAX Exam 4.13.3** — OSGi integration testing with Apache Felix 6.0.3
- **Javassist 3.19.0-GA** — bytecode manipulation for annotation scanning in `MockOsgi`
- **Reflections 0.9.10** — classpath scanning

### Build & Quality
- **Maven Bundle Plugin 5.1.8** — generates OSGi bundle manifests and JARs
- **Maven Surefire 3.5.1** — test execution with `--add-opens` JVM flags for Java module access
- **JaCoCo 0.8.12** — code coverage
- **SonarQube** — code quality analysis (hosted at sonar.judo.technology)
- **Flatten Maven Plugin 1.3.0** — resolves `${revision}` CI-friendly versioning
- **Lombok Maven Plugin** — delombok for JavaDoc generation

## Build Commands

```bash
# Full build with tests
./mvnw clean install

# Tests only
./mvnw clean test

# Single test class
./mvnw test -pl osgi-impl -Dtest=BundleTrackerManagerImplTest

# Skip tests
./mvnw clean install -DskipTests

# Build without submodules (parent POM only)
./mvnw clean install -DskipModules=true

# Code coverage (reports in reports/target/site/jacoco-aggregate/)
./mvnw clean verify
```

### Maven Profiles

| Profile | Purpose |
|---------|---------|
| `modules` | Active by default — includes all submodules (disabled with `-DskipModules=true`) |
| `sign-artifacts` | GPG-sign artifacts via `sign-maven-plugin` |
| `release-dummy` | Deploy to local `/tmp/` filesystem for testing |
| `release-judong` | Deploy to JuDong Nexus (nexus.judo.technology) |
| `release-central` | Deploy to Maven Central via Sonatype OSSRH |
| `generate-github-asciidoc-diagrams` | Generate PNG diagrams from PlantUML in `.github/` |
| `update-source-code-license` | Update Apache 2.0 license headers in all source files |

## Key Configuration Files

| File | Purpose |
|------|---------|
| `pom.xml` | Root POM — defines all dependency versions, plugin configurations, and profiles |
| `osgi-api/pom.xml` | API module POM — `bundle` packaging, Export-Package for `hu.blackbelt.osgi.utils.osgi.api` |
| `osgi-impl/pom.xml` | Impl module POM — `bundle` packaging, Export-Package for `hu.blackbelt.osgi.utils.internal.impl` |
| `osgi-test/pom.xml` | Test module POM — `bundle` packaging with compile-scoped Mockito and Reflections |
| `features/src/main/feature/feature.xml` | Karaf feature descriptor — bundles and feature dependencies |
| `logback-test.xml` | Logback configuration for test execution (console appender, INFO level) |
| `.github/workflows/build.yml` | GitHub Actions CI/CD — build, deploy, tag, release |
| `.mvn/extensions.xml` | Maven extensions — wagon-file and wagon-webdav-jackrabbit for deployment |

## Development Environment

**Required:**
- Java 21 JDK (Zulu distribution recommended)
- Maven 3.8+ (or use included `./mvnw` wrapper)

**JVM test flags:** Surefire is configured with `--add-opens` flags for `java.base/java.lang`, `java.base/java.util`, `java.base/java.time`, `java.base/java.net` (required for reflection-based testing with Mockito and MockOsgi).

## Git Workflow

- **Main Branch:** `develop`
- **Versioning:** `${revision}` property, currently `1.1.1-SNAPSHOT`; CI generates dynamic versions with timestamp and commit hash
- **Branch naming:** `feature/JNG-xxx_summary`, `bugfix/JNG-xxx_summary`, `release/X.Y-betaN`
- **Commit rule:** Every commit must reference a JIRA ticket (`JNG-xxx`)
- **CI:** GitHub Actions on `judong` runner, deploys to Nexus, creates GitHub releases on `develop`

## Important Notes

1. All implementations use **synchronous listeners** (`SynchronousBundleListener`, `SynchronousConfigurationListener`) — callbacks execute in the OSGi event thread, not asynchronously
2. `ServiceCache` uses `ConcurrentSkipListMap` with OSGi `SERVICE_RANKING` — higher-ranked services are preferred when multiple implementations exist for the same class
3. `MockOsgi` supports both **Felix SCR annotations** (`org.apache.felix.scr.annotations`) and **standard OSGi DS annotations** (`org.osgi.service.component.annotations`) — it uses Javassist to read class-level annotations at runtime
4. `BundleCallback.process()` returns an optional `Thread` for async processing — `BundleTrackerManagerImpl` manages these threads and interrupts them on unregistration
5. The project uses `flatten-maven-plugin` to resolve `${revision}` at build time — never edit `.flattened-pom.xml` files directly
6. `osgi-test` module has **compile-scoped** dependencies on Mockito and Reflections (not test-scoped) because it's a library consumed by other projects' tests

## Related Documentation

- [README.md](README.md) — project overview with architecture diagrams
- [CONTRIBUTING.md](CONTRIBUTING.md) — contribution guidelines and build commands
- [.github/CIFLOW.md](.github/CIFLOW.md) — CI/CD workflow documentation with branch strategy
