package hu.blackbelt.osgi.utils.osgi.api;

/*-
 * #%L
 * OSGi utils API
 * %%
 * Copyright (C) 2018 - 2022 BlackBelt Technology
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
