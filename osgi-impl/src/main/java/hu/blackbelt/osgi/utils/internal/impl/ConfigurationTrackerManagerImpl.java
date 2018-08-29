package hu.blackbelt.osgi.utils.internal.impl;

import com.google.common.collect.ImmutableList;
import hu.blackbelt.osgi.utils.osgi.api.ConfigurationCallback;
import hu.blackbelt.osgi.utils.osgi.api.ConfigurationInfo;
import hu.blackbelt.osgi.utils.osgi.api.ConfigurationInfo.ConfigEventType;
import hu.blackbelt.osgi.utils.osgi.api.ConfigurationTrackerManager;
import lombok.extern.slf4j.Slf4j;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationEvent;
import org.osgi.service.cm.SynchronousConfigurationListener;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.Maps.newConcurrentMap;
import static hu.blackbelt.osgi.utils.osgi.api.ConfigurationInfo.ConfigEventType.CREATE;
import static hu.blackbelt.osgi.utils.osgi.api.ConfigurationInfo.ConfigEventType.DELETE;
import static hu.blackbelt.osgi.utils.osgi.api.ConfigurationInfo.ConfigEventType.UPDATE;
import static hu.blackbelt.osgi.utils.osgi.api.ConfigurationInfo.ConfigEventType.UPDATE_LOCATION;
import static java.util.Optional.ofNullable;
import static org.osgi.service.cm.ConfigurationEvent.CM_DELETED;
import static org.osgi.service.cm.ConfigurationEvent.CM_LOCATION_CHANGED;
import static org.osgi.service.cm.ConfigurationEvent.CM_UPDATED;


/**
 * It is a helper service which helps to handle configurations. It is tracking {@link Configuration} instances, and
 * handles the existing configurations too. So when a new bundle tracker is arriving it will call the create and update
 * callback for all configurations. It is uses synchronous call, so it is important that do not block the
 * execution of a callback.
 */
@Slf4j
@Component(configurationPolicy = ConfigurationPolicy.IGNORE, immediate = true)
public class ConfigurationTrackerManagerImpl implements ConfigurationTrackerManager {

    @Reference
    ConfigurationAdmin configurationAdmin;

    SynchronousConfigurationListener synchronousConfigurationListener;
    ServiceRegistration<SynchronousConfigurationListener> synchronousConfigurationListenerServiceRegistration;

    final Map<Object, Consumer<ConfigurationInfo>> createCallbacks = newConcurrentMap();
    final Map<Object, Consumer<ConfigurationInfo>> updateCallbacks = newConcurrentMap();
    final Map<Object, Consumer<ConfigurationInfo>> deleteCallbacks = newConcurrentMap();
    final Map<Object, Predicate<ConfigurationInfo>> filters = new ConcurrentHashMap<>();

    final Map<String, Dictionary<String, Object>> configurationPropertiesByPid = new ConcurrentHashMap<>();
    final Map<String, String> configurationFactoryPidByPid = new ConcurrentHashMap<>();

    final Function<Configuration, String> FACTORY_PID = new Function<Configuration, String>() {
        @Override
        public String apply(Configuration s) {
            return s.getFactoryPid() == null ? s.getPid() : s.getFactoryPid();
        }
    };

    @Activate
    public final void activate(BundleContext bundleContext) {
        this.synchronousConfigurationListener = new SynchronousConfigurationListener() {
            @Override
            public void configurationEvent(ConfigurationEvent event) {
                try {
                    configurationChangedInternal(event);
                } catch (IOException e) {
                    log.error("Error on processing configuration", e);
                }
            }
        };
        this.synchronousConfigurationListenerServiceRegistration =
                bundleContext.registerService(SynchronousConfigurationListener.class, synchronousConfigurationListener, new Hashtable<>());

        // Apply for existing configurationsByKey
        List<Configuration> allConfigurations = getAllConfigurations();
        // Add to processed PIDs
        allConfigurations.stream().forEach(p -> configurationPropertiesByPid.put(p.getPid(), p.getProperties()));
        allConfigurations.stream().forEach(p -> configurationFactoryPidByPid.put(p.getPid(), FACTORY_PID.apply(p)));
    }

    @Deactivate
    public final void deactivate() {
        synchronousConfigurationListenerServiceRegistration.unregister();
    }

    private List<Configuration> getAllConfigurations() {
        // Apply for existing configurationsByKey
        List<Configuration> allConfigurations = ImmutableList.of();
        try {
            allConfigurations = Stream.of(configurationAdmin.listConfigurations(null)).collect(Collectors.toList());
        } catch (IOException | InvalidSyntaxException e) {
            log.error("Could not get configurationsByKey", e);
        }
        return allConfigurations;
    }

