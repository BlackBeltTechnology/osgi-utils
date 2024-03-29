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

/**
 * This interface defines a callback function which can be called on
 * change on configuration by BundleTrackerManager.
 */
public interface ConfigurationCallback {
    /**
     * This method gets the configuration which have been called.
     * @param configurationInfo
     */
    void accept(final ConfigurationInfo configurationInfo);

    /**
     * This method gets the configuration which have been called.
     * @param configurationInfo
     */
    Thread process(final ConfigurationInfo configurationInfo);
}
