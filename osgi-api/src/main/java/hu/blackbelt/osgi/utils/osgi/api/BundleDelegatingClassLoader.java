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