    @Override
    public void registerConfigurationCallback(final Object key, ConfigurationCallback createCallback,
                                              ConfigurationCallback updateCallback, ConfigurationCallback deleteCallback,
                                              Predicate<ConfigurationInfo> filter) {

        List<Configuration> allConfigurations = getAllConfigurations();

        createCallbacks.put(key, toConsumer(createCallback));
        updateCallbacks.put(key, toConsumer(updateCallback));
        deleteCallbacks.put(key, toConsumer(deleteCallback));
        if (filter != null) {
            filters.put(key, filter);
        }

        allConfigurations.stream()
                .map(transformConfigurationToConfigurationInfo(CREATE))
                .filter(configurationFilter(key))
                .forEach(createCallbacks.get(key));

        allConfigurations.stream()
                .map(transformConfigurationToConfigurationInfo(UPDATE))
                .filter(configurationFilter(key))
                .forEach(updateCallbacks.get(key));

    }

    @Override
    public void unregisterConfigurationCallback(Object key) {
        configurationPropertiesByPid.entrySet().stream()
                .map(e -> new ConfigurationInfo(e.getKey(), configurationFactoryPidByPid.get(e.getKey()), e.getValue(), DELETE))
                .filter(configurationFilter(key))
                .forEach(deleteCallbacks.get(key));

        createCallbacks.remove(key);
        updateCallbacks.remove(key);
        deleteCallbacks.remove(key);
        filters.remove(key);
    }

    private Predicate<ConfigurationInfo> configurationFilter(Object key)  {
        return ofNullable(filters.get(key)).orElse(x -> true);
    }


    private void configurationChangedInternal(ConfigurationEvent event) throws IOException {
        log.trace("Configuration event: PID={}, factory PID={}, type={}, keys={}", event.getPid(), event.getFactoryPid(), event.getType(), event.getReference() != null ? event.getReference().getPropertyKeys() : null);
        final Map<Object, Consumer<ConfigurationInfo>> callbacks;
        final ConfigurationInfo configurationInfo;
        if ((event.getType() == CM_UPDATED || event.getType() == CM_LOCATION_CHANGED) && !configurationPropertiesByPid.containsKey(event.getPid())) {
            final Optional<Configuration> cfg = getConfiguration(event.getPid());
            if (cfg.isPresent()) {
                configurationInfo = transformConfigurationToConfigurationInfo(CREATE).apply(cfg.get());
                configurationPropertiesByPid.put(event.getPid(), configurationInfo.getProperties());
                configurationFactoryPidByPid.put(event.getPid(), configurationInfo.getConfigurationFactoryPid());
                callbacks = createCallbacks;
            } else {
                log.warn("Configuration not found: {}", event.getPid());
                return;
            }
        } else if ((event.getType() == CM_UPDATED || event.getType() == CM_LOCATION_CHANGED) && configurationPropertiesByPid.containsKey(event.getPid())) {
            ConfigEventType eventType = null;
            if (event.getType() == CM_LOCATION_CHANGED) {
                eventType = UPDATE_LOCATION;
            } else if (event.getType() == CM_UPDATED) {
                eventType = UPDATE;
            }
            final Optional<Configuration> cfg = getConfiguration(event.getPid());
            if (cfg.isPresent()) {
                configurationInfo = transformConfigurationToConfigurationInfo(eventType).apply(cfg.get());
                configurationPropertiesByPid.put(event.getPid(), configurationInfo.getProperties());
                callbacks = updateCallbacks;
            } else {
                log.warn("Configuration not found: {}", event.getPid());
                return;
           }
        } else if (event.getType() == CM_DELETED) {
            configurationInfo = new ConfigurationInfo(event.getPid(), event.getFactoryPid(), configurationPropertiesByPid.get(event.getPid()), DELETE);
            configurationPropertiesByPid.remove(event.getPid());
            configurationFactoryPidByPid.remove(event.getPid());
            callbacks = deleteCallbacks;
        } else {
            return;
        }

        for (Map.Entry<Object, Consumer<ConfigurationInfo>> entry : callbacks.entrySet()) {
            ImmutableList.of(configurationInfo).stream()
                    .filter(configurationFilter(entry.getKey()))
                    .forEach(entry.getValue());
        }
    }

    private Optional<Configuration> getConfiguration(String pid) {
        try {
            return Arrays.stream(configurationAdmin.listConfigurations(null)).filter(c -> c.getPid().equals(pid)).findFirst();
        } catch (Exception ex) {
            log.error("Invalid configuration filter", ex);
        }
        return Optional.empty();
    }

    /**
     * Transform CM {@link Configuration} to {@link ConfigurationInfo}.
     * @param configEventType casas
     * @return
     */
    private Function<Configuration, ConfigurationInfo> transformConfigurationToConfigurationInfo(ConfigEventType configEventType) {
        return new Function<Configuration, ConfigurationInfo>() {
            @Override
            public ConfigurationInfo apply(Configuration configuration) {
                return new ConfigurationInfo(configuration.getPid(), FACTORY_PID.apply(configuration), configuration.getProperties(), configEventType);
            }
        };
    }


    /**
     * Convert {@link ConfigurationCallback} to {@link Consumer}.
     *
     * @param callback {@link ConfigurationCallback} callback to create
     * @return  {@link Consumer} for create
     */
    private Consumer<ConfigurationInfo> toConsumer(final ConfigurationCallback callback) {
        return new Consumer<ConfigurationInfo>() {
            @Nullable
            @Override
            public void accept(ConfigurationInfo input) {
                callback.accept(input);
            }
        };
    }

}