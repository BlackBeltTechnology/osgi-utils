package hu.blackbelt.osgi.utils.lang.util;

import static com.google.common.collect.FluentIterable.from;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;

import com.google.common.base.Predicate;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import lombok.SneakyThrows;

import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.FluentIterable;

import javax.annotation.Nullable;

/**
 * Class util based on Reflections Util (which uses Javassist under).
 * It can handle annotations which have CLASS retention policy. The java reflaction
 * API can handle RUNTIME retention policy only.
 */
public final class ClassUtil {
    private static final LoadingCache<String, Reflections> REFLECTIONS_CACHE_BY_PACKAGE = CacheBuilder.newBuilder()
            .build(new CacheLoader<String, Reflections>() {

                @Override
                public Reflections load(String key) throws Exception {
                    return new Reflections(key);
                }
            });

    private ClassUtil() {
    }

    /**
     * Search for all class which have annotated with the given annotation under the given package.
     *
     * @param packageName Class package find for
     * @param annotation Annotation to find
     * @return Iterable of founded classes
     */
    @SneakyThrows(ExecutionException.class)
    public static Iterable<Class<?>> getTypesAnnotatedWith(String packageName, Class<? extends Annotation> annotation) {
        return REFLECTIONS_CACHE_BY_PACKAGE.get(packageName).getTypesAnnotatedWith(annotation);
    }

    /**
     * Return if the given class have the given annotation.
     *
     * @param cl Class find with in
     * @param annotation Annotation to find
     * @return If the class have the annotation return true
     */
    public static boolean isAnnotatedWith(final Class<?> cl, Class<? extends Annotation> annotation) {
        return !from(new Reflections(cl, new TypeAnnotationsScanner(), new SubTypesScanner(), 
                startWith(cl.getName())).getTypesAnnotatedWith(annotation)).isEmpty();
    }

    /**
     * Returns all methods in the given class which have the given annotation.
     * @param cl Class find with in
     * @param annotation Annotation to find
     * @return Iterator of methods have the annotation
     */
    public static Iterable<Method> getMethodsAnnotatedWith(final Class<?> cl, Class<? extends Annotation> annotation) {
        return from(new Reflections(cl, new MethodAnnotationsScanner(), startWith(cl.getName())).getMethodsAnnotatedWith(annotation));
    }

    /**
     * Returns all fields in the given class which have the given annotation.
     * @param cl Class find with in
     * @param annotation Annotation to find
     * @return Iterator of fields have the annotation
     */

    public static Iterable<Field> getFieldsAnnotatedWith(final Class<?> cl, Class<? extends Annotation> annotation) {
        return from(new Reflections(cl, new FieldAnnotationsScanner(), startWith(cl.getName())).getFieldsAnnotatedWith(annotation));
    }

    /**
     * Returns all annotations of the given class.
     * @param cl Class find with in
     * @return Iterator of annotations
     */
    @SneakyThrows({ ClassNotFoundException.class, NotFoundException.class })
    public static Iterable<Annotation> getTypeAnnotations(final Class<?> cl) {
        ClassPool classPool = ClassPool.getDefault();
        classPool.insertClassPath(new ClassClassPath(cl));
        CtClass ctClass = classPool.get(cl.getName());
        
        return FluentIterable
                .of(ctClass.getAnnotations())
                .transform(FunctionUtil.cast(Annotation.class));
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
}

