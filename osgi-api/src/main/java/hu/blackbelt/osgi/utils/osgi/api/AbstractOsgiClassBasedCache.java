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

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.SynchronousBundleListener;

import java.util.Map;
import java.util.Set;


/**
 * OSGi class based cache of objects. It tracks bundle lifecycle and evicts object from cache if neccesary.
 * @param <O>
 * @param <C>
 */
public abstract class AbstractOsgiClassBasedCache<C extends Class<?>, O> implements ClassBasedCache<C, O> {

    private Map<C, O> instances = Maps.newHashMap();

    private SynchronousBundleListener synchronousBundleListener;

    @Override
    public O getInstance(C clazz) {
        synchronized (instances) {
            return instances.get(clazz);
        }
    }

    public void addToCache(C clazz, O instance) {
        synchronized (instances) {
            instances.put(clazz, instance);
        }
    }

    public void clear() {
        synchronized (instances) {
            instances.clear();
        }
    }

    public void registerListener(BundleContext bundleContext) {
        synchronousBundleListener = new SynchronousBundleListener() {
            @Override
            public void bundleChanged(BundleEvent event) {
                bundleChangedInternal(event);
            }
        };
        bundleContext.addBundleListener(synchronousBundleListener);
    }

    public void unregisterListener(BundleContext bundleContext) {
        bundleContext.removeBundleListener(synchronousBundleListener);
    }

    public void bundleChangedInternal(BundleEvent event) {

        synchronized (instances) {
            Set<O> instancesToRemove = Sets.newHashSet();
            for (Map.Entry<C, O> entry : instances.entrySet()) {
                if (event.getBundle().getBundleId() == FrameworkUtil.getBundle(entry.getKey()).getBundleId()) {
                    instancesToRemove.add(entry.getValue());
                }
            }
            for (O toRemove : instancesToRemove) {
                instances.remove(toRemove);
            }
        }
    }
}
