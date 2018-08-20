package hu.blackbelt.osgi.utils.osgi.api.bundlecallback;

import hu.blackbelt.osgi.utils.osgi.api.BundleCallback;
import org.osgi.framework.Bundle;

import java.util.function.Consumer;

/**
 * Asynch callback which does handle threads only but does nothing during the sync call.
 */
public final class AsynchBundleCallback implements BundleCallback {
    private final Consumer<Bundle> consumer;

    private AsynchBundleCallback(Consumer<Bundle> consumer) {
        this.consumer = consumer;
    }

    public static AsynchBundleCallback asynchBundleCallback(Consumer<Bundle> consumer) {
        return new AsynchBundleCallback(consumer);
    }

    @Override
    public void accept(Bundle bundle) {
        // do nothing
    }

    @Override
    public Thread process(Bundle bundle) {
        return new Thread(() -> consumer.accept(bundle));
    }
}
