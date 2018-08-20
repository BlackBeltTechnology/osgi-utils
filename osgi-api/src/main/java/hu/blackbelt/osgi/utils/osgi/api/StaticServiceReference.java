package hu.blackbelt.osgi.utils.osgi.api;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import java.util.function.Consumer;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkState;

public class StaticServiceReference<T> implements AutoCloseable {
    private final BundleContext bundleContext;
    private final ServiceReference<T> serviceReference;
    private final T service;

    public StaticServiceReference(String serviceClassName, Class<?> bundleClass) {
        Bundle bundle = FrameworkUtil.getBundle(bundleClass);
        checkState(bundle != null, "The bundle is null.");
        bundleContext = bundle.getBundleContext();
        checkState(bundleContext != null, "The bundle context is null.");
        serviceReference = (ServiceReference<T>) bundleContext.getServiceReference(serviceClassName);
        service = bundleContext.getService(serviceReference);
    }

    public StaticServiceReference(Class<T> serviceClass, Class<?> bundleClass) {
        this(serviceClass.getName(), bundleClass);
    }

    public T getService() {
        return service;
    }

    @Override
    public void close() {
        bundleContext.ungetService(serviceReference);
    }

    public static <T> FluentStaticServiceReference<T> staticServiceReference(Class<T> serviceClass) {
        return staticServiceReference(serviceClass, serviceClass);
    }

    public static <T> FluentStaticServiceReference<T> staticServiceReference(Class<T> serviceClass, Class<?> bundleClass) {
        return staticServiceReference(serviceClass.getName(), bundleClass);
    }

    public static <T> FluentStaticServiceReference<T> staticServiceReference(String serviceClassName, Class<?> bundleClass) {
        return new FluentStaticServiceReference<T>(serviceClassName, bundleClass);
    }

    public static class FluentStaticServiceReference<T> {
        private final String serviceClassName;
        private final Class<?> bundleClass;

        public FluentStaticServiceReference(String serviceClassName, Class<?> bundleClass) {
            this.serviceClassName = serviceClassName;
            this.bundleClass = bundleClass;
        }

        public <R> R access(Function<T, R> serviceConsumer) {
            try (StaticServiceReference<T> serviceReference = new StaticServiceReference<T>(serviceClassName, bundleClass)) {
                return serviceConsumer.apply(serviceReference.getService());
            }
        }

        public void consume(Consumer<T> serviceConsumer) {
            try (StaticServiceReference<T> serviceReference = new StaticServiceReference<T>(serviceClassName, bundleClass)) {
                serviceConsumer.accept(serviceReference.getService());
            }
        }
    }
}
