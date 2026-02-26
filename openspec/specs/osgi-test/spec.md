# osgi-test Specification

## Purpose

Provides mock OSGi utilities that allow testing OSGi DS components (activate, deactivate, bind, unbind) via reflection without requiring a running OSGi container.

## Architecture

- `MockOsgi` — Static utility class that uses reflection and Javassist to discover OSGi annotations on components, then invokes lifecycle methods and sets `@Reference`-annotated fields. Supports both Apache Felix SCR annotations and standard OSGi DS annotations.
- `BeanUtil` — Functional reflection helpers that return `Function` objects for invoking methods and setting fields on target objects.

## Requirements

### Requirement: Component activation

`MockOsgi.activate()` SHALL invoke the `@Activate`-annotated method on a component using reflection.

#### Scenario: Activate with no parameters
- **GIVEN** a component class with an `@Activate` method accepting no parameters
- **WHEN** `MockOsgi.activate(object)` is called
- **THEN** the `@Activate` method SHALL be invoked

#### Scenario: Activate with BundleContext
- **GIVEN** a component class with an `@Activate` method accepting `BundleContext`
- **WHEN** `MockOsgi.activate(object, bundleContext)` is called
- **THEN** the `@Activate` method SHALL be invoked with the provided `BundleContext`

#### Scenario: Activate with ComponentContext
- **GIVEN** a component class with an `@Activate` method accepting `ComponentContext`
- **WHEN** `MockOsgi.activate(object, componentContext)` is called
- **THEN** the `@Activate` method SHALL be invoked with the provided `ComponentContext`

#### Scenario: Activate with Map or Dictionary
- **GIVEN** a component class with an `@Activate` method accepting `Map` or `Dictionary`
- **WHEN** `MockOsgi.activate(object, propertiesMap)` is called
- **THEN** the `@Activate` method SHALL be invoked with the provided properties

### Requirement: Component deactivation

`MockOsgi.deactivate()` SHALL invoke the `@Deactivate`-annotated method on a component using reflection, following the same parameter resolution as activation.

#### Scenario: Deactivate component
- **GIVEN** a component with a `@Deactivate` method
- **WHEN** `MockOsgi.deactivate(object)` is called
- **THEN** the `@Deactivate` method SHALL be invoked

### Requirement: Reference binding

`MockOsgi.setReferences()` SHALL inject service instances into `@Reference`-annotated fields or via bind methods.

#### Scenario: Set references via field injection
- **GIVEN** a component with `@Reference`-annotated fields
- **WHEN** `MockOsgi.setReferences(object, serviceA, serviceB)` is called
- **THEN** each field whose type matches a provided instance SHALL be set via reflection

#### Scenario: Set references via bind method
- **GIVEN** a component with a `@Reference` annotation specifying a `bind` method name
- **WHEN** `MockOsgi.setReferences(object, serviceInstance)` is called
- **THEN** the bind method SHALL be invoked with the matching service instance

### Requirement: Felix SCR annotation support

`MockOsgi` SHALL support Apache Felix SCR annotations (`org.apache.felix.scr.annotations.Reference`, `@References`, `@Activate`, `@Deactivate`).

#### Scenario: Felix @References at class level
- **GIVEN** a component annotated with `@References` containing multiple `@Reference` entries at class level
- **WHEN** `MockOsgi.setReferences(object, instances...)` is called
- **THEN** each reference SHALL be resolved by `referenceInterface` and bound via the specified `bind` method

### Requirement: Standard OSGi DS annotation support

`MockOsgi` SHALL support standard OSGi DS annotations (`org.osgi.service.component.annotations.Reference`, `@Component`, `@Activate`, `@Deactivate`).

#### Scenario: Standard @Reference on fields
- **GIVEN** a component with `org.osgi.service.component.annotations.Reference`-annotated fields
- **WHEN** `MockOsgi.setReferences(object, instances...)` is called
- **THEN** fields SHALL be matched by type and set via reflection

### Requirement: Annotation inheritance

`MockOsgi` SHALL discover annotations on superclasses and implemented interfaces, not just the direct class.

#### Scenario: Inherited @Reference
- **GIVEN** a component extends a base class with `@Reference`-annotated fields
- **WHEN** `MockOsgi.setReferences(object, instance)` is called
- **THEN** the inherited field SHALL be set

### Requirement: BeanUtil reflection helpers

`BeanUtil` SHALL provide functional wrappers for reflective method invocation and field setting.

#### Scenario: Call method via reflection
- **GIVEN** a target object with a method accepting a specific parameter type
- **WHEN** `BeanUtil.callMethod(target, arg).apply(method)` is called
- **THEN** the method SHALL be invoked on the target with the provided argument

#### Scenario: Set field via reflection
- **GIVEN** a target object with a private field
- **WHEN** `BeanUtil.setField(target, value).apply(field)` is called
- **THEN** the field SHALL be made accessible and set to the provided value
