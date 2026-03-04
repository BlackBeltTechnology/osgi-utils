# osgi-impl Specification

## Purpose

Provides the OSGi Declarative Services (DS) component implementations of the tracking manager interfaces defined in `osgi-api`. These components are registered as OSGi services and manage the lifecycle of bundle and configuration tracking.

## Architecture

- `BundleTrackerManagerImpl` — `@Component(immediate = true)` implementing `BundleTrackerManager`. Uses a `SynchronousBundleListener` and maintains maps of registered callbacks, filters, tracked bundles, and processing threads, all protected by a shared lock object.
- `ConfigurationTrackerManagerImpl` — `@Component(immediate = true, configurationPolicy = IGNORE)` implementing `ConfigurationTrackerManager`. References `ConfigurationAdmin`, registers a `SynchronousConfigurationListener` as an OSGi service, and maintains callback maps for create/update/delete events.
- `OsgiUtil` — Static utility to retrieve OSGi services from outside the OSGi container using `FrameworkUtil.getBundle()`.

## Requirements

### Requirement: BundleTrackerManagerImpl activation

The component SHALL register a `SynchronousBundleListener` on activation and unregister it on deactivation.

#### Scenario: Component activates
- **GIVEN** the OSGi container starts the component
- **WHEN** `@Activate` is called with `BundleContext`
- **THEN** a `SynchronousBundleListener` SHALL be registered with the bundle context

#### Scenario: Component deactivates
- **GIVEN** the component is active with registered callbacks
- **WHEN** `@Deactivate` is called
- **THEN** the `SynchronousBundleListener` SHALL be removed
- **THEN** all unregister callbacks SHALL be invoked for currently tracked bundles
- **THEN** all processing threads SHALL be interrupted

### Requirement: Bundle event handling

The `BundleTrackerManagerImpl` SHALL process bundle events synchronously and apply matching callbacks.

#### Scenario: Bundle starts
- **GIVEN** callbacks are registered with a filter predicate
- **WHEN** a bundle fires a STARTED event matching the filter
- **THEN** the register callback SHALL be invoked
- **THEN** the bundle SHALL be added to the tracked set for that key

#### Scenario: Bundle stops
- **GIVEN** a bundle was previously tracked for a key
- **WHEN** the bundle fires a STOPPED event
- **THEN** the unregister callback SHALL be invoked
- **THEN** the bundle SHALL be removed from the tracked set

#### Scenario: Async thread management
- **GIVEN** a `BundleCallback.process()` returns a non-null `Thread`
- **WHEN** the bundle event is processed
- **THEN** the thread SHALL be started
- **THEN** on unregistration, the corresponding thread SHALL be interrupted

### Requirement: Existing bundle scanning on callback registration

When a new callback is registered, `BundleTrackerManagerImpl` SHALL apply the register callback to all currently ACTIVE bundles that match the filter.

#### Scenario: Register callback with existing active bundles
- **GIVEN** bundles A and B are already ACTIVE and match the filter
- **WHEN** `registerBundleCallback(key, registerCb, unregisterCb, filter)` is called
- **THEN** `registerCb` SHALL be invoked for both A and B

### Requirement: ConfigurationTrackerManagerImpl activation

The component SHALL scan all existing configurations on activation and register a `SynchronousConfigurationListener`.

#### Scenario: Component activates
- **GIVEN** the OSGi container starts the component
- **WHEN** `@Activate` is called
- **THEN** all existing configurations SHALL be loaded into the PID cache
- **THEN** a `SynchronousConfigurationListener` SHALL be registered as an OSGi service

#### Scenario: Component deactivates
- **GIVEN** the component is active
- **WHEN** `@Deactivate` is called
- **THEN** the `SynchronousConfigurationListener` service registration SHALL be unregistered

### Requirement: Configuration event handling

The `ConfigurationTrackerManagerImpl` SHALL process configuration events and dispatch to the appropriate callback (create/update/delete).

#### Scenario: New configuration created
- **GIVEN** a callback is registered with a filter
- **WHEN** a configuration CM_UPDATED event fires for a PID not previously seen
- **THEN** the create callback SHALL be invoked with a `ConfigurationInfo` of type CREATE
- **THEN** the PID and its properties SHALL be cached

#### Scenario: Configuration updated
- **GIVEN** a PID was previously cached
- **WHEN** a CM_UPDATED event fires for that PID
- **THEN** the update callback SHALL be invoked with type UPDATE
- **THEN** the cached properties SHALL be updated

#### Scenario: Configuration deleted
- **GIVEN** a PID was previously cached
- **WHEN** a CM_DELETED event fires for that PID
- **THEN** the delete callback SHALL be invoked with type DELETE
- **THEN** the PID SHALL be removed from the cache

### Requirement: Configuration callback registration scans existing configurations

When a new callback is registered, `ConfigurationTrackerManagerImpl` SHALL invoke the create callback for all existing configurations matching the filter.

#### Scenario: Register callback with existing configurations
- **GIVEN** configurations X and Y exist and match the filter
- **WHEN** `registerConfigurationCallback(key, createCb, updateCb, deleteCb, filter)` is called
- **THEN** `createCb` SHALL be invoked for both X and Y

### Requirement: OsgiUtil service retrieval

`OsgiUtil.getServiceFromOsgi(Class<T>)` SHALL retrieve an OSGi service instance from outside the container using `FrameworkUtil`.

#### Scenario: Successful service lookup
- **GIVEN** an OSGi service of type T is registered
- **WHEN** `getServiceFromOsgi(T.class)` is called
- **THEN** the service instance SHALL be returned

#### Scenario: Service not available
- **GIVEN** no OSGi service of type T is registered
- **WHEN** `getServiceFromOsgi(T.class)` is called
- **THEN** a `RuntimeException` SHALL be thrown
