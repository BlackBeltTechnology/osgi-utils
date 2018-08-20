package hu.blackbelt.osgi.utils.lang;

import javax.annotation.Nonnull;

/**
 * Represents an object which have a unique name.
 * The name must be unique in some context but not necessary everywhere 
 * as it is not a (global) unique identifier.  
 */
public interface Named {
    /**
     * The name of the object.
     * This method must return a non-null not empty {@link String} value.
     * 
     * @return the name
     */
    @Nonnull String getName();
}
