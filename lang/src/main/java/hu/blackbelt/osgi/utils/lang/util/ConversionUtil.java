package hu.blackbelt.osgi.utils.lang.util;

import com.google.common.collect.ImmutableList;

import java.math.BigDecimal;

import static java.lang.String.format;

/**
 * Conversion between basic types.
 */
public final class ConversionUtil {
    private static final ImmutableList<Converter> CONVERTERS = ImmutableList.<Converter>of(
            StringToValueOfConverter.INSTANCE,
            AnyToStringConverter.INSTANCE,
            EnumConverter.INSTANCE,
            IdenticalConverter.INSTANCE
    );

    private ConversionUtil() {
    }

    /**
     * <p>
     * Converts between basic types.
     * </p>
     * <p>
     * Supported conversions:
     * </p>
     * <ul>
     * <li>String to Short, Integer, Long, Float, Double, Boolean</li>
     * <li>Anything to String</li>
     * <li>Identical</li>
     * </ul>
     *
     * @param input       conversion unput
     * @param resultClass conversion result
     * @param <T>         conversion result type
     * @return converted input
     */
    public static <T> T convert(Object input, Class<T> resultClass) {
        if (input == null) {
            return null;
        }
        Class<?> inputClass = input.getClass();
        if (resultClass.isAssignableFrom(inputClass)) {
            return (T) input;
        }
        for (Converter converter : CONVERTERS) {
            if (converter.isSupported(inputClass, resultClass)) {
                return converter.convert(input, resultClass);
            }
        }
        throw new IllegalArgumentException(format("The input/result class (%s/%s) is not supported.",
                inputClass.getName(), resultClass.getName()));
    }

    private interface Converter {
        boolean isSupported(Class<?> inputClass, Class<?> resultClass);

        <T> T convert(Object input, Class<?> resultClass);
    }

    private enum EnumConverter implements Converter {
        INSTANCE;

        @Override
        public boolean isSupported(Class<?> inputClass, Class<?> resultClass) {
            return inputClass == String.class && resultClass.isEnum();
        }

        @Override
        public <T> T convert(Object input, Class<?> resultClass) {
            return (T) Enum.valueOf((Class<Enum>) resultClass, input.toString());
        }
    }

    private enum StringToValueOfConverter implements Converter {
        INSTANCE;

        @Override
        public boolean isSupported(Class<?> inputClass, Class<?> resultClass) {
            return inputClass == String.class
                    && (resultClass == Integer.class || resultClass == Long.class || resultClass == Short.class
                    || resultClass == Float.class || resultClass == Double.class || resultClass == BigDecimal.class
                    || resultClass == Boolean.class);
        }

        @Override
        public <T> T convert(Object input, Class<?> resultClass) {
            String str = (String) input;
            if (resultClass == Integer.class) {
                return (T) Integer.valueOf(str);
            }
            if (resultClass == Long.class) {
                return (T) Long.valueOf(str);
            }
            if (resultClass == Short.class) {
                return (T) Short.valueOf(str);
            }
            if (resultClass == Float.class) {
                return (T) Float.valueOf(str);
            }
            if (resultClass == Double.class) {
                return (T) Double.valueOf(str);
            }
            if (resultClass == Boolean.class) {
                return (T) Boolean.valueOf(str);
            }
            if (resultClass == BigDecimal.class) {
                return (T) new BigDecimal(str);
            }
            throw new IllegalArgumentException(format("Unsupported result class %s", resultClass.getName()));
        }
    }

    public enum AnyToStringConverter implements Converter {
        INSTANCE;

        @Override
        public boolean isSupported(Class<?> inputClass, Class<?> resultClass) {
            return resultClass == String.class;
        }

        @Override
        public <T> T convert(Object input, Class<?> resultClass) {
            return (T) input.toString();
        }
    }

    public enum IdenticalConverter implements Converter {
        INSTANCE;

        @Override
        public boolean isSupported(Class<?> inputClass, Class<?> resultClass) {
            return resultClass.isAssignableFrom(inputClass);
        }

        @Override
        public <T> T convert(Object input, Class<?> resultClass) {
            return (T) input;
        }
    }

}
