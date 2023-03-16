package hu.blackbelt.osgi.utils.test;

/*-
 * #%L
 * OSGi util tester (mock)
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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;
import org.junit.Ignore;
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

import static hu.blackbelt.osgi.utils.test.MockOsgi.referenceIterableFromFelixReferencesAnnotation;
import static hu.blackbelt.osgi.utils.test.MockOsgi.referenceIterableFromStandardComponentAnnotation;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Ignore
@RunWith(MockitoJUnitRunner.class)
public class FelixMockOsgiTest {

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
        org.apache.felix.scr.annotations.Reference i1ref =
                MockOsgi.getAllAnnotations(T1.class, org.apache.felix.scr.annotations.References.class).stream()
                        .flatMap(referenceIterableFromFelixReferencesAnnotation()).findFirst().get();

    }

    @Test
    public void referenceIterableFromReferencesAnnotationInheritenceTest() {
        org.apache.felix.scr.annotations.Reference i1ref =
                MockOsgi.getAllAnnotations(T1C.class, org.apache.felix.scr.annotations.References.class).stream()
                        .flatMap(referenceIterableFromFelixReferencesAnnotation()).findFirst().get();
    }


    @Test
    public void callBindAndUnbindForReferenceTest() {
        T1 t1 = new T1();
        org.apache.felix.scr.annotations.Reference i1ref =
                MockOsgi.getAllAnnotations(T1.class, org.apache.felix.scr.annotations.References.class).stream()
                        .flatMap(referenceIterableFromFelixReferencesAnnotation()).findFirst().get();
        org.apache.felix.scr.annotations.Reference i2ref = MockOsgi.getAllAnnotations(T1.class, org.apache.felix.scr.annotations.Reference.class).get(0);
        assertEquals(I1.class, i1ref.referenceInterface());
        assertEquals(I2.class, i2ref.referenceInterface());

        MockOsgi.callBindForFelixReference(t1, i1Mock).apply(i1ref);
        assertEquals(i1Mock, t1.getI1s().get(0));
        MockOsgi.callBindForFelixReference(t1, i2Mock).apply(i2ref);
        assertEquals(i2Mock, t1.getI2s().get(0));

        MockOsgi.callUnbindForFelixReference(t1, i1Mock).apply(i1ref);
        assertEquals(0, t1.getI1s().size());
        MockOsgi.callUnbindForFelixReference(t1, i2Mock).apply(i2ref);
        assertEquals(0, t1.getI2s().size());
    }


    @Test
    public void callBindAndUnbindForReferenceInheritenceTest() {
        T1C t1c = new T1C();
        org.apache.felix.scr.annotations.Reference i1ref = referenceIterableFromFelixReferencesAnnotation().apply(MockOsgi.getAllAnnotations(T1C.class, org.apache.felix.scr.annotations.References.class).get(0)).findFirst().get();
        org.apache.felix.scr.annotations.Reference i2ref = MockOsgi.getAllAnnotations(T1C.class, org.apache.felix.scr.annotations.Reference.class).get(0);
        assertEquals(I1.class, i1ref.referenceInterface());
        assertEquals(I2.class, i2ref.referenceInterface());

        MockOsgi.callBindForFelixReference(t1c, i1Mock).apply(i1ref);
        assertEquals(i1Mock, t1c.getI1s().get(0));
        MockOsgi.callBindForFelixReference(t1c, i2Mock).apply(i2ref);
        assertEquals(i2Mock, t1c.getI2s().get(0));

        MockOsgi.callUnbindForFelixReference(t1c, i1Mock).apply(i1ref);
        assertEquals(0, t1c.getI1s().size());
        MockOsgi.callUnbindForFelixReference(t1c, i2Mock).apply(i2ref);
        assertEquals(0, t1c.getI2s().size());
    }

    @Test
    public void setReferecesTest() {
        T1 t1 = new T1();
        MockOsgi.setReferences(t1, i1Mock, i2Mock);
        assertEquals(i1Mock, t1.getI1s().get(0));
        assertEquals(i2Mock, t1.getI2s().get(0));

        T1 t1c = new T1C();
        MockOsgi.setReferences(t1c, i1Mock, i2Mock);
        assertEquals(i1Mock, t1c.getI1s().get(0));
        assertEquals(i2Mock, t1c.getI2s().get(0));

        T2 t2 = new T2();
        MockOsgi.setReferences(t2, i1Mock);
        assertEquals(i1Mock, t2.getI1());

    }


    public static class A1 {
        boolean activated;

        @org.apache.felix.scr.annotations.Activate
        public void activate() {
            activated = true;
        }
    }

    public static class A1C extends A1 { }

    public static class A2 {
        boolean activated;

        @org.apache.felix.scr.annotations.Activate
        public void activate(Dictionary dictionary) {
            activated = true;
        }
    }

    public static class A3 {
        boolean activated;

        @org.apache.felix.scr.annotations.Activate
        public void activate(BundleContext bundleContext) {
            activated = true;
        }
    }

    public static class A4 {
        boolean activated;

        @org.apache.felix.scr.annotations.Activate
        public void activate(ComponentContext bundleContext) {
            activated = true;
        }
    }

    public static class A5 {
        boolean activated;

        @org.apache.felix.scr.annotations.Activate
        public void activate(String invalid) {
            activated = true;
        }
    }

    public static class D1 {
        boolean deactivated;

        @org.apache.felix.scr.annotations.Deactivate
        public void detivate() {
            deactivated = true;
        }
    }

    public static class D1C extends D1 { }

    public interface I1 {
    }

    public interface I2 {
    }

    @org.apache.felix.scr.annotations.References({
            @org.apache.felix.scr.annotations.Reference(name = "I1", referenceInterface = I1.class)
    })
    @org.apache.felix.scr.annotations.Reference(name = "I2", referenceInterface = I2.class)
    @Getter
    @Setter
    public static class T1 {
        List<I1> i1s = new ArrayList<>();
        List<I2> i2s = new ArrayList<>();

        public void bindI1(I1 i1) {
            i1s.add(i1);
        }

        public void bindI2(I2 i2) {
            i2s.add(i2);
        }

        public void unbindI1(I1 i1) {
            i1s.remove(i1);
        }

        public void unbindI2(I2 i2) {
            i2s.remove(i2);
        }
    }

    @Getter
    public static class T2 {
        @org.apache.felix.scr.annotations.Reference
        I1 i1;
    }


    public static class T1C extends T1 { };

}
