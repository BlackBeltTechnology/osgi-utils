package hu.blackbelt.osgi.utils.osgi.api;

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
