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

import org.osgi.framework.Bundle;

import java.util.function.Predicate;

/**
 * It is a functional helper class for SynchrounusBundleListener. When this service starts scan all existing bundles and calls all defined
 * register callbacks for all existing instance, and after any register/unregister called it calls the callbacks.
 * It is not filtering any bundle, so the callback have to determinate that
 * the bundle is suitable or not for it.
 *
 */
public interface BundleTrackerManager {


    /**
     * Register callbacks for bundles. In this method call the registerCallback is called for every bundle first, and after on any registration.
     * The unregister callback is called when a bundle is unregistered on tracker.
     * @param key It is a disciriminator where the callback related to.
     * @param registerCallback The callback which is called any service registration
     * @param unregisterCallback The callback which is called any service unregistration
     * @param filter Filtering predicate for ServiceReference.
     */
    void registerBundleCallback(Object key, BundleCallback registerCallback, BundleCallback unregisterCallback,
                                 Predicate<Bundle> filter);

    /**
     * Register callbacks for bundles. In this method call the registerCallback is called for every bundle first, and after on any registration.
     * The unregister callback is called when a bundle is unregistered on tracker.
     * @param key It is a disciriminator where the callback related to.
     * @param registerCallback The callback which is called any service registration
     * @param unregisterCallback The callback which is called any service unregistration
     */
    void registerBundleCallback(Object key, BundleCallback registerCallback, BundleCallback unregisterCallback);

    /**
     * Unrregister callbacks for services. In this method call the unregisterCallback for every service and no longer calls the registered callbacks.
     * @param key It is a disciriminator where the callback related to.
     */
    void unregisterBundleCallback(Object key);
}
