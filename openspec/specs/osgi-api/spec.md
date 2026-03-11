# osgi-api Specification

## Purpose

Defines the public API contracts for OSGi bundle tracking, configuration tracking, service caching, and bundle utilities. All other modules depend on these interfaces and classes.

## Architecture

The API module contains four major groups:

- **Tracking interfaces** — `BundleTrackerManager`, `ConfigurationTrackerManager` with their callback interfaces `BundleCallback`, `ConfigurationCallback`, `ServiceCallback`
- **Service caching** — `ServiceCache<O>` (class-indexed), `ServiceCacheByProperty<O,K>` (property-indexed), `ClassBasedCache<C,O>` interface, `AbstractOsgiClassBasedCache<C,O>` base class
- **Utilities** — `BundleUtil` (manifest headers, resource extraction), `PropertiesUtil` (type conversions), `BundleDelegatingClassLoader`, `StaticServiceReference<T>` (fluent service access)
- **Data types** — `ConfigurationInfo` (configuration event data with `ConfigEventType` enum), `DefaultServiceReady`, `ServiceReady` marker interface

## Requirements

### Requirement: Bundle tracker callback registration

The `BundleTrackerManager` SHALL allow clients to register keyed callbacks for bundle lifecycle events with an optional predicate filter.

#### Scenario: Register callback with filter
- **GIVEN** a `BundleTrackerManager` instance is active
- **WHEN** `registerBundleCallback(key, registerCallback, unregisterCallback, filter)` is called
- **THEN** the `registerCallback` SHALL be invoked for each currently active bundle matching the filter
- **THEN** subsequent bundle STARTED events matching the filter SHALL trigger `registerCallback`
- **THEN** subsequent bundle STOPPED events SHALL trigger `unregisterCallback`

#### Scenario: Unregister callback
- **GIVEN** a callback is registered with a key
- **WHEN** `unregisterBundleCallback(key)` is called
- **THEN** no further callbacks SHALL be invoked for that key

### Requirement: Configuration tracker callback registration

The `ConfigurationTrackerManager` SHALL allow clients to register keyed callbacks for Configuration Admin events (create, update, delete) with a predicate filter.

#### Scenario: Register configuration callback
- **GIVEN** a `ConfigurationTrackerManager` instance is active
- **WHEN** `registerConfigurationCallback(key, createCb, updateCb, deleteCb, filter)` is called
- **THEN** existing configurations matching the filter SHALL trigger `createCb` for each
- **THEN** subsequent configuration create/update/delete events matching the filter SHALL trigger the corresponding callback

### Requirement: Service caching by class

`ServiceCache<O>` SHALL maintain a ranked cache of services indexed by their class, returning the highest-ranked service for a given class.

#### Scenario: Find highest-ranked service
- **GIVEN** multiple services of the same class are registered with different `SERVICE_RANKING` values
- **WHEN** `find(Class)` is called
- **THEN** the service with the highest ranking SHALL be returned

#### Scenario: Open and close tracker
- **WHEN** `openTracker(bundleContext, clazz)` is called
- **THEN** an OSGi `ServiceTracker` SHALL be opened to dynamically bind/unbind services
- **WHEN** `closeTracker()` is called
- **THEN** the tracker SHALL be closed and all cached entries removed

### Requirement: Service caching by property

`ServiceCacheByProperty<O,K>` SHALL maintain a ranked cache of services indexed by a custom property derived from the service via an inspector function.

#### Scenario: Find service by property key
- **GIVEN** services are registered with different property keys and rankings
- **WHEN** `find(K)` is called with a property key
- **THEN** the highest-ranked service matching that key SHALL be returned

### Requirement: Bundle utility header parsing

`BundleUtil` SHALL parse OSGi manifest headers into structured data, handling quoted values correctly.

#### Scenario: Get header values
- **GIVEN** a bundle with manifest header `Custom-Header: value1,value2`
- **WHEN** `getHeaderValues(bundle, "Custom-Header")` is called
- **THEN** a list `["value1", "value2"]` SHALL be returned

#### Scenario: Get header entries with parameters
- **GIVEN** a bundle with manifest header `Custom-Header: name;attr1=val1;attr2=val2`
- **WHEN** `getHeaderEntries(bundle, "Custom-Header")` is called
- **THEN** a list of maps SHALL be returned with the parsed key-value pairs

### Requirement: Bundle resource extraction

`BundleUtil` SHALL copy bundle resources to persistent storage on the filesystem.

#### Scenario: Copy single file
- **GIVEN** a bundle containing a file at a given path
- **WHEN** `copyBundleFileToPersistentStorage(bundle, targetName, fileInBundle)` is called
- **THEN** the file SHALL be written to the bundle's data area and the `File` reference returned

### Requirement: Properties type conversion

`PropertiesUtil` SHALL convert OSGi property values (which may be arrays, collections, or single objects) to typed Java values.

#### Scenario: Convert to boolean
- **WHEN** `toBoolean(propValue, defaultValue)` is called with a string "true"
- **THEN** `true` SHALL be returned

#### Scenario: Convert to string array
- **WHEN** `toStringArray(propValue)` is called with a comma-separated string or array
- **THEN** a `String[]` SHALL be returned with individual values

### Requirement: Static service reference

`StaticServiceReference<T>` SHALL provide a try-with-resources pattern for safely accessing OSGi services.

#### Scenario: Fluent service access
- **WHEN** `staticServiceReference(SomeService.class).access(svc -> svc.doSomething())` is called
- **THEN** the service SHALL be looked up, the function applied, and the service reference released

### Requirement: Bundle delegating class loader

`BundleDelegatingClassLoader` SHALL delegate class loading, resource finding, and resource enumeration to the wrapped OSGi `Bundle`.

#### Scenario: Load class from bundle
- **GIVEN** a `BundleDelegatingClassLoader` wrapping a bundle
- **WHEN** `findClass(name)` is called
- **THEN** `bundle.loadClass(name)` SHALL be invoked

### Requirement: ConfigurationInfo data integrity

`ConfigurationInfo` SHALL be an immutable data class carrying configuration PID, factory PID, properties, and event type.

#### Scenario: Create ConfigurationInfo
- **WHEN** a `ConfigurationInfo` is constructed with pid, factoryPid, properties, and CREATE type
- **THEN** all fields SHALL be accessible via getters
- **THEN** `equals()` and `hashCode()` SHALL be based on all fields
