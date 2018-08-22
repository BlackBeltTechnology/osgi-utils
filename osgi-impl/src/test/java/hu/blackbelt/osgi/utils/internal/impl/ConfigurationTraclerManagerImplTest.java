package hu.blackbelt.osgi.utils.internal.impl;

import hu.blackbelt.osgi.utils.osgi.api.ConfigurationCallback;
import hu.blackbelt.osgi.utils.osgi.api.ConfigurationInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationEvent;
import org.osgi.service.cm.SynchronousConfigurationListener;

import java.io.IOException;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.List;

import static hu.blackbelt.osgi.utils.osgi.api.ConfigurationInfo.ConfigEventType.CREATE;
import static hu.blackbelt.osgi.utils.osgi.api.ConfigurationInfo.ConfigEventType.DELETE;
import static hu.blackbelt.osgi.utils.osgi.api.ConfigurationInfo.ConfigEventType.UPDATE;
import static hu.blackbelt.osgi.utils.osgi.api.ConfigurationInfo.ConfigEventType.UPDATE_LOCATION;
import static hu.blackbelt.osgi.utils.test.MockOsgi.activate;
import static hu.blackbelt.osgi.utils.test.MockOsgi.deactivate;
import static hu.blackbelt.osgi.utils.test.MockOsgi.setReferences;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.osgi.service.cm.ConfigurationEvent.CM_DELETED;
import static org.osgi.service.cm.ConfigurationEvent.CM_LOCATION_CHANGED;
import static org.osgi.service.cm.ConfigurationEvent.CM_UPDATED;

@RunWith(MockitoJUnitRunner.class)
public class ConfigurationTraclerManagerImplTest {

    private static final String PID1 = "pid1";
    private static final String FACTORY_PID1 = "pid1.factory";

    private static final String PID2 = "pid2";
    private static final String FACTORY_PID2 = "pid2.factory";

    private ConfigurationTrackerManagerImpl trackerManager;

    @Mock
    BundleContext bundleContextMock;

    @Mock
    ServiceRegistration asyncConfigListenerRegistrationMock;

    @Mock
    Configuration configuration1Mock;

    @Mock
    Configuration configuration2Mock;

    @Mock
    Dictionary<String, Object> configuration1PropertiesMock;

    @Mock
    Dictionary<String, Object> configuration2PropertiesMock;


    @Mock
    ConfigurationCallback createCallbackMock;

    @Mock
    ConfigurationCallback updateCallbackMock;

    @Mock
    ConfigurationCallback deleteCallbackMock;

    @Mock
    ConfigurationAdmin configurationAdminMock;


    @Mock
    ConfigurationEvent updateConfigEventMock;

    @Mock
    ConfigurationEvent updateLocationConfigEventMock;

    @Mock
    ConfigurationEvent deleteConfigEventMock;

    ArgumentCaptor<ConfigurationInfo> configurationInfoArgumentCaptor;

    @Before
    public void setUp() throws Exception {
        trackerManager = new ConfigurationTrackerManagerImpl();

        when(updateConfigEventMock.getFactoryPid()).thenReturn(FACTORY_PID1);
        when(updateConfigEventMock.getFactoryPid()).thenReturn(PID1);
        when(updateConfigEventMock.getType()).thenReturn(CM_UPDATED);

        when(updateLocationConfigEventMock.getFactoryPid()).thenReturn(FACTORY_PID1);
        when(updateLocationConfigEventMock.getFactoryPid()).thenReturn(PID1);
        when(updateLocationConfigEventMock.getType()).thenReturn(CM_LOCATION_CHANGED);

        when(deleteConfigEventMock.getFactoryPid()).thenReturn(FACTORY_PID1);
        when(deleteConfigEventMock.getFactoryPid()).thenReturn(PID1);
        when(deleteConfigEventMock.getType()).thenReturn(ConfigurationEvent.CM_DELETED);

        when(configuration1Mock.getPid()).thenReturn(PID1);
        when(configuration1Mock.getFactoryPid()).thenReturn(FACTORY_PID1);
        when(configuration1Mock.getProperties()).thenReturn(configuration1PropertiesMock);

        when(configuration2Mock.getPid()).thenReturn(PID2);
        when(configuration2Mock.getFactoryPid()).thenReturn(FACTORY_PID2);
        when(configuration2Mock.getProperties()).thenReturn(configuration2PropertiesMock);

        when(bundleContextMock.registerService(any(Class.class), any(SynchronousConfigurationListener.class), any(Dictionary.class))).thenReturn(asyncConfigListenerRegistrationMock);

        setReferences(trackerManager, configurationAdminMock);

    }


