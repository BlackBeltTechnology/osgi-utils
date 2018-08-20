package hu.blackbelt.osgi.utils.lang.util;

import com.google.common.base.Function;

public final class FunctionUtil {
    private FunctionUtil() {
    }
    
    /**
     * Creates a function which will cast the input to the provided class.
     * 
     * @param resultClass class which will be used for the class 
     * @return function
     */
    public static <T> Function<Object, T> cast(final Class<T> resultClass) {
        return new Function<Object, T>() {
            @Override
            public T apply(Object input) {
                return resultClass.cast(input);
            }
        };
    }
}
