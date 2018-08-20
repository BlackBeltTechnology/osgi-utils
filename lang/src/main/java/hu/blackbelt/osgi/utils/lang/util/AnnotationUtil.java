package hu.blackbelt.osgi.utils.lang.util;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Optional.presentInstances;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.ImmutableList.of;


/**
 * Annotation utils to handle annotations over supertypes and interfaces.
 * It handles the CLASS or RUNTIME retention policy. When retention policy
 * is CLASS it uses ClassUtil to retrieve information via JavaAssist.
 */
public final class AnnotationUtil {
    private AnnotationUtil() {
    }

    /**
     * Checks for the annotation on the class, superclasses and interfaces.
     *
     * @param cl Class to scan
     * @param annotationClass Annotation class find for
     * @return If annotation is presented in class or any superclass or iterface
     */
    public static boolean isAnnotationPresent(Class<?> cl, Class<? extends Annotation> annotationClass) {
        return getAnnotation(cl, annotationClass) != null;
    }

    /**
     * Get the last version of annotation from type and supertpes.
     *
     * @param cl Class to scan
     * @param annotationClass Annotation class find for
     * @param <T> The type of annotation
     * @return The annotation instance find for or null if it is not presented
     */
    public static <T extends Annotation> T getAnnotation(Class<?> cl, Class<T> annotationClass) {
        return from(getAllAnnotations(cl, annotationClass)).last().orNull();
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
    public static <T extends Annotation> List<T> getAllAnnotations(Class<?> cl, Class<T> annotationClass) {
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
            annotation = FluentIterable.from(ClassUtil.getTypeAnnotations(cl)).filter(annotationClass);
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

}
