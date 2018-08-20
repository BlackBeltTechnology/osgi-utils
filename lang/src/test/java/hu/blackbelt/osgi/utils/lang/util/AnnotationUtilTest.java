package hu.blackbelt.osgi.utils.lang.util;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import org.junit.Test;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

import static hu.blackbelt.osgi.utils.lang.util.AnnotationUtil.getAnnotation;
import static hu.blackbelt.osgi.utils.lang.util.AnnotationUtil.getAllAnnotations;
import static hu.blackbelt.osgi.utils.lang.util.AnnotationUtil.isAnnotationPresent;
import static java.lang.annotation.ElementType.TYPE;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class AnnotationUtilTest {

    @Test(expected = IllegalArgumentException.class)
    public void testGetAllAnnotationsSource() {
        getAllAnnotations(SourceClass.class, SourceAnnotation.class);
    }

    @Test
    public void testGetAllAnnotationsRuntime() {
        List<RuntimeAnnotation> result = getAllAnnotations(RuntimeClass6.class, RuntimeAnnotation.class);
        String[] expected = new String[] { "RuntimeInterface1", "RuntimeInterface2", "RuntimeClass1",
                "RuntimeClass2", "RuntimeClass3", "RuntimeClass4", "RuntimeClass6" };

        assertArrayEquals(expected,
                FluentIterable.from(result).transform(extractRuntimeAnnotationValues()).toList().toArray(new String[result.size()]));

    }

    @Test
    public void testGetAllAnnotationsClass() {
        List<ClassAnnotation> result = getAllAnnotations(ClassClass6.class, ClassAnnotation.class);
        String[] expected = new String[] { "ClassInterface1", "ClassInterface2", "ClassClass1", "ClassClass2",
                "ClassClass3", "ClassClass4", "ClassClass6" };

        assertArrayEquals(expected,
                FluentIterable.from(result).transform(extractClassAnnotationValues()).toList().toArray(new String[result.size()]));
    }

    @Test
    public <A extends Annotation> void testAnnotation() {
        assertEquals(RuntimeClass4.class.getAnnotation(RuntimeAnnotation.class),
                getAnnotation(RuntimeClass4.class, RuntimeAnnotation.class));
        assertEquals(RuntimeInterface2.class.getAnnotation(RuntimeAnnotation.class),
                getAnnotation(RuntimeCLass7.class, RuntimeAnnotation.class));
    }

    @Test
    public void testIsAnnotationPresent() {
        assertEquals(false, isAnnotationPresent(NoAnnotationClass.class, RuntimeAnnotation.class));

        assertEquals(true, isAnnotationPresent(RuntimeClass4.class, RuntimeAnnotation.class));
        assertEquals(true, isAnnotationPresent(RuntimeClass5.class, RuntimeAnnotation.class));
        assertEquals(true, isAnnotationPresent(RuntimeCLass7.class, RuntimeAnnotation.class));

        assertEquals(true, isAnnotationPresent(ClassClass4.class, ClassAnnotation.class));
        assertEquals(true, isAnnotationPresent(ClassClass5.class, ClassAnnotation.class));
        assertEquals(true, isAnnotationPresent(ClassCLass7.class, ClassAnnotation.class));
    }



    private static Function<ClassAnnotation, String> extractClassAnnotationValues() {
        return new Function<ClassAnnotation, String>() {
            @Nullable
            @Override
            public String apply(ClassAnnotation input) {
                return input.value();
            }
        };
    }

    private static Function<RuntimeAnnotation, String> extractRuntimeAnnotationValues() {
        return new Function<RuntimeAnnotation, String>() {
            @Nullable
            @Override
            public String apply(RuntimeAnnotation input) {
                return input.value();
            }
        };
    }


    public static class NoAnnotationClass { }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(value = TYPE)
    public @interface RuntimeAnnotation {
        String value();
    }

    @RuntimeAnnotation("RuntimeInterface1") public interface RuntimeInterface1 { }

    @RuntimeAnnotation("RuntimeInterface2") public interface RuntimeInterface2 extends RuntimeInterface1 { }

    @RuntimeAnnotation("RuntimeClass1") public static class RuntimeClass1 { }

    @RuntimeAnnotation("RuntimeClass2") public static class RuntimeClass2 extends RuntimeClass1 { }

    @RuntimeAnnotation("RuntimeClass3") public static class RuntimeClass3 extends RuntimeClass2 implements RuntimeInterface2 { }

    @RuntimeAnnotation("RuntimeClass4") public static class RuntimeClass4 extends RuntimeClass3 { }

    public static class RuntimeClass5 extends RuntimeClass4 { }

    @RuntimeAnnotation("RuntimeClass6") public static class RuntimeClass6 extends RuntimeClass5 { }

    public static class RuntimeCLass7 implements RuntimeInterface2 { }


    @Retention(RetentionPolicy.CLASS)
    @Target(value = TYPE)
    public @interface ClassAnnotation {
        String value();
    }

    @ClassAnnotation("ClassInterface1") public interface ClassInterface1 { }

    @ClassAnnotation("ClassInterface2") public interface ClassInterface2 extends ClassInterface1 { }

    @ClassAnnotation("ClassClass1") public static class ClassClass1 { }

    @ClassAnnotation("ClassClass2") public static class ClassClass2 extends ClassClass1 { }

    @ClassAnnotation("ClassClass3") public static class ClassClass3 extends ClassClass2 implements ClassInterface2 { }

    @ClassAnnotation("ClassClass4") public static class ClassClass4 extends ClassClass3 { }

    public static class ClassClass5 extends ClassClass4 { }

    @ClassAnnotation("ClassClass6") public static class ClassClass6 extends ClassClass5 { }

    public static class ClassCLass7 implements ClassInterface2 { }

    @Retention(RetentionPolicy.SOURCE)
    @Target(value = TYPE)
    public @interface SourceAnnotation {
        String value();
    }
    @SourceAnnotation("source") public static class SourceClass { }

}
