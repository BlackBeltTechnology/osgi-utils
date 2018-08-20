package hu.blackbelt.osgi.utils.osgi.api;

import hu.blackbelt.osgi.utils.osgi.api.BundleUtil;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.osgi.framework.Bundle;

import static hu.blackbelt.osgi.utils.lang.util.Dictionaries.dictionary;
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

}