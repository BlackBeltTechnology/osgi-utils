package hu.blackbelt.osgi.utils.osgi.api;

import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.osgi.framework.Bundle;

import java.util.Dictionary;
import java.util.Hashtable;

import static com.google.common.base.Preconditions.checkArgument;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BundleUtilTest {
    @Test
    public void testHasHeadersTrue() {
        MatcherAssert.assertThat(BundleUtil.hasHeader(header("name", "value"), "name"), is(true));
    }

    @Test
    public void testHasHeadersFalse() {
        MatcherAssert.assertThat(BundleUtil.hasHeader(emptyHeader(), "name"), is(false));
        MatcherAssert.assertThat(BundleUtil.hasHeader(header("name2", "value2"), "name"), is(false));
    }

    @Test
    public void testHeaderValues() {
        MatcherAssert.assertThat(BundleUtil.getHeaderValues(header("name", "value1"), "name"), contains("value1"));
        MatcherAssert.assertThat(BundleUtil.getHeaderValues(header("name", "value1, value2"), "name"), contains("value1", "value2"));
        MatcherAssert.assertThat(BundleUtil.getHeaderValues(header("name", "value1, value2,"), "name"), contains("value1", "value2", ""));
        MatcherAssert.assertThat(BundleUtil.getHeaderValues(header("name", "value1, value2,,"), "name"), contains("value1", "value2", "", ""));
        MatcherAssert.assertThat(BundleUtil.getHeaderValues(header("name", "value1, value2,,,"), "name"), contains("value1", "value2", "", "", ""));
    }
    @Test
    public void testHeaderValuesEmpty() {
        MatcherAssert.assertThat(BundleUtil.getHeaderValues(emptyHeader(), "name1"), hasSize(0));
        MatcherAssert.assertThat(BundleUtil.getHeaderValues(header("name", "value1"), "name1"), hasSize(0));
    }

    private Bundle emptyHeader() {
        Bundle bundle = mock(Bundle.class);
        when(bundle.getHeaders()).thenReturn(dictionary());
        return bundle;
    }

    private Bundle header(String key, String value) {
        Bundle bundle = mock(Bundle.class);
        when(bundle.getHeaders()).thenReturn(dictionary(key, value));
        return bundle;
    }

    private static <K, V> Dictionary<K, V> dictionary(K key, V value) {
        Dictionary<K, V> result = new Hashtable<>();
        result.put(key, value);
        return result;
    }

    private static <K, V> Dictionary<K, V> dictionary(Object... keysAndValues) {
        checkArgument(keysAndValues.length % 2 == 0, "Equal number of key-value pairs expected.");
        Dictionary<K, V> result = new Hashtable<>();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            result.put((K) keysAndValues[i], (V) keysAndValues[i + 1]);
        }
        return result;
    }
}