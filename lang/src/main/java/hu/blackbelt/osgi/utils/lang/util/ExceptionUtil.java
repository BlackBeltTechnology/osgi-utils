package hu.blackbelt.osgi.utils.lang.util;

import javax.annotation.Nullable;
import javax.annotation.Nonnull;

public final class ExceptionUtil {

    private ExceptionUtil() {
    }

    /**
     * If the last argument is instance of {@link Throwable} then returns the remaining arguments.
     * 
     * @param args arguments
     * @return arguments or arguments but the last one
     */
    @Nonnull public static Object[] retainArguments(Object... args) {
        if (args == null) {
            return null;
        }
        if (args.length > 0 && args[args.length - 1] instanceof Throwable) {
            Object[] result = new Object[args.length - 1];
            System.arraycopy(args, 0, result, 0, args.length - 1);
            return result;
        } else {
            return args;
        }
    }
    
    /**
     * If the last argument is instance of {@link Throwable} then returns that argument.
     * 
     * @param args arguments
     * @return Throwable or null
     */
    @Nullable public static Throwable retainThrowable(Object... args) {
        if (args == null) {
            return null;
        }
        if (args.length > 0 && args[args.length - 1] instanceof Throwable) {
            return (Throwable) args[args.length - 1];
        } else {
            return null;
        }
    }


}
