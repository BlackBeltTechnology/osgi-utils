package hu.blackbelt.osgi.utils.test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;

import static hu.blackbelt.osgi.utils.test.MockOsgi.callBindForStandardReference;
import static hu.blackbelt.osgi.utils.test.MockOsgi.callUnbindForStandardReference;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class StandardMockOsgiTest {

    @Mock
    Dictionary dictionaryMock;

    Map mapMock = Maps.newHashMap();

    @Mock
    BundleContext bundleContextMock;

    @Mock
    ComponentContext componentContextMock;

    @Mock
    I1 i1Mock;

    @Mock
    I2 i2Mock;

    @Test
    public void activateValidTest() {
        A1 a1 = new A1();
        MockOsgi.activate(a1);
        assertTrue(a1.activated);

        A1C a1c = new A1C();
        MockOsgi.activate(a1c);
        assertTrue(a1c.activated);

        A2 a2 = new A2();
        MockOsgi.activate(a2, dictionaryMock);
        assertTrue(a2.activated);

        A2 a2Map = new A2();
        MockOsgi.activate(a2Map, mapMock);
        assertTrue(a2Map.activated);

        A3 a3 = new A3();
        MockOsgi.activate(a3, bundleContextMock);
        assertTrue(a3.activated);

        A4 a4 = new A4();
        MockOsgi.activate(a4, componentContextMock);
        assertTrue(a4.activated);

        A4 a4Dictionary = new A4();
        MockOsgi.activate(a4Dictionary, dictionaryMock);
        assertTrue(a4Dictionary.activated);

        A4 a4Map = new A4();
        MockOsgi.activate(a4Map, mapMock);
        assertTrue(a4Map.activated);
    }

    @Test(expected = IllegalArgumentException.class)
    public void activateInvalidTest() {
        A5 a5 = new A5();
        MockOsgi.activate(a5);
    }

    @Test
    public void deactivateTest() {
        D1 d1 = new D1();
        MockOsgi.deactivate(d1);
        assertTrue(d1.deactivated);

        D1 d1c = new D1C();
        MockOsgi.deactivate(d1c);
        assertTrue(d1c.deactivated);

    }

    @Test
    public void referenceIterableFromReferencesAnnotationTest() {
        org.osgi.service.component.annotations.Reference i1ref = ImmutableList.copyOf(MockOsgi.referenceIterableFromStandardComponentAnnotation().apply(MockOsgi.getAllAnnotations(T1.class, org.osgi.service.component.annotations.Component.class).get(0))).get(0);
    }


    @Test
    public void callBindAndUnbindForReferenceTest() throws NoSuchFieldException {
        T1 t1 = new T1();
        org.osgi.service.component.annotations.Reference i1ref = ImmutableList.copyOf(MockOsgi.referenceIterableFromStandardComponentAnnotation().apply(MockOsgi.getAllAnnotations(T1.class, org.osgi.service.component.annotations.Component.class).get(0))).get(0);
        assertEquals(I1.class, i1ref.service());

        callBindForStandardReference(t1, i1Mock).apply(i1ref);
        assertEquals(i1Mock, t1.getI1s().get(0));

        callUnbindForStandardReference(t1, i1Mock).apply(i1ref);
        assertEquals(0, t1.getI1s().size());
    }

    @Test
    public void setReferecesTest() {
        T1 t1 = new T1();
        MockOsgi.setReferences(t1, i1Mock, i2Mock);
        assertEquals(i1Mock, t1.getI1s().get(0));

        T2 t2 = new T2();
        MockOsgi.setReferences(t2, i1Mock);
        assertEquals(i1Mock, t2.getI1());

    }


    public static class A1 {
        boolean activated;

        @org.osgi.service.component.annotations.Activate
        public void activate() {
            activated = true;
        }
    }

    public static class A1C extends A1 { }

    public static class A2 {
        boolean activated;

        @org.osgi.service.component.annotations.Activate
        public void activate(Dictionary dictionary) {
            activated = true;
        }
    }

    public static class A3 {
        boolean activated;

        @org.osgi.service.component.annotations.Activate
        public void activate(BundleContext bundleContext) {
            activated = true;
        }
    }

    public static class A4 {
        boolean activated;

        @org.osgi.service.component.annotations.Activate
        public void activate(ComponentContext bundleContext) {
            activated = true;
        }
    }

    public static class A5 {
        boolean activated;

        @org.osgi.service.component.annotations.Activate
        public void activate(String invalid) {
            activated = true;
        }
    }

    public static class D1 {
        boolean deactivated;

        @org.osgi.service.component.annotations.Deactivate
        public void detivate() {
            deactivated = true;
        }
    }

    public static class D1C extends D1 { }

    public interface I1 {
    }

    public interface I2 {
    }

    @org.osgi.service.component.annotations.Component(reference = {
            @org.osgi.service.component.annotations.Reference(name = "I1", service = I1.class)
    })
    @Getter
    @Setter
    public static class T1 {
        List<I1> i1s = new ArrayList<>();

        public void bindI1(I1 i1) {
            i1s.add(i1);
        }

        public void unbindI1(I1 i1) {
            i1s.remove(i1);
        }
    }

    @Getter
    public static class T2 {
        @org.osgi.service.component.annotations.Reference
        I1 i1;
    }
}
