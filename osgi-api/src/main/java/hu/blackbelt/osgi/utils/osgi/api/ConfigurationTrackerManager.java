package hu.blackbelt.osgi.utils.osgi.api;

import java.util.function.Predicate;

/**
 * It is a functional helper class for ConfigurationListener. When this service starts scan all existing configurations and calls all defined
 * register callbacks for all existing instance, and after any configuration change called it calls the callbacks.
 * It is not filtering any configuration, so the callback have to determinate that
 * the configuration is suitable or not for it.
 *
 */
public interface ConfigurationTrackerManager {

    /**
     * Create callbacks for configurations. In this method call the createCallback and updateCallback is called for every configuration first, and after on any create, update or delete event.
     * The delete callback is called when a configuration is unregistered on tracker.
     * @param key It is a disciriminator where the callback related to.
     * @param createCallback The callback which is called any configuration creation
     * @param updateCallback The callback which is called any configuration update or location update
     * @param deleteCallback The callback which is called any configuration delete
     * @param filter Filter the Configuration accept for
     */
    void registerConfigurationCallback(Object key, ConfigurationCallback createCallback, ConfigurationCallback updateCallback, ConfigurationCallback deleteCallback, Predicate<ConfigurationInfo> filter);

    /**
     * Unrregister callbacks for services. In this method call the unregisterCallback for every service and no longer calls the registered callbacks.
     * @param key It is a disciriminator where the callback related to.
     */
    void unregisterConfigurationCallback(Object key);
}