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
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.concurrent.ConcurrentSkipListMap;

import static com.google.common.base.Preconditions.checkState;

public class ServiceCacheByProperty<O, K> {

    public static final String PROP_KEY = "__service_cache_property_key";
    public static final String RANK_KEY = "__service_cache_rank_key";
    public static final String PROPERTY_HAVE_TO_BE_DEFINED = "Property have to be defined";
    private final Map<K, NavigableMap<Comparable, O>> serviceMap = Maps.newConcurrentMap();
    private final Map<ServiceReference, Map> propertiesCache = Maps.newConcurrentMap();

    private Function<O, K> inspector;
    private Function<ServiceReference, Comparable> ranker;

    private static final Function<ServiceReference, Comparable> DEFAULT_RANKER = serviceReference -> PropertiesUtil.toInteger(serviceReference.getProperty(Constants.SERVICE_RANKING), 0);

    public ServiceCacheByProperty(@Nonnull Function<O, K> inspector, @Nullable Function<ServiceReference, Comparable> ranker) {
        this.inspector = inspector;
        this.ranker = ranker != null ? ranker : DEFAULT_RANKER;
    }

    public O find(K input) {
        Objects.requireNonNull(input, PROPERTY_HAVE_TO_BE_DEFINED);
        NavigableMap<Comparable, O> serviceInstances = serviceMap.get(input);
        if (serviceInstances == null || serviceInstances.size() == 0) {
            return null;
        }
        return serviceInstances.lastEntry().getValue();
    }

    void bindService(ServiceReference serviceReference, O instance) {
        K prop = inspector.apply(instance);
        if (prop == null) {
            return;
        }
        propertiesCache.put(serviceReference, ImmutableMap.of(
                RANK_KEY, ranker.apply(serviceReference),
                PROP_KEY, prop
        ));

        Comparable rank = (Comparable) propertiesCache.get(serviceReference).get(RANK_KEY);

        NavigableMap<Comparable, O> inst = serviceMap.get(prop);
        if (inst == null) {
            inst = new ConcurrentSkipListMap<>();
            serviceMap.put(prop, inst);
        } else {
            checkState(!inst.containsKey(rank),
                    "There is another instance of service for entity %s with same rank: %s", prop, rank);
        }
        inst.put(rank, instance);
    }

    void unbindService(ServiceReference serviceReference) {
        K prop = (K)propertiesCache.get(serviceReference).get(PROP_KEY);
        Comparable rank = ranker.apply(serviceReference);

        propertiesCache.remove(serviceReference);

        NavigableMap<Comparable, O> inst = serviceMap.get(prop);
        checkState(inst != null, "There is no instance of service for entity %s", prop);
        inst.remove(rank);
        if (inst.size() == 0) {
            serviceMap.remove(prop);
        }
    }

    private ServiceCacheByProperty.CacheServiceTracker cacheServiceTracker;

    public void openTracker(BundleContext bundleContext, Class<O> clazz) {
        cacheServiceTracker = new ServiceCacheByProperty.CacheServiceTracker(bundleContext, clazz);
        cacheServiceTracker.open(true);
    }

    public void closeTracker() {
        cacheServiceTracker.close();
    }

    private final class CacheServiceTracker extends ServiceTracker<O, O> {

        private CacheServiceTracker(BundleContext bundleContext, Class<O> clazz) {
            super(bundleContext, clazz.getName(), null);
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

            Map oldProperties = propertiesCache.get(serviceReference);
            if (oldProperties == null ||
                    !Objects.equals(oldProperties.get(RANK_KEY), ranker.apply(serviceReference)) ||
                    !Objects.equals(oldProperties.get(PROP_KEY), inspector.apply(service))) {
                unbindService(serviceReference);
                bindService(serviceReference, service);
            }
        }

    }
}
