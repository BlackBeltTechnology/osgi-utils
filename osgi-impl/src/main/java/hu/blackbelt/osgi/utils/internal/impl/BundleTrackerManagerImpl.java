package hu.blackbelt.osgi.utils.internal.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import hu.blackbelt.osgi.utils.osgi.api.BundleCallback;
import hu.blackbelt.osgi.utils.osgi.api.BundleTrackerManager;
import lombok.Synchronized;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.SynchronousBundleListener;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Maps.newConcurrentMap;
import static com.google.common.collect.Sets.newConcurrentHashSet;
import static java.util.Optional.ofNullable;
import static org.osgi.framework.BundleEvent.STARTED;
import static org.osgi.framework.BundleEvent.STOPPED;
import static org.osgi.framework.BundleEvent.STOPPING;


/**
 * It is a helper service which helps to handle bundles. It is tracking bundles, and
 * handles the existing bundles too. So when a new bundle tracker is arriving it will call the register
 * callback for all bundles. It is uses synchron call, so it is important that do not block the
 * execution of a callback.
 */
@Component(immediate = true)
public class BundleTrackerManagerImpl implements BundleTrackerManager {

    private final Object lock = new Object();

    /**
     * This returns true if bundle is started.
     *
     * @return
     */
    private static final Predicate<Bundle>  IS_BUNDLE_ACTIVE = new Predicate<Bundle>() {
        @Override
        public boolean test(@Nullable Bundle bundle) {
            return bundle.getState() == Bundle.ACTIVE;
        }
    };

    private BundleContext bundleContext;

    private final Map<Object, Consumer<Bundle>> registerCallbacks = newConcurrentMap();
    private final Map<Object, Consumer<Bundle>> unregisterCallbacks = newConcurrentMap();
    private final Map<Object, Map<Bundle, Thread>> registerThreads = newConcurrentMap();
    private final Map<Object, Map<Bundle, Thread>> unregisterThreads = newConcurrentMap();
    private final Set<Bundle> processedStartedBundles = newConcurrentHashSet();

    private final Map<Object, Predicate<Bundle>> filters = new ConcurrentHashMap<>();
    private final Map<Object, Set<Bundle>> bundles = new ConcurrentHashMap<>();

    private SynchronousBundleListener synchronousBundleListener;

    public BundleTrackerManagerImpl() {
    }

    @Activate
    public final void activate(BundleContext bundleContextPar) {
        this.bundleContext = bundleContextPar;
        this.synchronousBundleListener = new SynchronousBundleListener() {
            @Override
            public void bundleChanged(BundleEvent event) {
                bundleChangedInternal(event);
            }
        };
        this.bundleContext.addBundleListener(synchronousBundleListener);
    }

    @Deactivate
    public final void deactivate(BundleContext bundleContextPar) {
        this.bundleContext.removeBundleListener(synchronousBundleListener);
        for (Object key : unregisterCallbacks.keySet()) {
            appyForExistingBundles(ofNullable(unregisterCallbacks.get(key)).get(), key);
        }
    }

    @Synchronized("lock")
    public void bundleChangedInternal(BundleEvent event) {
        final Map<Object, Consumer<Bundle>> callbacks;

        if (event.getType() == STARTED && !processedStartedBundles.contains(event.getBundle())) {
            processedStartedBundles.add(event.getBundle());
            callbacks = registerCallbacks;
        } else if ((event.getType() == STOPPING || event.getType() == STOPPED) && processedStartedBundles.contains(event.getBundle())) {
            processedStartedBundles.remove(event.getBundle());
            callbacks = unregisterCallbacks;
        } else {
            callbacks = ImmutableMap.of();
        }

        for (Map.Entry<Object, Consumer<Bundle>> entry : callbacks.entrySet()) {
            final Predicate<Bundle> filterForRegistrator =
                    ofNullable(filters.get(entry.getKey())).orElse(x -> true);
            ImmutableList.of(event.getBundle()).stream()
                    .filter(Objects::nonNull)
                    .filter(filterForRegistrator)
                    .forEach(entry.getValue());
        }
    }

    @Override
    @Synchronized("lock")
    public void registerBundleCallback(Object key, BundleCallback registerCallback, BundleCallback unregisterCallback,
                                       Predicate<Bundle> filter) {
        bundles.put(key, Collections.synchronizedSet(new HashSet<Bundle>()));
        registerCallbacks.put(key, toRegister(registerCallback, key, this.bundleContext));
        unregisterCallbacks.put(key, toUnregister(unregisterCallback, key, this.bundleContext));
        if (ofNullable(filter).isPresent()) {
            filters.put(key, filter);
        }
        appyForExistingBundles(registerCallbacks.get(key), key);
    }


