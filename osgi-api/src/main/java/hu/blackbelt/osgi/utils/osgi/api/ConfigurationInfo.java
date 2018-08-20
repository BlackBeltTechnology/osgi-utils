package hu.blackbelt.osgi.utils.osgi.api;

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
