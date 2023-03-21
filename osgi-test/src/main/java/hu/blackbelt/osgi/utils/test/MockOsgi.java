package hu.blackbelt.osgi.utils.test;

/*-
 * #%L
 * OSGi util tester (mock)
 * %%
 * Copyright (C) 2018 - 2022 BlackBelt Technology
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.References;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.MethodAnnotationsScanner;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static hu.blackbelt.osgi.utils.test.BeanUtil.callMethod;
import static hu.blackbelt.osgi.utils.test.BeanUtil.setField;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Getter
@Setter
public final class MockOsgi {

    public static final String THE_CONTEXT_MUST_BE_NON_NULL = "The context must be non-null.";
    public static final String OBJECT_MUST_BE_NON_NULL = "Object must be non-null.";
    public static final String REFERENCE_IN_CLASS_LEVEL_HAVE_TO_CONTAIN_REFERENCE_INTERFACE =
            "@Reference in class level have to contain referenceInterface";
    public static final String REFERENCE_IN_CLASS_LEVEL_HAVE_TO_CONTAIN_NAME = "@Reference in class level have to contain name";

    private MockOsgi() {
    }

    public static void activate(Object object) {
        activate(object, new Hashtable<>());
    }

    public static void deactivate(Object object) {
        deactivate(object, new Hashtable<>());
    }

    public static void activate(final Object object, final Object param) {
        checkNotNull(param, OBJECT_MUST_BE_NON_NULL);
        activate(object.getClass(), object, param);
    }

    public static void deactivate(Object object, Object param) {
        checkNotNull(param, OBJECT_MUST_BE_NON_NULL);
        deactivate(object.getClass(), object, param);
    }

    public static Function<References, Stream<Reference>> referenceIterableFromFelixReferencesAnnotation() {
        return new Function<org.apache.felix.scr.annotations.References, Stream<org.apache.felix.scr.annotations.Reference>>() {
            @Nullable
            @Override
            public Stream<org.apache.felix.scr.annotations.Reference> apply(@Nullable org.apache.felix.scr.annotations.References input) {
                return Stream.of(input.value());
            }
        };
    }


    public static Function<org.apache.felix.scr.annotations.Reference, Void> callBindForFelixReference(final Object object, final Object instance) {
        return new Function<org.apache.felix.scr.annotations.Reference, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable org.apache.felix.scr.annotations.Reference reference) {
                final Optional<String> name = Optional.ofNullable(reference.name()).filter(s -> !s.isEmpty());
                checkNotNull(name.isPresent(), REFERENCE_IN_CLASS_LEVEL_HAVE_TO_CONTAIN_NAME);

                final Class<?> referenceInterface = reference.referenceInterface();
                checkNotNull(referenceInterface, REFERENCE_IN_CLASS_LEVEL_HAVE_TO_CONTAIN_REFERENCE_INTERFACE);

                if (!referenceInterface.isAssignableFrom(instance.getClass())) {
                    return null;
                }

                final String bind = Optional.ofNullable(reference.bind()).filter(s -> !s.isEmpty()).orElse("bind" + name.get());
                ReflectionUtils.getAllMethods(object.getClass(),
                        methodParameterType(bind, referenceInterface)).forEach(m -> {
                    callMethod(object, instance).apply(m);
                });
                return null;
            }
        };
    }

    public static Function<org.apache.felix.scr.annotations.Reference, Void> callUnbindForFelixReference(final Object object, final Object instance) {
        return new Function<org.apache.felix.scr.annotations.Reference, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable org.apache.felix.scr.annotations.Reference reference) {
                final Optional<String> name = Optional.ofNullable(reference.name()).filter(s -> !s.isEmpty());
                checkNotNull(name.isPresent(), REFERENCE_IN_CLASS_LEVEL_HAVE_TO_CONTAIN_NAME);

                final Class<?> referenceInterface = reference.referenceInterface();
                checkNotNull(referenceInterface, REFERENCE_IN_CLASS_LEVEL_HAVE_TO_CONTAIN_REFERENCE_INTERFACE);

                if (!referenceInterface.isAssignableFrom(instance.getClass())) {
                    return null;
                }

                final String unbind = Optional.ofNullable(reference.bind()).filter(s -> !s.isEmpty()).orElse("unbind" + name.get());
                ReflectionUtils.getAllMethods(object.getClass(),
                        methodParameterType(unbind, referenceInterface)).forEach(m -> {
                    callMethod(object, instance).apply(m);
                });
                return null;
            }
        };
    }

    public static Function<org.osgi.service.component.annotations.Component, Stream<org.osgi.service.component.annotations.Reference>> referenceIterableFromStandardComponentAnnotation() {
        return new Function<org.osgi.service.component.annotations.Component, Stream<org.osgi.service.component.annotations.Reference>>() {
            @Nullable
            @Override
            public Stream<org.osgi.service.component.annotations.Reference> apply(@Nullable org.osgi.service.component.annotations.Component input) {
                return Stream.of(input.reference());
            }
        };
    }


    public static Function<org.osgi.service.component.annotations.Reference, Void> callBindForStandardReference(final Object object, final Object instance) {
        return new Function<org.osgi.service.component.annotations.Reference, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable org.osgi.service.component.annotations.Reference reference) {
                final Optional<String> name = Optional.ofNullable(reference.name()).filter(s -> !s.isEmpty());
                checkNotNull(name.isPresent(), REFERENCE_IN_CLASS_LEVEL_HAVE_TO_CONTAIN_NAME);

                final Class<?> referenceInterface = reference.service();
                checkNotNull(referenceInterface, REFERENCE_IN_CLASS_LEVEL_HAVE_TO_CONTAIN_REFERENCE_INTERFACE);

                if (!referenceInterface.isAssignableFrom(instance.getClass())) {
                    return null;
                }

                final String bind = Optional.ofNullable(reference.bind()).filter(s -> !s.isEmpty()).orElse("bind" + name.get());
                ReflectionUtils.getAllMethods(object.getClass(),
                        methodParameterType(bind, referenceInterface)).forEach(m -> {
                    callMethod(object, instance).apply(m);
                });
                return null;
            }
        };
    }

    public static Function<org.osgi.service.component.annotations.Reference, Void> callUnbindForStandardReference(final Object object, final Object instance) {
        return new Function<org.osgi.service.component.annotations.Reference, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable org.osgi.service.component.annotations.Reference reference) {
                final Optional<String> name = Optional.ofNullable(reference.name()).filter(s -> !s.isEmpty());
                checkNotNull(name.isPresent(), REFERENCE_IN_CLASS_LEVEL_HAVE_TO_CONTAIN_NAME);

                final Class<?> referenceInterface = reference.service();
                checkNotNull(referenceInterface, REFERENCE_IN_CLASS_LEVEL_HAVE_TO_CONTAIN_REFERENCE_INTERFACE);

                if (!referenceInterface.isAssignableFrom(instance.getClass())) {
                    return null;
                }

                final String unbind = Optional.ofNullable(reference.bind()).filter(s -> !s.isEmpty()).orElse("unbind" + name.get());
                ReflectionUtils.getAllMethods(object.getClass(),
                        methodParameterType(unbind, referenceInterface)).forEach(m -> {
                    callMethod(object, instance).apply(m);
                });
                return null;
            }
        };
    }

    /**
     * Set the reference with the type of references.
     * @param object
     * @param instances
     */
    public static void setReferences(final Object object, final Object... instances) {

        // Processing class @Reference and @References annotations
        for (Object instance : instances) {
            AtomicBoolean found = new AtomicBoolean(false);
            Class<?> clz = object.getClass();
            while (!found.get() && clz != null) {
                // Class references (standard OSGi)
                getAllAnnotations(clz, org.osgi.service.component.annotations.Component.class)
                        .stream().flatMap(referenceIterableFromStandardComponentAnnotation()).forEach(r -> {
                    callBindForStandardReference(object, instance).apply(r);
                });

                // Class references (Felix SCR)
                Stream.concat(getAllAnnotations(clz, org.apache.felix.scr.annotations.References.class).stream()
                        .flatMap(referenceIterableFromFelixReferencesAnnotation()),
                        getAllAnnotations(clz, org.apache.felix.scr.annotations.Reference.class).stream())
                        .forEach(r -> {
                    callBindForFelixReference(object, instance).apply(r);
                });

                // Class references (standard OSGi)
                // Field references (Felix SCR)
                Stream.concat(getFieldsAnnotatedWith(clz, org.osgi.service.component.annotations.Reference.class),
                        getFieldsAnnotatedWith(clz, org.apache.felix.scr.annotations.Reference.class))
                                .filter(f -> f.getType().isAssignableFrom(instance.getClass()))
                        .forEach(f -> {
                            setField(object, instance).apply(f);
                            found.set(true);
                        });

                if (!found.get() && clz.getSuperclass() != null && !clz.getSuperclass().equals(Object.class)) {
                    clz = clz.getSuperclass();
                } else {
                    clz = null;
                }
            }
        }
    }


    private static Set<Class<?>> getActivatorDeactivatorTypes() {
        return ImmutableSet.of(Dictionary.class, Map.class, ComponentContext.class, BundleContext.class);
    }


    @SneakyThrows(ReflectiveOperationException.class)
    private static void deactivate(Class clazz, Object object, Object param) {
        checkNotNull(param, OBJECT_MUST_BE_NON_NULL);
        checkNotNull(param, THE_CONTEXT_MUST_BE_NON_NULL);

        List<Method> deactivators = Stream.concat(
                getMethodsAnnotatedWith(clazz, org.osgi.service.component.annotations.Deactivate.class),
                getMethodsAnnotatedWith(clazz, org.apache.felix.scr.annotations.Deactivate.class)).collect(Collectors.toList());

        checkArgument(deactivators.size() < 2, "The parameter must have zero or one deactivator (@Deactivator annotated) method.");

        if (deactivators.size() == 0 && clazz.getSuperclass() != null) {
            deactivate(clazz.getSuperclass(), object, param);
        } else {
            for (Method deactivator : deactivators) {
                checkArgument(deactivator.getParameterTypes().length == 0
                                || getActivatorDeactivatorTypes().stream().filter(t -> t.isAssignableFrom (deactivator.getParameterTypes()[0])).collect(Collectors.toSet()).size() != 0,
                        "The deactivator (@Deactivator annotated) method must have zero or one parameter.");

                if (deactivator.getParameterTypes().length == 0) {
                    deactivator.invoke(object);
                } else {
                    deactivator.invoke(object, param);
                }
            }
        }
    }


    @SneakyThrows(ReflectiveOperationException.class)
    private static void activate(Class clazz, final Object object, final Object param) {
        checkNotNull(param, OBJECT_MUST_BE_NON_NULL);
        checkNotNull(param, THE_CONTEXT_MUST_BE_NON_NULL);

        List<Method> activators = Stream.concat(
                getMethodsAnnotatedWith(clazz, org.osgi.service.component.annotations.Activate.class),
                getMethodsAnnotatedWith(clazz, org.apache.felix.scr.annotations.Activate.class)).collect(Collectors.toList());

        checkArgument(activators.size() < 2, "The parameter must have zero or one activator (@Activator annotated) method.");

        if (activators.size() == 0 && clazz.getSuperclass() != null) {
            activate(clazz.getSuperclass(), object, param);
        } else {
            for (final Method activator : activators) {
                checkArgument(activator.getParameterTypes().length == 0
                                || getActivatorDeactivatorTypes().stream().filter(t -> t.isAssignableFrom (activator.getParameterTypes()[0])).collect(Collectors.toSet()).size() != 0,
                        "The activator (@Activator annotated) method must have zero or one parameter.");
                boolean saveState = activator.isAccessible();
                activator.setAccessible(true);
                if (activator.getParameterTypes().length == 0) {
                    activator.invoke(object);
                } else {
                    Class<?> activatorParamClass = activator.getParameterTypes()[0];
                    Object convertedParam = param;
                    if (param instanceof Map) {
                        convertedParam = new Hashtable((Map) param);
                    }
                    if (convertedParam instanceof Dictionary && activatorParamClass == ComponentContext.class) {
                        ComponentContext context = mock(ComponentContext.class);
                        when(context.getProperties()).thenReturn((Dictionary) convertedParam);
                        activator.invoke(object, context);
                    } else {
                        activator.invoke(object, convertedParam);
                    }
                }
                activator.setAccessible(saveState);
            }
        }
    }

    /**
     * Returns all fields in the given class which have the given annotation.
     * @param cl Class find with in
     * @param annotation Annotation to find
     * @return Iterator of fields have the annotation
     */

    private static Stream<Field> getFieldsAnnotatedWith(final Class<?> cl, Class<? extends Annotation> annotation) {
        return new Reflections(cl, new FieldAnnotationsScanner(), startWith(cl.getName())).getFieldsAnnotatedWith(annotation).stream();
    }

    /**
     * Returns all methods in the given class which have the given annotation.
     * @param cl Class find with in
     * @param annotation Annotation to find
     * @return Iterator of methods have the annotation
     */
    private static Stream<Method> getMethodsAnnotatedWith(final Class<?> cl, Class<? extends Annotation> annotation) {
        return new Reflections(cl, new MethodAnnotationsScanner(), startWith(cl.getName())).getMethodsAnnotatedWith(annotation).stream();
    }

    /**
     * Returns a String predicate where the given strings matches with the given prefix.
     * @param prefix
     * @param <T>
     * @return
     */
    private static <T> com.google.common.base.Predicate<String> startWith(final String prefix) {
        return new com.google.common.base.Predicate<String>() {
            @Nullable
            @Override
            public boolean apply(@Nullable String input) {
                return input.startsWith(prefix);
            }
        };
    }

    private static <T> com.google.common.base.Predicate<Method> methodParameterType(final String name, final Class<T> cl) {
        return new com.google.common.base.Predicate<Method>() {
            @Nullable
            @Override
            public boolean apply(@Nullable Method input) {
                return input.getName().equals(name) && input.getParameterTypes().length == 1 && input.getParameterTypes()[0].isAssignableFrom(cl);
            }
        };
    }

    /**
     * It returns all instance of given annotation in the given class with all interfaces and super types.
     * The list element in the list is the current class annotation, the order of annotations is the
     * order of inheritance.
     *
     * @param cl Class to scanClass to scan
     * @param annotationClass Annotation class find for
     * @param <T> The type of annotation
     * @return The annotation instances find for or empty if it is not presented
     */
    static <T extends Annotation> List<T> getAllAnnotations(Class<?> cl, Class<T> annotationClass) {
        checkNotNull(cl);
        checkNotNull(annotationClass);
        checkArgument(Annotation.class.isAssignableFrom(annotationClass), "The given annotation class parameter is not an annotation");
        Retention retention = annotationClass.getAnnotation(Retention.class);
        RetentionPolicy retentionPolicy = RetentionPolicy.CLASS;
        if (retention != null) {
            retentionPolicy = retention.value();
        }

        checkArgument(retentionPolicy != RetentionPolicy.SOURCE, "The CLASS and the RUNTIME retention policy is allowed");

        List<T> ret = new ArrayList<>();

        if (retentionPolicy == RetentionPolicy.CLASS) {
            ret.addAll(getTypeAnnotations(cl).filter(a -> annotationClass.isAssignableFrom(a.getClass())).map(a -> annotationClass.cast(a)).collect(Collectors.toSet()));
        } else {
            if (cl.getAnnotation(annotationClass) != null) {
                ret.add(cl.getAnnotation(annotationClass));
            }
        }

        Class<?> superclass = cl.getSuperclass();
        if (superclass != null) {
            ret.addAll(0, getAllAnnotations(superclass, annotationClass));
        }
        Class<?>[] interfaces = cl.getInterfaces();
        for (Class<?> iface : interfaces) {
            ret.addAll(0, getAllAnnotations(iface, annotationClass));
        }
        return ImmutableList.copyOf(ret);
    }

    /**
     * Returns all annotations of the given class.
     * @param cl Class find with in
     * @return Iterator of annotations
     */
    @SneakyThrows({ ClassNotFoundException.class, NotFoundException.class })
    static Stream<Annotation> getTypeAnnotations(final Class<?> cl) {
        ClassPool classPool = ClassPool.getDefault();
        classPool.insertClassPath(new ClassClassPath(cl));
        CtClass ctClass = classPool.get(cl.getName());

        return Stream
                .of(ctClass.getAnnotations())
                .map(a -> (Annotation) a);
    }
}
