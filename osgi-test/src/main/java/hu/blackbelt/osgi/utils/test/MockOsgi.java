package hu.blackbelt.osgi.utils.test;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
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

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Optional.presentInstances;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.emptyToNull;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.ImmutableList.of;
import static com.google.common.collect.Iterables.concat;
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

    public static Function<org.apache.felix.scr.annotations.References, Iterable<org.apache.felix.scr.annotations.Reference>> referenceIterableFromFelixReferencesAnnotation() {
        return new Function<org.apache.felix.scr.annotations.References, Iterable<org.apache.felix.scr.annotations.Reference>>() {
            @Nullable
            @Override
            public Iterable<org.apache.felix.scr.annotations.Reference> apply(@Nullable org.apache.felix.scr.annotations.References input) {
                return from(copyOf(input.value()));
            }
        };
    }


    public static Function<org.apache.felix.scr.annotations.Reference, Void> callBindForFelixReference(final Object object, final Object instance) {
        return new Function<org.apache.felix.scr.annotations.Reference, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable org.apache.felix.scr.annotations.Reference reference) {
                final String name = fromNullable(emptyToNull(reference.name())).get();
                checkNotNull(emptyToNull(name), REFERENCE_IN_CLASS_LEVEL_HAVE_TO_CONTAIN_NAME);

                final Class<?> referenceInterface = reference.referenceInterface();
                checkNotNull(referenceInterface, REFERENCE_IN_CLASS_LEVEL_HAVE_TO_CONTAIN_REFERENCE_INTERFACE);

                if (!referenceInterface.isAssignableFrom(instance.getClass())) {
                    return null;
                }

                final String bind = fromNullable(emptyToNull(reference.bind())).or("bind" + name);
                for (Method m : from(ReflectionUtils.getAllMethods(object.getClass(),
                        methodParameterType(bind, referenceInterface))).toList()) {
                    callMethod(object, instance).apply(m);
                }
                return null;
            }
        };
    }

    public static Function<org.apache.felix.scr.annotations.Reference, Void> callUnbindForFelixReference(final Object object, final Object instance) {
        return new Function<org.apache.felix.scr.annotations.Reference, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable org.apache.felix.scr.annotations.Reference reference) {
                final String name = fromNullable(emptyToNull(reference.name())).get();
                checkNotNull(emptyToNull(name), REFERENCE_IN_CLASS_LEVEL_HAVE_TO_CONTAIN_NAME);

                final Class<?> referenceInterface = reference.referenceInterface();
                checkNotNull(referenceInterface, REFERENCE_IN_CLASS_LEVEL_HAVE_TO_CONTAIN_REFERENCE_INTERFACE);

                if (!referenceInterface.isAssignableFrom(instance.getClass())) {
                    return null;
                }

                final String unbind = fromNullable(emptyToNull(reference.bind())).or("unbind" + name);
                for (Method m : from(ReflectionUtils.getAllMethods(object.getClass(),
                        methodParameterType(unbind, referenceInterface))).toList()) {
                    callMethod(object, instance).apply(m);
                }
                return null;
            }
        };
    }

    public static Function<org.osgi.service.component.annotations.Component, Iterable<org.osgi.service.component.annotations.Reference>> referenceIterableFromStandardComponentAnnotation() {
        return new Function<org.osgi.service.component.annotations.Component, Iterable<org.osgi.service.component.annotations.Reference>>() {
            @Nullable
            @Override
            public Iterable<org.osgi.service.component.annotations.Reference> apply(@Nullable org.osgi.service.component.annotations.Component input) {
                return from(copyOf(input.reference()));
            }
        };
    }


    public static Function<org.osgi.service.component.annotations.Reference, Void> callBindForStandardReference(final Object object, final Object instance) {
        return new Function<org.osgi.service.component.annotations.Reference, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable org.osgi.service.component.annotations.Reference reference) {
                final String name = fromNullable(emptyToNull(reference.name())).get();
                checkNotNull(emptyToNull(name), REFERENCE_IN_CLASS_LEVEL_HAVE_TO_CONTAIN_NAME);

                final Class<?> referenceInterface = reference.service();
                checkNotNull(referenceInterface, REFERENCE_IN_CLASS_LEVEL_HAVE_TO_CONTAIN_REFERENCE_INTERFACE);

                if (!referenceInterface.isAssignableFrom(instance.getClass())) {
                    return null;
                }

                final String bind = fromNullable(emptyToNull(reference.bind())).or("bind" + name);
                for (Method m : from(ReflectionUtils.getAllMethods(object.getClass(),
                        methodParameterType(bind, referenceInterface))).toList()) {
                    callMethod(object, instance).apply(m);
                }
                return null;
            }
        };
    }

    public static Function<org.osgi.service.component.annotations.Reference, Void> callUnbindForStandardReference(final Object object, final Object instance) {
        return new Function<org.osgi.service.component.annotations.Reference, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable org.osgi.service.component.annotations.Reference reference) {
                final String name = fromNullable(emptyToNull(reference.name())).get();
                checkNotNull(emptyToNull(name), REFERENCE_IN_CLASS_LEVEL_HAVE_TO_CONTAIN_NAME);

                final Class<?> referenceInterface = reference.service();
                checkNotNull(referenceInterface, REFERENCE_IN_CLASS_LEVEL_HAVE_TO_CONTAIN_REFERENCE_INTERFACE);

                if (!referenceInterface.isAssignableFrom(instance.getClass())) {
                    return null;
                }

                final String unbind = fromNullable(emptyToNull(reference.bind())).or("unbind" + name);
                for (Method m : from(ReflectionUtils.getAllMethods(object.getClass(),
                        methodParameterType(unbind, referenceInterface))).toList()) {
                    callMethod(object, instance).apply(m);
                }
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
            boolean found = false;
            Class<?> clz = object.getClass();
            while (!found && clz != null) {
                // Class references (standard OSGi)
                for (org.osgi.service.component.annotations.Reference reference : from(concat(
                        from(getAllAnnotations(clz, org.osgi.service.component.annotations.Component.class))
                                .transformAndConcat(referenceIterableFromStandardComponentAnnotation())))) {
                    callBindForStandardReference(object, instance).apply(reference);
                    found = true;
                }
                // Class references (Felix SCR)
                for (org.apache.felix.scr.annotations.Reference reference : from(concat(
                        from(getAllAnnotations(clz, org.apache.felix.scr.annotations.Reference.class)),
                        from(getAllAnnotations(clz, org.apache.felix.scr.annotations.References.class))
                                .transformAndConcat(referenceIterableFromFelixReferencesAnnotation())))) {
                    callBindForFelixReference(object, instance).apply(reference);
                    found = true;
                }

                // Class references (standard OSGi)
                for (Field f : getFieldsAnnotatedWith(clz, org.osgi.service.component.annotations.Reference.class)) {
                    if (f.getType().isAssignableFrom(instance.getClass())) {
                        setField(object, instance).apply(f);
                        found = true;
                    }
                }
                // Field references (Felix SCR)
                for (Field f : getFieldsAnnotatedWith(clz, org.apache.felix.scr.annotations.Reference.class)) {
                    if (f.getType().isAssignableFrom(instance.getClass())) {
                        setField(object, instance).apply(f);
                        found = true;
                    }
                }

                if (!found && clz.getSuperclass() != null && !clz.getSuperclass().equals(Object.class)) {
                    clz = clz.getSuperclass();
                } else {
                    clz = null;
                }
            }
        }
    }


    private static FluentIterable<Class<?>> getActivatorDeactivatorTypes() {
        return from(of(Dictionary.class, Map.class, ComponentContext.class, BundleContext.class));
    }


    @SneakyThrows(ReflectiveOperationException.class)
    private static void deactivate(Class clazz, Object object, Object param) {
        checkNotNull(param, OBJECT_MUST_BE_NON_NULL);
        checkNotNull(param, THE_CONTEXT_MUST_BE_NON_NULL);

        List<Method> deactivators = copyOf(concat(
                getMethodsAnnotatedWith(clazz, org.osgi.service.component.annotations.Deactivate.class),
                getMethodsAnnotatedWith(clazz, org.apache.felix.scr.annotations.Deactivate.class)));

        checkArgument(deactivators.size() < 2, "The parameter must have zero or one deactivator (@Deactivator annotated) method.");

        if (deactivators.size() == 0 && clazz.getSuperclass() != null) {
            deactivate(clazz.getSuperclass(), object, param);
        } else {
            for (Method deactivator : deactivators) {
                checkArgument(deactivator.getParameterTypes().length == 0
                                || !getActivatorDeactivatorTypes().filter(Predicates.assignableFrom(deactivator.getParameterTypes()[0])).isEmpty(),
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

        List<Method> activators = copyOf(concat(
                getMethodsAnnotatedWith(clazz, org.osgi.service.component.annotations.Activate.class),
                getMethodsAnnotatedWith(clazz, org.apache.felix.scr.annotations.Activate.class)));

        checkArgument(activators.size() < 2, "The parameter must have zero or one activator (@Activator annotated) method.");

        if (activators.size() == 0 && clazz.getSuperclass() != null) {
            activate(clazz.getSuperclass(), object, param);
        } else {
            for (final Method activator : activators) {
                checkArgument(activator.getParameterTypes().length == 0
                                || !getActivatorDeactivatorTypes().filter(Predicates.assignableFrom(activator.getParameterTypes()[0])).isEmpty(),
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

    private static Iterable<Field> getFieldsAnnotatedWith(final Class<?> cl, Class<? extends Annotation> annotation) {
        return from(new Reflections(cl, new FieldAnnotationsScanner(), startWith(cl.getName())).getFieldsAnnotatedWith(annotation));
    }

    /**
     * Returns all methods in the given class which have the given annotation.
     * @param cl Class find with in
     * @param annotation Annotation to find
     * @return Iterator of methods have the annotation
     */
    private static Iterable<Method> getMethodsAnnotatedWith(final Class<?> cl, Class<? extends Annotation> annotation) {
        return from(new Reflections(cl, new MethodAnnotationsScanner(), startWith(cl.getName())).getMethodsAnnotatedWith(annotation));
    }

    /**
     * Returns a String predicate where the given strings matches with the given prefix.
     * @param prefix
     * @param <T>
     * @return
     */
    private static <T> Predicate<String> startWith(final String prefix) {
        return new Predicate<String>() {
            @Nullable
            @Override
            public boolean apply(@Nullable String input) {
                return input.startsWith(prefix);
            }
        };
    }

    private static <T> Predicate<Method> methodParameterType(final String name, final Class<T> cl) {
        return new Predicate<Method>() {
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

        Iterable<T> annotation;
        if (retentionPolicy == RetentionPolicy.CLASS) {
            annotation = FluentIterable.from(getTypeAnnotations(cl)).filter(annotationClass);
        } else {
            annotation = presentInstances(of(fromNullable(cl.getAnnotation(annotationClass))));
        }

        if (annotation.iterator().hasNext()) {
            ret.addAll(from(annotation).toList());
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
    static Iterable<Annotation> getTypeAnnotations(final Class<?> cl) {
        ClassPool classPool = ClassPool.getDefault();
        classPool.insertClassPath(new ClassClassPath(cl));
        CtClass ctClass = classPool.get(cl.getName());

        return FluentIterable
                .of(ctClass.getAnnotations())
                .transform(cast(Annotation.class));
    }

    /**
     * Creates a function which will cast the input to the provided class.
     *
     * @param resultClass class which will be used for the class
     * @return function
     */
    static <T> Function<Object, T> cast(final Class<T> resultClass) {
        return new Function<Object, T>() {
            @Override
            public T apply(Object input) {
                return resultClass.cast(input);
            }
        };
    }
}