    public void  assertConfigurationInfo(ConfigurationInfo.ConfigEventType eventType, String pid, String factoryPid, Dictionary<String, Object> properties, ConfigurationInfo expected) {
        assertEquals(eventType, expected.getType());
        assertEquals(pid, expected.getConfigurationPid());
        assertEquals(factoryPid, expected.getConfigurationFactoryPid());
        assertEquals(properties, expected.getProperties());

    }

    @Test
    public void testCreateUpdateDeleteWithTwoConfigurationExists() throws IOException, InvalidSyntaxException {
        when(configurationAdminMock.listConfigurations(null)).thenReturn(new Configuration[]{configuration1Mock, configuration2Mock});
        activate(trackerManager, bundleContextMock);

        trackerManager.registerConfigurationCallback(this.getClass(), createCallbackMock, updateCallbackMock, deleteCallbackMock, null);

        configurationInfoArgumentCaptor = ArgumentCaptor.forClass(ConfigurationInfo.class);
        verify(createCallbackMock, times(2)).accept(configurationInfoArgumentCaptor.capture());
        List<ConfigurationInfo> capturedCreates = configurationInfoArgumentCaptor.getAllValues();
        capturedCreates.sort(Comparator.comparing(ConfigurationInfo::getConfigurationPid));

        assertConfigurationInfo(CREATE, PID1, FACTORY_PID1, configuration1PropertiesMock, capturedCreates.get(0));
        assertConfigurationInfo(CREATE, PID2, FACTORY_PID2, configuration2PropertiesMock, capturedCreates.get(1));

        configurationInfoArgumentCaptor = ArgumentCaptor.forClass(ConfigurationInfo.class);
        verify(updateCallbackMock, times(2)).accept(configurationInfoArgumentCaptor.capture());
        List<ConfigurationInfo> capturedUpdates = configurationInfoArgumentCaptor.getAllValues();
        capturedUpdates.sort(Comparator.comparing(ConfigurationInfo::getConfigurationPid));

        assertConfigurationInfo(UPDATE, PID1, FACTORY_PID1, configuration1PropertiesMock, capturedUpdates.get(0));
        assertConfigurationInfo(UPDATE, PID2, FACTORY_PID2, configuration2PropertiesMock, capturedUpdates.get(1));

        verify(deleteCallbackMock, times(0)).accept(any(ConfigurationInfo.class));

        trackerManager.unregisterConfigurationCallback(this.getClass());

        configurationInfoArgumentCaptor = ArgumentCaptor.forClass(ConfigurationInfo.class);
        verify(deleteCallbackMock, times(2)).accept(configurationInfoArgumentCaptor.capture());
        List<ConfigurationInfo> capturedDeletes = configurationInfoArgumentCaptor.getAllValues();
        capturedCreates.sort(Comparator.comparing(ConfigurationInfo::getConfigurationPid));

        assertConfigurationInfo(DELETE, PID1, FACTORY_PID1, configuration1PropertiesMock, capturedDeletes.get(0));
        assertConfigurationInfo(DELETE, PID2, FACTORY_PID2, configuration2PropertiesMock, capturedDeletes.get(1));

        deactivate(trackerManager, bundleContextMock);

    }

    @Test
    public void testNoCallWithoutEvent() throws IOException, InvalidSyntaxException {
        when(configurationAdminMock.listConfigurations(null)).thenReturn(new Configuration[]{});
        activate(trackerManager, bundleContextMock);

        trackerManager.registerConfigurationCallback(this.getClass(), createCallbackMock, updateCallbackMock, deleteCallbackMock, null);
        trackerManager.unregisterConfigurationCallback(this.getClass());

        verify(createCallbackMock, times(0)).accept(any(ConfigurationInfo.class));
        verify(updateCallbackMock, times(0)).accept(any(ConfigurationInfo.class));
        verify(deleteCallbackMock, times(0)).accept(any(ConfigurationInfo.class));

        deactivate(trackerManager, bundleContextMock);

    }


