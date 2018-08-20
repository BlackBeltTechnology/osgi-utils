package hu.blackbelt.osgi.utils.osgi.api.bundlecallback;

import hu.blackbelt.osgi.utils.osgi.api.BundleCallback;
import org.osgi.framework.Bundle;

import java.util.function.Consumer;

/**
 * Synch callback which does not handle threads therefore not capable of asynch behavior.
 */
public final class SynchBundleCallback implements BundleCallback {
    private final Consumer<Bundle> consumer;

    private SynchBundleCallback(Consumer<Bundle> consumer) {
        this.consumer = consumer;
    }

    public static SynchBundleCallback synchBundleCallback(Consumer<Bundle> consumer) {
        return new SynchBundleCallback(consumer);
    }

    @Override
    public void accept(Bundle bundle) {
        consumer.accept(bundle);
    }

    @Override
    public Thread process(Bundle bundle) {
        // do nothing
        return null;
    }
}
