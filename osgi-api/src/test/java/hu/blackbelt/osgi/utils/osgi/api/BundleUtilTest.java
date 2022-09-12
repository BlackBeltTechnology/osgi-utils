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

import com.google.common.collect.ImmutableMap;
import org.hamcrest.MatcherAssert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.osgi.framework.Bundle;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BundleUtilTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

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
    public void testInvalidHeaderEntries() {
        thrown.expect(IllegalArgumentException.class);
        BundleUtil.getHeaderEntries(header("name", "value1"), "name");
    }

    @Test
    public void testHeaderEntries() {
        MatcherAssert.assertThat(
                BundleUtil.getHeaderEntries(header("name", "key=value"), "name").stream().map(Map::entrySet).collect(Collectors.toList()),
                hasItems(
                        hasItems(ImmutableMap.of("key", "value").entrySet().toArray(new Map.Entry[1]))
                ));

        MatcherAssert.assertThat(
                BundleUtil.getHeaderEntries(header("name", "key=value;key2=value2"), "name").stream().map(Map::entrySet).collect(Collectors.toList()),
                hasItem(
                        hasItems(ImmutableMap.of("key", "value", "key2", "value2").entrySet().toArray(new Map.Entry[2]))
                ));

        MatcherAssert.assertThat(
                BundleUtil.getHeaderEntries(header("name", "key=value;key2=value2,key3=value3;key4=value4"), "name").stream().map(Map::entrySet).collect(Collectors.toList()),
                hasItems(
                        hasItems(ImmutableMap.of("key", "value", "key2", "value2").entrySet().toArray(new Map.Entry[2])),
                        hasItems(ImmutableMap.of("key3", "value3", "key4", "value4").entrySet().toArray(new Map.Entry[2]))
                ));

        MatcherAssert.assertThat(
                BundleUtil.getHeaderEntries(header("name", "file=model/northwind.psm;version=1.0.0-SNAPSHOT;name=Northwind;checksum=831725387bbfca86abdac6c382cd8326;meta-version=\"[1.0,1.1)\""), "name").stream().map(Map::entrySet).collect(Collectors.toList()),
                hasItems(
                        hasItems(ImmutableMap.of("file", "model/northwind.psm", "version", "1.0.0-SNAPSHOT", "name", "Northwind", "checksum", "831725387bbfca86abdac6c382cd8326", "meta-version", "\"[1.0,1.1)\"").entrySet().toArray(new Map.Entry[5]))
                ));



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
