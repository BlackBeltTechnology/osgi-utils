package hu.blackbelt.osgi.utils.lang.util;

import com.google.common.reflect.TypeToken;

/**
 * <p>
 * Allows easy access to the generic type parameters.
 * </p>
 * <p>
 * Examples are provided per method basis.
 * </p>
 */
public final class GenericUtil {

    private GenericUtil() {
    }

    /**
     * Resolves the first generic argument.
     * 
     * <pre class="code"><code class="java">
     * public interface Service&lt;T&gt; {}
     * public interface UserService&lt;User&gt; {}
     * GenericUtil.resolveType(UserService.class, Service.class); // returns User
     * </code></pre>
     * 
     * @param cl class which defines the generic argument
     * @param declaringClass class which declares the generic argument
     * @return resolved generic class
     */
    public static Class<?> resolveType(Class<?> cl, Class<?> declaringClass) {
        return resolveType(cl, declaringClass, 0);
    }

    /**
     * Resolves the generic argument at the provided index. Indexing starts from 0.
     *  
     * <pre>
     * {@code
     * public interface Chain&lt;I, O&gt; {}
     * public interface FileToStreamchain&lt;File, Stream&gt; {}
     * GenericUtil.resolveType(FileToStreamchain.class, Chain.class, 0); // returns File
     * GenericUtil.resolveType(FileToStreamchain.class, Chain.class, 1); // returns Stream
     * </pre>
     * 
     * @param cl class which defines the generic argument
     * @param declaringClass class which declares the generic argument
     * @param genericsIndex generic argument index
     * @return resolved generic class
     */
    public static Class<?> resolveType(Class<?> cl, Class<?> declaringClass, int genericsIndex) {
        return TypeToken.of(cl).resolveType(declaringClass.getTypeParameters()[genericsIndex]).getRawType();
    }

    /**
     * Resolve the real implemetation class of anchestor class.
     * @param cl
     * @param declaringClass
     * @return
     */
    public static Class<?> resolveImplementationOf(Class<?> cl, Class<?> declaringClass) {
        Class<?> f = null;
        for (Class iface : cl.getInterfaces()) {
            if (declaringClass.isAssignableFrom(iface)) {
                f = iface;
                break;
            }
        }
        return f;
    }
}
