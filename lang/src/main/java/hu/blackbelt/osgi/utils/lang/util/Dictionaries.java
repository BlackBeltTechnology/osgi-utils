package hu.blackbelt.osgi.utils.lang.util;

import java.util.Dictionary;
import java.util.Hashtable;

import static com.google.common.base.Preconditions.checkArgument;

public final class Dictionaries {
    private Dictionaries() {
    }

    public static <K, V> Dictionary<K, V> dictionary(K key, V value) {
        Dictionary<K, V> result = new Hashtable<>();
        result.put(key, value);
        return result;
    }

    public static <K, V> Dictionary<K, V> dictionary(Object... keysAndValues) {
        checkArgument(keysAndValues.length % 2 == 0, "Equal number of key-value pairs expected.");
        Dictionary<K, V> result = new Hashtable<>();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            result.put((K) keysAndValues[i], (V) keysAndValues[i + 1]);
        }
        return result;
    }
}
