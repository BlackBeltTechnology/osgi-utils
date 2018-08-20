package hu.blackbelt.osgi.utils.osgi.api;


/**
 * It provides intance of class by class.
 * @param <O>
 * @param <C>
 */
public interface ClassBasedCache<C extends Class<?>, O> {
    /**
     * It returns the instance if found, otherwise returns null.
     * @param clazz
     * @return
     */
    O getInstance(C clazz);
}
