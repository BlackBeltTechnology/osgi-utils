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

import java.util.Dictionary;

public class ConfigurationInfo {

    public enum ConfigEventType {
        CREATE, UPDATE, UPDATE_LOCATION, DELETE
    }

    final String configurationPid;
    final String configurationFactoryPid;
    final Dictionary<String, Object> properties;
    final ConfigEventType type;

    public ConfigurationInfo(String configurationPid, String configurationFactoryPid, Dictionary<String, Object> properties, ConfigEventType type) {
        this.configurationPid = configurationPid;
        this.configurationFactoryPid = configurationFactoryPid;
        this.properties = properties;
        this.type = type;
    }

    public String getConfigurationPid() {
        return configurationPid;
    }

    public String getConfigurationFactoryPid() {
        return configurationFactoryPid;
    }

    public Dictionary<String, Object> getProperties() {
        return properties;
    }

    public ConfigEventType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConfigurationInfo)) return false;

        ConfigurationInfo that = (ConfigurationInfo) o;

        if (!getConfigurationPid().equals(that.getConfigurationPid())) return false;
        if (!getConfigurationFactoryPid().equals(that.getConfigurationFactoryPid())) return false;
        if (!getProperties().equals(that.getProperties())) return false;
        return getType() == that.getType();
    }

    @Override
    public int hashCode() {
        int result = getConfigurationPid().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ConfigurationInfo{" +
                "configurationPid='" + configurationPid + '\'' +
                ", configurationFactoryPid='" + configurationFactoryPid + '\'' +
                ", properties=" + properties +
                ", type=" + type +
                '}';
    }


}
