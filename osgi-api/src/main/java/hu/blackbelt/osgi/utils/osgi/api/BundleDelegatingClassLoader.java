package hu.blackbelt.osgi.utils.osgi.api;

import org.osgi.framework.Bundle;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;

import static com.google.common.base.MoreObjects.firstNonNull;

/**
 * Resource and class loading delegated to the OSGi Bundle.
 */
public class BundleDelegatingClassLoader extends ClassLoader {
    private final Bundle bundle;

    public BundleDelegatingClassLoader(Bundle bundle) {
        this.bundle = bundle;
    }

    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException {
        return bundle.loadClass(name);
    }

    @Override
    protected URL findResource(final String name) {
        return bundle.getResource(name);
    }

    @Override
    protected Enumeration<URL> findResources(final String name) throws IOException {
        return firstNonNull(bundle.getResources(name), Collections.<URL>emptyEnumeration());
    }
}
