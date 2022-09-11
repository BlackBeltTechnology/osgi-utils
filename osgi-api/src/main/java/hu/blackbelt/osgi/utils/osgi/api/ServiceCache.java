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

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import java.util.Map;
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class ServiceCache<O> {

    public static final String PROP_CLASS = "class";
    public static final String CLASS_HAVE_TO_BE_DEFINED = "Class have to be defined";
    private final Map<Class, NavigableMap<Integer, O>> serviceMap = Maps.newConcurrentMap();
    private final Map<ServiceReference, O> instanceCache = Maps.newConcurrentMap();
    private final Map<ServiceReference, Map> propertiesCache = Maps.newConcurrentMap();

    private Function<Object, Class> clazzInspector;

    public ServiceCache(Function<Object, Class> clazzInspector) {
        this.clazzInspector = clazzInspector;
    }

    public O find(Class input) {
        checkNotNull(input, CLASS_HAVE_TO_BE_DEFINED);
        NavigableMap<Integer, O> serviceInstances = serviceMap.get(input);
        if (serviceInstances == null || serviceInstances.size() == 0) {
            return null;
        }
        return serviceInstances.lastEntry().getValue();
    }

    public O find(String input) {
        checkNotNull(input, CLASS_HAVE_TO_BE_DEFINED);
        for (Class clazz : serviceMap.keySet()) {
            if (clazz.getName().equals(input)) {
                return find(clazz);
            }
        }
        return null;
    }

    public void bindService(ServiceReference serviceReference, O instance) {
        Class clazz = (Class) clazzInspector.apply(instance);
        if (clazz == null) {
            return;
        }
        instanceCache.put(serviceReference, instance);
        propertiesCache.put(serviceReference, ImmutableMap.of(
                Constants.SERVICE_RANKING, PropertiesUtil.toInteger(serviceReference.getProperty(Constants.SERVICE_RANKING), 0),
                PROP_CLASS, clazz
        ));

        Integer rank = (Integer) propertiesCache.get(serviceReference).get(Constants.SERVICE_RANKING);

        NavigableMap<Integer, O> inst = serviceMap.get(clazz);
        if (inst == null) {
            inst = new ConcurrentSkipListMap<>();
            serviceMap.put(clazz, inst);
        } else {
            checkState(!inst.containsKey(rank),
                    "There is another instance of service for entity %s with same rank: %s", clazz, rank);
        }
        inst.put(rank, (O) instance);
    }

    public void unbindService(ServiceReference serviceReference) {
        O instance = instanceCache.get(serviceReference);
        if (instance == null) {
            return;
        }
        Class clazz = (Class) propertiesCache.get(serviceReference).get(PROP_CLASS);
        Integer rank = (Integer) propertiesCache.get(serviceReference).get(Constants.SERVICE_RANKING);

        propertiesCache.remove(serviceReference);
        instanceCache.remove(serviceReference);

        NavigableMap<Integer, O> inst = serviceMap.get(clazz);
        checkState(inst != null, "There is no instance of service for entity %s", clazz);
        checkState(inst.get(rank) == instance,
                "The instance of crud service for entity %s in unregister is not same as in register for rank %s", clazz, rank);
        inst.remove(rank);
        if (inst.size() == 0) {
            serviceMap.remove(clazz);
        }
    }

    private void addToCache() {

    }

    private  CacheServiceTracker cacheServiceTracker;

    @SneakyThrows(InvalidSyntaxException.class)
    public void openTracker(BundleContext bundleContext, Class<O> clazz) {
        cacheServiceTracker = new CacheServiceTracker(bundleContext, clazz);
        cacheServiceTracker.open(true);
    }

    public void closeTracker() {
        cacheServiceTracker.close();
    }

    private final class CacheServiceTracker extends ServiceTracker<O, O> {

        private CacheServiceTracker(BundleContext bundleContext, Class<O> clazz) throws InvalidSyntaxException {
            super(bundleContext, clazz.getName(), (ServiceTrackerCustomizer) null);
        }

        @Override
        public O addingService(ServiceReference<O> serviceReference) {
            O instance = super.addingService(serviceReference);
            bindService(serviceReference, instance);
            return instance;
        }

        @Override
        public void removedService(ServiceReference<O> serviceReference, O service) {
            unbindService(serviceReference);
            super.removedService(serviceReference, service);
        }

        @Override
        public void modifiedService(ServiceReference<O> serviceReference,
                                    O service) {
            super.modifiedService(serviceReference, service);
        }

    }

}
