package hu.blackbelt.osgi.utils.lang.util;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class ConversionUtilTest {
    @Parameterized.Parameter(0)
    public Object from;
    @Parameterized.Parameter(1)
    public Object to;
    @Parameterized.Parameter(2)
    public Class<?> toClass;

    @Parameterized.Parameters(name = "{0} -> {2} = {1}")
    public static Object[][] data() {
        return new Object[][] {
            // null;
            new Object[] { null, null, Integer.class },
            new Object[] { null, null, String.class },
            new Object[] { null, null, ImmutableMap.class },
            // valueOf
            new Object[] { "1", (short) 1, Short.class},
            new Object[] { "1", (int) 1, Integer.class },
            new Object[] { "1", (long) 1, Long.class },
            new Object[] { "1.1", (float) 1.1, Float.class },
            new Object[] { "1.1", (double) 1.1, Double.class },
            new Object[] { "1,1", NumberFormatException.class, Float.class },
            new Object[] { "1,1", NumberFormatException.class, Double.class },
            new Object[] { "true", true, Boolean.class },
            // any to string
            new Object[] { "abc", "abc", String.class },
            new Object[] { (short) 1, "1", String.class },
            new Object[] { (int) 1, "1", String.class },
            new Object[] { (long) 1, "1", String.class },
            new Object[] { (float) 1.1, "1.1", String.class },
            new Object[] { (double) 1.1, "1.1", String.class },
            new Object[] { true, "true", String.class },
            new Object[] { ImmutableMap.of("a", 1, "b", 2), "{a=1, b=2}", String.class },
            new Object[]{"0", new BigDecimal("0"), BigDecimal.class},
            // identical
            new Object[] { (short) 1, (short) 1, Short.class },
            new Object[] { (int) 1, (int) 1, Integer.class },
            new Object[] { (long) 1, (long) 1, Long.class },
            new Object[] { (float) 1.1, (float) 1.1, Float.class },
            new Object[] { (double) 1.1, (double) 1.1, Double.class },
            new Object[] { true, true, Boolean.class },
            // enum
            new Object[] { "enum", IllegalArgumentException.class, TestEnum.class },
            new Object[] { "enum1", TestEnum.enum1, TestEnum.class },
            // not found
            new Object[] { 1, IllegalArgumentException.class, ImmutableMap.class }
        };
    }

    @Test
    public void testConvert() {
        if (to instanceof Class) {
            Class<? extends Throwable> actual = null;
            try {
                ConversionUtil.convert(from, toClass);
            } catch (Exception e) {
                actual = e.getClass();
            }
            assertEquals(to, actual);
        } else {
            assertEquals(to, ConversionUtil.convert(from, toClass));
        }
    }

    private enum TestEnum {
        enum1, enum2
    }
}
