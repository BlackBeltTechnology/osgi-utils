package hu.blackbelt.osgi.utils.internal.impl;

/*-
 * #%L
 * OSGI utils implementation
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
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

public final class OsgiUtil {
    private OsgiUtil() {
    }

    /**
     * Retrieves a service implementation from outside of the OSGI container.
     * Basically it is the same as the {@link org.osgi.service.component.annotations.Reference} in a plain OSGI service.
     * <p/>
     * Use it ONLY if there is no other solution!!!!
     *
     * @param serviceClass the service interface (must not be null)
     * @param <T>          the type of the service interface
     * @return the service interface implementation
     * @throws IllegalArgumentException if the provided class is null
     * @throws RuntimeException         if no bundle/bundleConext/implementation found for this interface
     */
    public static <T> T getServiceFromOsgi(Class<T> serviceClass) {
        if (serviceClass == null) {
            throw new IllegalArgumentException("Provided service class can not be null!");
        }
        Bundle bundle = FrameworkUtil.getBundle(serviceClass);
        if (bundle == null) {
            throw new RuntimeException("No bundle defined class: " + serviceClass);
        }

        BundleContext ctx = bundle.getBundleContext();
        if (ctx == null) {
            throw new RuntimeException("No valid BundleContext for class: " + serviceClass);
        }

        ServiceReference<T> serviceRef = ctx.getServiceReference(serviceClass);
        if (serviceRef == null) {
            throw new RuntimeException("No service implementation for: " + serviceClass);
        }

        return (T) ctx.getService(serviceRef);
    }

}
