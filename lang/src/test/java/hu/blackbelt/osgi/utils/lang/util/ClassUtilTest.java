package hu.blackbelt.osgi.utils.lang.util;

import com.google.common.collect.ImmutableSet;
import lombok.Getter;
import lombok.Setter;
import org.junit.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.ImmutableList.of;
import static hu.blackbelt.osgi.utils.lang.util.ClassUtil.getFieldsAnnotatedWith;
import static hu.blackbelt.osgi.utils.lang.util.ClassUtil.getMethodsAnnotatedWith;
import static hu.blackbelt.osgi.utils.lang.util.ClassUtil.getTypeAnnotations;
import static hu.blackbelt.osgi.utils.lang.util.ClassUtil.getTypesAnnotatedWith;
import static hu.blackbelt.osgi.utils.lang.util.ClassUtil.isAnnotatedWith;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ClassUtilTest {


    @Test
    public void testGetTypesAnnotatedWith() {
        assertEquals(ImmutableSet.of(T2.class, T3.class), ImmutableSet.copyOf(getTypesAnnotatedWith(T2.class.getPackage().getName(), A.class)));
        assertTrue(isAnnotatedWith(T2.class, A.class));
        assertEquals(ImmutableSet.of(T2.class, T3.class), ImmutableSet.copyOf(getTypesAnnotatedWith(T3.class.getPackage().getName(), A.class)));
        assertTrue(isAnnotatedWith(T3.class, A.class));
    }

    @Test
    public void testGetFieldsAnnotatedWith() throws NoSuchFieldException {
        assertArrayEquals(of(T2.class.getDeclaredField("test")).toArray(),
                copyOf(getFieldsAnnotatedWith(T2.class, A2.class)).toArray());
        assertArrayEquals(of(T3.class.getDeclaredField("test")).toArray(),
                copyOf(getFieldsAnnotatedWith(T3.class, A2.class)).toArray());
    }

    @Test
    public void testGetMethodsAnnotatedWith() throws NoSuchMethodException {
        assertArrayEquals(of(T2.class.getDeclaredMethod("testMethod")).toArray(),
                copyOf(getMethodsAnnotatedWith(T2.class, A3.class)).toArray());
        assertArrayEquals(of(T3.class.getDeclaredMethod("testMethod")).toArray(),
                copyOf(getMethodsAnnotatedWith(T3.class, A3.class)).toArray());
    }

    @Test
    public void testGetTypeAnnotations() throws NoSuchMethodException {
        assertTrue(copyOf(getTypeAnnotations(T2.class)).toArray()[0] instanceof A);
    }


    @Retention(RetentionPolicy.CLASS)
    @Target(value = TYPE)
    public @interface A { }

    @Retention(RetentionPolicy.CLASS)
    @Target(value = FIELD)
    public @interface A2 { }

    @Retention(RetentionPolicy.CLASS)
    @Target(value = METHOD)
    public @interface A3 { }

    public static class F1 { }

    public static class F2 extends F1 { }

    @Setter
    @Getter
    public static class T1 { private F1 test; }

    @A @Setter @Getter public static class T2 { @A2 public F2 test; @A3 public void testMethod() { }; }

    @A @Setter @Getter public static class T3 { @A2 private F2 test; @A3 private void testMethod() { }; }

}
