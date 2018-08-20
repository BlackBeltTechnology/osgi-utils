package hu.blackbelt.osgi.utils.osgi.api;

import org.osgi.framework.ServiceReference;

/**
 * This interface defines a callback function hich can be called on
 * registration and unregistration by ServiceTrackerManager.
 */
public interface ServiceCallback {
    /**
     * This method gets the service reference which have been called.
     * @param reference
     * @param instance
     */
    void apply(final ServiceReference reference, Object instance);
}
