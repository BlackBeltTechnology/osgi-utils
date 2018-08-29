package hu.blackbelt.osgi.utils.test;

import com.google.common.base.Function;
import lombok.SneakyThrows;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Bean functions to work with an instance of object
 */
public class BeanUtil {

    private BeanUtil() {
    }

    /**
     * Call a method in an object and returns with the result.
     * @param target The bean object where method is called
     * @param instance The method parameters called on method
     * @return
     */
    public static Function<Method, Object> callMethod(final Object target, final Object... instance) {
        return new Function<Method, Object>() {
            @Nullable
            @Override
            @SneakyThrows(ReflectiveOperationException.class)
            public Object apply(@Nullable Method input) {
                return input.invoke(target, instance);
            }
        };
    }

    /**
     * Sets the field value of the given target with the given instance.
     * @param target
     * @param instance
     * @return
     */
    public static Function<Field, Void> setField(final Object target, final Object instance) {
        return new Function<Field, Void>() {
            @Nullable
            @Override
            @SneakyThrows(IllegalAccessException.class)
            public Void apply(@Nullable Field input) {
                boolean acc = input.isAccessible();
                input.setAccessible(true);
                input.set(target, instance);
                input.setAccessible(acc);
                return null;
            }
        };
    }
}
