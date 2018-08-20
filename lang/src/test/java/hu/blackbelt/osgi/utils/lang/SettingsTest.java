package hu.blackbelt.osgi.utils.lang;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class SettingsTest {
    public Settings settings;

    @Before
    public void setup() {
        settings = new Settings(ImmutableMap.<String, Object>builder()
                .put("a", 1)
                .put("b", "x")
                .put("qc", "p")
                .put("qc-a", "q")
                .put("qd-a", "k")
                .put("qe", "t")
                .build());
    }

    @Test(expected = NullPointerException.class)
    public void testDefaultValueNullKey() {
        settings.getEntry(null, "y");
    }

    @Test(expected = NullPointerException.class)
    public void testDefaultValueNullValue() {
        settings.getEntry("a", (Object) null);
    }

    @Test
    public void testDefaultValue() {
        assertEquals(Integer.valueOf(1), settings.getEntry("a", 2));
        assertEquals("x", settings.getEntry("b", "y"));
        assertEquals(Integer.valueOf(2), settings.getEntry("c", 2));
        assertEquals("z", settings.getEntry("d", "z"));
    }

    @Test(expected = NullPointerException.class)
    public void testResultClassNullKey() {
        settings.getEntry(null, "y");
    }

    @Test(expected = NullPointerException.class)
    public void testResultClassNullClass() {
        settings.getEntry("a", (Class) null);
    }

    @Test
    public void testResultClass() {
        assertEquals(Integer.valueOf(1), settings.getEntry("a", Integer.class));
        assertEquals("x", settings.getEntry("b", String.class));
        assertEquals(null, settings.getEntry("c", Integer.class));
        assertEquals(null, settings.getEntry("d", String.class));
    }

    @Test(expected = NullPointerException.class)
    public void testStringNullKey() {
        settings.getEntry(null);
    }

    @Test
    public void testString() {
        assertEquals("x", settings.getEntry("b"));
        assertEquals(null, settings.getEntry("d"));
    }

    @Test
    public void testQualifiedDefaultValue() {
        assertEquals("p", settings.getEntry("qc"));
        assertEquals("q", settings.getQualifiedEntry("qc", "a"));
        assertEquals(null, settings.getEntry("qd"));
        assertEquals("k", settings.getQualifiedEntry("qd", "a"));
        assertEquals("t", settings.getEntry("qe"));
        assertEquals("t", settings.getQualifiedEntry("qe", "a"));
    }

    @Test
    public void testEntries() {
        Map<String, Object> expected = ImmutableMap.<String, Object>builder()
                .put("a", 1)
                .put("b", "x")
                .put("qc", "p")
                .put("qe", "t")
                .build();
        assertEquals(expected, settings.entries());
    }

    @Test
    public void testQualifiedEntries() {
        Map<String, Object> expected = ImmutableMap.<String, Object>builder()
                .put("a", 1)
                .put("b", "x")
                .put("qc", "q")
                .put("qd", "k")
                .put("qe", "t")
                .build();
        assertEquals(expected, settings.qualifiedEntries("a"));
    }
}