    @Override
    public void registerBundleCallback(Object key, BundleCallback registerCallback, BundleCallback unregisterCallback) {
        registerBundleCallback(key, registerCallback, unregisterCallback, null);
    }

    @Override
    @Synchronized("lock")
    public void unregisterBundleCallback(Object key) {
        Consumer<Bundle> registerCallback = ofNullable(registerCallbacks.get(key)).get();
        Consumer<Bundle> unregisterCallback = ofNullable(unregisterCallbacks.get(key)).get();

        appyForExistingBundles(unregisterCallback, key);
        registerCallbacks.remove(key);
        unregisterCallbacks.remove(key);

        bundles.remove(key);
        filters.remove(key);
    }

    /**
     * Convert {@link BundleCallback} to Registration {@link Consumer}.
     *
     * @param callback Bundle callback for registration
     * @param key Register for key
     * @param thisBundleContext Which gets service instance
     * @return {@link Consumer} for register
     */

    private Consumer<Bundle> toRegister(final BundleCallback callback, final Object key, final BundleContext thisBundleContext) {
        return new Consumer<Bundle>() {
            @Nullable
            @Override
            public void accept(Bundle input) {
                if (thisBundleContext.getBundle(0).getState() == Bundle.STOPPING) {
                    return;
                }
                checkState(!bundles.get(key).contains(input),
                        String.format("Bundle is already registered - Bundle: %s Key: %s!", input.getSymbolicName(), key));
                bundles.get(key).add(input);
                callback.accept(input);
                handleThreadStates(key, input, callback, registerThreads, unregisterThreads);
            }
        };
    }

    /**
     * Convert {@link BundleCallback} to Unrsegistration {@link Consumer}.
     *
     * @param callback Bundle callback for unregistration
     * @param key Register for key
     * @param thisBundleContext Which gets bundle instance
     * @return {@link Consumer} for unregister
     */
    private Consumer<Bundle> toUnregister(final BundleCallback callback, final Object key, final BundleContext thisBundleContext) {
        return new Consumer<Bundle>() {
            @Nullable
            @Override
            public void accept(Bundle input) {
                if (thisBundleContext.getBundle(0).getState() == Bundle.STOPPING) {
                    return;
                }
                processedStartedBundles.remove(bundleContext.getBundle());
                if (bundles.get(key).contains(input)) {
                    bundles.get(key).remove(input);
                    callback.accept(input);
                    handleThreadStates(key, input, callback, unregisterThreads, registerThreads);
                }
            }
        };
    }

    /**
     * It iterates all existing STARTED bundles. It filters the bundles with the registered Predicate for the key and
     *  bundles which does not contain the Judo-Platform on MANIFEST.MF.
     *
     * @param callback - The callback (registration/unregistration) which have to be made the filtered service bundles
     * @param key - The key which registers the listener
     */
    private void appyForExistingBundles(Consumer<Bundle> callback, Object key) {
        Predicate<Bundle> filterForRegistratorClass = ofNullable(filters.get(key)).orElse(x -> true);

        ImmutableList.copyOf(this.bundleContext.getBundles()).stream()
                .filter(IS_BUNDLE_ACTIVE)
                .filter(filterForRegistratorClass)
                .forEach(callback.andThen(b -> processedStartedBundles.add(b)));
    }

    private void handleThreadStates(Object key, final Bundle bundle, final BundleCallback callback,
                                    Map<Object, Map<Bundle, Thread>> startableThreads, Map<Object, Map<Bundle, Thread>> stopableThreads) {
        if (stopableThreads.containsKey(key)) {
            Thread stop = stopableThreads.get(key).get(bundle);
            if (ofNullable(stop).isPresent()) {
                if (stop.isAlive()) {
                    stop.interrupt();
                }
                stopableThreads.get(key).remove(bundle);
            }
            if (stopableThreads.get(key).size() == 0) {
                stopableThreads.remove(key);
            }
        }

        Thread start = callback.process(bundle);
        if (ofNullable(start).isPresent()) {
            if (!startableThreads.containsKey(key)) {
                startableThreads.put(key, Maps.<Bundle, Thread>newConcurrentMap());
            }
            startableThreads.get(key).put(bundle, start);
            start.start();
        }
    }

}
