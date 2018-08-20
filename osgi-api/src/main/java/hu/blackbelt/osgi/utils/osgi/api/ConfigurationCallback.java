package hu.blackbelt.osgi.utils.osgi.api;

/**
 * This interface defines a callback function which can be called on
 * change on configuration by BundleTrackerManager.
 */
public interface ConfigurationCallback {
    /**
     * This method gets the configuration which have been called.
     * @param configurationInfo
     */
    void accept(final ConfigurationInfo configurationInfo);

    /**
     * This method gets the configuration which have been called.
     * @param configurationInfo
     */
    Thread process(final ConfigurationInfo configurationInfo);
}