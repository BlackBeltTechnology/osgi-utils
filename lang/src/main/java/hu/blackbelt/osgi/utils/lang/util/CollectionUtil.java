package hu.blackbelt.osgi.utils.lang.util;

import lombok.SneakyThrows;

import java.io.IOException;
import java.io.StringReader;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public final class CollectionUtil {
    private CollectionUtil() {
    }

    /**
     * Converts {@link Dictionary} to {@link Map}.
     *
     * @param dictionary input dictionary
     * @param <K> key type
     * @param <V> value type
     * @return map or null if the input was null
     */
    public static <K, V> Map<K, V> fromDictionary(Dictionary<K, V> dictionary) {
        if (dictionary == null) {
            return null;
        }
        Map<K, V> map = new HashMap<K, V>(dictionary.size());
        Enumeration<K> keys = dictionary.keys();
        while (keys.hasMoreElements()) {
            K key = keys.nextElement();
            map.put(key, dictionary.get(key));
        }
        return map;
    }

    /**
     * Converts {@link Properties} format text to {@link Map}.
     * @param propertiesText {@link Properties} format text
     * @return map or null if the input was null,
     *     the map is empty if no key-value pairs were found in the input
     */
    @SneakyThrows(IOException.class)
    public static Map<String, String> fromPropertiesText(String propertiesText) {
        if (propertiesText == null) {
            return null;
        }
        Properties properties = new Properties();
        properties.load(new StringReader(propertiesText));
        return fromDictionary((Dictionary) properties);
    }
}
