package hu.blackbelt.osgi.utils.internal.impl;

import hu.blackbelt.osgi.utils.osgi.api.BundleCallback;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.InvalidSyntaxException;

import java.util.Dictionary;
import java.util.function.Predicate;

import static hu.blackbelt.osgi.utils.test.MockOsgi.activate;
import static hu.blackbelt.osgi.utils.test.MockOsgi.deactivate;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BundleTrackerManagerImplTest {

    private final static String TEST_FILTER_HEADER = "Test";

    private BundleTrackerManagerImpl trackerManager;

    @Mock
    BundleContext bundleContextMock;

    @Mock
    BundleCallback registerCallbackMock;

    @Mock
    BundleCallback unregisterCallbackMock;

    @Mock
    BundleEvent registerBundleEventMock;

    @Mock
    BundleEvent unregisterBundleEventMock;

    @Mock
    Dictionary headersMock;

    @Mock
    Bundle systemBundleMock;

    @Mock
    Bundle bundleMock;

    @Mock
    Bundle bundleMock2;

    @Mock
    Object instance;

    @Mock
    Object instance2;

    @Before
    public void setUp() throws Exception {
        trackerManager = new BundleTrackerManagerImpl();
        when(registerBundleEventMock.getType()).thenReturn(BundleEvent.STARTED);
        when(unregisterBundleEventMock.getType()).thenReturn(BundleEvent.STOPPING);
        when(systemBundleMock.getHeaders()).thenReturn(headersMock);
        when(bundleMock.getHeaders()).thenReturn(headersMock);
        when(bundleMock2.getHeaders()).thenReturn(headersMock);

        when(headersMock.get(TEST_FILTER_HEADER)).thenReturn("true");
        when(systemBundleMock.getBundleContext()).thenReturn(bundleContextMock);

        when(bundleContextMock.getBundle(0)).thenReturn(systemBundleMock);
        when(bundleContextMock.getBundle()).thenReturn(systemBundleMock);

        when(bundleMock.getState()).thenReturn(Bundle.ACTIVE);
    }

    @Test
    public void testRegisterAndUnregisterWithOneBundleExists() throws InvalidSyntaxException {
        Bundle[] bundles = new Bundle[]{bundleMock, bundleMock2};
        when(bundleContextMock.getBundles()).thenReturn(bundles);
        when(bundleMock.getState()).thenReturn(Bundle.ACTIVE);
        when(bundleMock2.getState()).thenReturn(Bundle.ACTIVE);

        activate(trackerManager, bundleContextMock);
        trackerManager.registerBundleCallback(this.getClass(), registerCallbackMock, unregisterCallbackMock);
        trackerManager.unregisterBundleCallback(this.getClass());
        deactivate(trackerManager, bundleContextMock);

        verify(registerCallbackMock, times(1)).accept(bundleMock);
        verify(registerCallbackMock, times(1)).accept(bundleMock2);
        verify(unregisterCallbackMock, times(1)).accept(bundleMock);
        verify(unregisterCallbackMock, times(1)).accept(bundleMock2);
    }

    @Test
    public void testBundleChangeRegister() throws InvalidSyntaxException {
        Bundle[] bundlesBeforeChange = new Bundle[]{bundleMock};
        Bundle[] bundlesAfterChange = new Bundle[]{bundleMock, bundleMock2};
        when(bundleMock.getState()).thenReturn(Bundle.ACTIVE);
        when(bundleMock2.getState()).thenReturn(Bundle.STARTING);

        when(bundleContextMock.getBundles()).thenReturn(bundlesBeforeChange);

        activate(trackerManager, bundleContextMock);
        trackerManager.registerBundleCallback(this.getClass(), registerCallbackMock, unregisterCallbackMock);
        verify(registerCallbackMock, times(1)).accept(bundleMock);

        when(registerBundleEventMock.getBundle()).thenReturn(bundleMock2);
        trackerManager.bundleChangedInternal(registerBundleEventMock);
        verify(registerCallbackMock, times(1)).accept(bundleMock2);

        when(bundleContextMock.getBundles()).thenReturn(bundlesAfterChange);
        when(bundleMock2.getState()).thenReturn(Bundle.ACTIVE);

        trackerManager.unregisterBundleCallback(this.getClass());
        verify(unregisterCallbackMock, times(1)).accept(bundleMock2);

        deactivate(trackerManager, bundleContextMock);
        verify(unregisterCallbackMock, times(1)).accept(bundleMock);
    }


    @Test
    public void testBundleChangeUnregister() throws InvalidSyntaxException {
        Bundle[] bundlesBeforeChange = new Bundle[]{bundleMock, bundleMock2};
        Bundle[] bundlesAfterChange = new Bundle[]{bundleMock};
        when(bundleMock.getState()).thenReturn(Bundle.ACTIVE);
        when(bundleMock2.getState()).thenReturn(Bundle.ACTIVE);

        when(bundleContextMock.getBundles()).thenReturn(bundlesBeforeChange);

        activate(trackerManager, bundleContextMock);
        trackerManager.registerBundleCallback(this.getClass(), registerCallbackMock, unregisterCallbackMock);

        when(unregisterBundleEventMock.getBundle()).thenReturn(bundleMock2);
        trackerManager.bundleChangedInternal(unregisterBundleEventMock);

        when(bundleContextMock.getBundles()).thenReturn(bundlesAfterChange);
        when(bundleMock2.getState()).thenReturn(Bundle.RESOLVED);

        trackerManager.unregisterBundleCallback(this.getClass());

        deactivate(trackerManager, bundleContextMock);
        verify(registerCallbackMock, times(1)).accept(bundleMock);
        verify(registerCallbackMock, times(1)).accept(bundleMock2);
        verify(unregisterCallbackMock, times(1)).accept(bundleMock);
        verify(unregisterCallbackMock, times(1)).accept(bundleMock2);
    }

    @Test
    public void testBundleChangeRegisterAndUnregister() throws InvalidSyntaxException {
        Bundle[] bundlesBeforeChange = new Bundle[]{bundleMock};
        Bundle[] bundlesAfterChange = new Bundle[]{bundleMock};
        when(bundleMock.getState()).thenReturn(Bundle.ACTIVE);
        when(bundleMock2.getState()).thenReturn(Bundle.STARTING);

        when(bundleContextMock.getBundles()).thenReturn(bundlesBeforeChange);

        activate(trackerManager, bundleContextMock);

        when(registerBundleEventMock.getBundle()).thenReturn(bundleMock2);
        when(unregisterBundleEventMock.getBundle()).thenReturn(bundleMock2);

        trackerManager.registerBundleCallback(this.getClass(), registerCallbackMock, unregisterCallbackMock);

        trackerManager.bundleChangedInternal(registerBundleEventMock);

        when(bundleMock2.getState()).thenReturn(Bundle.ACTIVE);
        trackerManager.bundleChangedInternal(unregisterBundleEventMock);

        when(bundleMock2.getState()).thenReturn(Bundle.RESOLVED);
        when(bundleContextMock.getBundles()).thenReturn(bundlesAfterChange);
        trackerManager.unregisterBundleCallback(this.getClass());

        deactivate(trackerManager, bundleContextMock);
        verify(registerCallbackMock, times(1)).accept(bundleMock);
        verify(registerCallbackMock, times(1)).accept(bundleMock2);
        verify(unregisterCallbackMock, times(1)).accept(bundleMock);
        verify(unregisterCallbackMock, times(1)).accept(bundleMock2);
    }



    @Test
    public void testNonProcessableBundle() throws InvalidSyntaxException {
        when(headersMock.get(TEST_FILTER_HEADER)).thenReturn(null);

        Bundle[] bundlesBeforeChange = new Bundle[]{bundleMock, bundleMock2};
        Bundle[] bundlesAfterChange = new Bundle[]{bundleMock};
        when(bundleMock.getState()).thenReturn(Bundle.ACTIVE);
        when(bundleMock2.getState()).thenReturn(Bundle.ACTIVE);

        when(bundleContextMock.getBundles()).thenReturn(bundlesBeforeChange);
        activate(trackerManager, bundleContextMock);

        when(registerBundleEventMock.getBundle()).thenReturn(bundleMock2);
        when(unregisterBundleEventMock.getBundle()).thenReturn(bundleMock2);

        trackerManager.registerBundleCallback(this.getClass(), registerCallbackMock, unregisterCallbackMock, new Predicate<Bundle>() {
            @Override
            public boolean test(Bundle bundle) {
                return bundle.getHeaders().get(TEST_FILTER_HEADER) != null && bundle.getHeaders().get(TEST_FILTER_HEADER).equals("true");
            }
        });

        when(bundleMock2.getState()).thenReturn(Bundle.ACTIVE);
        trackerManager.bundleChangedInternal(unregisterBundleEventMock);

        when(bundleMock2.getState()).thenReturn(Bundle.RESOLVED);
        when(bundleContextMock.getBundles()).thenReturn(bundlesAfterChange);
        trackerManager.unregisterBundleCallback(this.getClass());

        deactivate(trackerManager, bundleContextMock);
        verify(registerCallbackMock, times(0)).accept(bundleMock);
        verify(registerCallbackMock, times(0)).accept(bundleMock2);
        verify(unregisterCallbackMock, times(0)).accept(bundleMock);
        verify(unregisterCallbackMock, times(0)).accept(bundleMock2);
    }

}
