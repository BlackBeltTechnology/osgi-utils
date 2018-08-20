package hu.blackbelt.osgi.utils.osgi.api;

import org.osgi.framework.Bundle;

/**
 * This interface defines a callback function which can be called on
 * registration and unregistration by BundleTrackerManager.
 */
public interface BundleCallback {
    /**
     * This method gets the bundle which have been called.
     * @param bundle
     */
    void accept(final Bundle bundle);

    Thread process(final Bundle bundle);
}
