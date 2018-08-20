package hu.blackbelt.osgi.utils.lang.util;

import com.google.common.collect.FluentIterable;
import lombok.Getter;
import lombok.Setter;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static hu.blackbelt.osgi.utils.lang.util.ReflectionUtil.getFieldValue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;

public class ReflectionUtilTest {

    @Test
    public void testFindNonStaticFields() throws NoSuchFieldException {
        assertArrayEquals(new Field[]{T1.class.getDeclaredField("test")},
                FluentIterable.from(ReflectionUtil.findNonStaticFields(T1.class)).toArray(Field.class));
    }

    @Test
    public void testFindGetter() throws NoSuchMethodException {
        assertEquals(T1.class.getDeclaredMethod("getTest"), ReflectionUtil.findGetter(T1.class, "test"));
    }

    @Test
    public void testFindStaticFinal() throws NoSuchFieldException {
        assertEquals(T3.class.getDeclaredField("TEST"), ReflectionUtil.findFinalField(T3.class, "TEST"));
    }

    @Test
    public void testFindSetter() throws NoSuchMethodException {
        assertEquals(T1.class.getDeclaredMethod("setTest", F1.class), ReflectionUtil.findSetter(T1.class, "test"));
    }


    @Test
    public void testGetFieldValueRecurivelyEmpty() {
        assertThat(getFieldValue(null, (Iterator) null), nullValue());
        assertThat(getFieldValue(null, Arrays.asList("").iterator()), nullValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetFieldValueRecurivelyNoField() {
        assertThat(getFieldValue(new T1(), Arrays.asList("valami").iterator()), nullValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetFieldValueRecurivelyNoField2() {
        T1 t1 = new T1();
        t1.setTest(new F1());
        assertThat(getFieldValue(t1, Arrays.asList("test", "valami").iterator()), nullValue());
    }

    @Test
    public void testGetFieldValueRecurively() {
        T1 t = new T1();
        assertThat(getFieldValue(t, Arrays.asList("test").iterator()), nullValue());
        F1 f = new F1();
        t.setTest(f);
        assertThat((F1) getFieldValue(t, Arrays.asList("test").iterator()), is(f));

        String s = "aadfads";
        List<Integer> ints = Arrays.asList(1, 2, 3);
        F3 f3 = new F3();
        f3.setS(s);
        f3.setList(ints);
        T4 t4 = new T4();
        t4.setTest(f3);
        assertThat((String) getFieldValue(t4, Arrays.asList("test", "s").iterator()), is(s));
        assertThat((List<Integer>) getFieldValue(t4, Arrays.asList("test", "list").iterator()), is(ints));

    }


    public static class F1 {
    }

    public static class F2 extends F1 {
    }

    @Setter
    @Getter
    public static class F3 extends F1 {
        private String s;
        private List<Integer> list;
    }

    @Setter
    @Getter
    public static class T1 {
        private F1 test;
    }

    @Setter
    @Getter
    public static class T2 {
        private F2 test;
    }

    @Setter
    @Getter
    public static class T4 {
        private F3 test;
    }

    @Setter
    @Getter
    public static class T3 {
        private static final String TEST = "";
    }

}