    @Test
    public void testCreateUpdateDeleteEvent() throws IOException, InvalidSyntaxException {
        when(configurationAdminMock.listConfigurations(null)).thenReturn(new Configuration[]{});
        activate(trackerManager, bundleContextMock);

        trackerManager.registerConfigurationCallback(this.getClass(), createCallbackMock, updateCallbackMock, deleteCallbackMock, null);

        // CREATE
        ConfigurationEvent event = Mockito.mock(ConfigurationEvent.class);
        when(event.getType()).thenReturn(CM_UPDATED);
        when(event.getPid()).thenReturn(PID1);
        when(event.getFactoryPid()).thenReturn(FACTORY_PID1);
        when(configurationAdminMock.listConfigurations(null)).thenReturn(new Configuration[]{configuration1Mock});

        trackerManager.synchronousConfigurationListener.configurationEvent(event);

        configurationInfoArgumentCaptor = ArgumentCaptor.forClass(ConfigurationInfo.class);
        verify(createCallbackMock, times(1)).accept(configurationInfoArgumentCaptor.capture());
        assertConfigurationInfo(CREATE, PID1, FACTORY_PID1, configuration1PropertiesMock, configurationInfoArgumentCaptor.getValue());

        verify(updateCallbackMock, times(0)).accept(any(ConfigurationInfo.class));
        verify(deleteCallbackMock, times(0)).accept(any(ConfigurationInfo.class));

        // UPDATE
        trackerManager.synchronousConfigurationListener.configurationEvent(event);

        configurationInfoArgumentCaptor = ArgumentCaptor.forClass(ConfigurationInfo.class);
        verify(createCallbackMock, times(1)).accept(configurationInfoArgumentCaptor.capture());
        assertConfigurationInfo(CREATE, PID1, FACTORY_PID1, configuration1PropertiesMock, configurationInfoArgumentCaptor.getValue());

        configurationInfoArgumentCaptor = ArgumentCaptor.forClass(ConfigurationInfo.class);
        verify(updateCallbackMock, times(1)).accept(configurationInfoArgumentCaptor.capture());
        assertConfigurationInfo(UPDATE, PID1, FACTORY_PID1, configuration1PropertiesMock, configurationInfoArgumentCaptor.getValue());

        verify(deleteCallbackMock, times(0)).accept(any(ConfigurationInfo.class));


        // UPDATE LOCATION
        when(event.getType()).thenReturn(CM_LOCATION_CHANGED);
        trackerManager.synchronousConfigurationListener.configurationEvent(event);

        configurationInfoArgumentCaptor = ArgumentCaptor.forClass(ConfigurationInfo.class);
        verify(createCallbackMock, times(1)).accept(configurationInfoArgumentCaptor.capture());
        assertConfigurationInfo(CREATE, PID1, FACTORY_PID1, configuration1PropertiesMock, configurationInfoArgumentCaptor.getValue());

        configurationInfoArgumentCaptor = ArgumentCaptor.forClass(ConfigurationInfo.class);
        verify(updateCallbackMock, times(2)).accept(configurationInfoArgumentCaptor.capture());
        List<ConfigurationInfo> capturedUpdates = configurationInfoArgumentCaptor.getAllValues();
        capturedUpdates.sort(Comparator.comparing(ConfigurationInfo::getType));
        assertConfigurationInfo(UPDATE, PID1, FACTORY_PID1, configuration1PropertiesMock, capturedUpdates.get(0));
        assertConfigurationInfo(UPDATE_LOCATION, PID1, FACTORY_PID1, configuration1PropertiesMock, capturedUpdates.get(1));

        verify(deleteCallbackMock, times(0)).accept(any(ConfigurationInfo.class));

        // DELETE
        when(event.getType()).thenReturn(CM_DELETED);
        trackerManager.synchronousConfigurationListener.configurationEvent(event);
        configurationInfoArgumentCaptor = ArgumentCaptor.forClass(ConfigurationInfo.class);
        verify(createCallbackMock, times(1)).accept(configurationInfoArgumentCaptor.capture());
        assertConfigurationInfo(CREATE, PID1, FACTORY_PID1, configuration1PropertiesMock, configurationInfoArgumentCaptor.getValue());
        configurationInfoArgumentCaptor = ArgumentCaptor.forClass(ConfigurationInfo.class);
        verify(updateCallbackMock, times(2)).accept(configurationInfoArgumentCaptor.capture());
        capturedUpdates = configurationInfoArgumentCaptor.getAllValues();
        capturedUpdates.sort(Comparator.comparing(ConfigurationInfo::getType));
        assertConfigurationInfo(UPDATE, PID1, FACTORY_PID1, configuration1PropertiesMock, capturedUpdates.get(0));
        assertConfigurationInfo(UPDATE_LOCATION, PID1, FACTORY_PID1, configuration1PropertiesMock, capturedUpdates.get(1));

        verify(deleteCallbackMock, times(1)).accept(configurationInfoArgumentCaptor.capture());
        assertConfigurationInfo(DELETE, PID1, FACTORY_PID1, configuration1PropertiesMock, configurationInfoArgumentCaptor.getValue());

        trackerManager.unregisterConfigurationCallback(this.getClass());

        // Not new delete event can be created
        configurationInfoArgumentCaptor = ArgumentCaptor.forClass(ConfigurationInfo.class);
        verify(deleteCallbackMock, times(1)).accept(configurationInfoArgumentCaptor.capture());
        assertConfigurationInfo(DELETE, PID1, FACTORY_PID1, configuration1PropertiesMock, configurationInfoArgumentCaptor.getValue());
        deactivate(trackerManager, bundleContextMock);
    }

}
