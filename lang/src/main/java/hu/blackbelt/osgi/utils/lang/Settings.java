package hu.blackbelt.osgi.utils.lang;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.util.Map;
import java.util.TreeMap;

import static com.google.common.base.Preconditions.checkNotNull;
import static hu.blackbelt.osgi.utils.lang.util.ConversionUtil.convert;
import static java.lang.String.format;

/**
 * Represents settings which consists of entries.
 */
@Slf4j
@Immutable
@EqualsAndHashCode
public class Settings {
    private final ImmutableMap<String, ?> map;
    private final String toString;

    /**
     * Creates a settings from a String keyed map.
     * Subsequent modifications of the input map has no effect on this {@link Settings}.
     *
     * @param map settings source
     */
    public Settings(@Nonnull Map<String, ?> map) {
        this.map = ImmutableMap.copyOf(map);
        this.toString = new TreeMap<>(this.map).toString();
    }

    /**
     * Retrieves an entry, returns the default value if not found and converts it to the default value type if found.
     *
     * @param key settings entry key
     * @param defaultValue default value
     * @param <T> result/default value type
     * @return the converted entry if found, default value otherwise
     */
    @Nonnull public <T> T getEntry(@Nonnull String key, @Nonnull T defaultValue) {
        checkNotNull(key, "The 'key' is mandatory.");
        checkNotNull(defaultValue, "The 'defaultValue' is mandatory.");
        T value = getEntry(key, (Class<T>) defaultValue.getClass());
        return value == null ? defaultValue : value;
    }

    /**
     * Retrieves an entry and converts it to the provided type.
     *
     * @param key settings entry key
     * @param type result type
     * @param <T> result type
     * @return the converted entry if found, null otherwise
     */
    public <T> T getEntry(@Nonnull String key, @Nonnull Class<T> type) {
        checkNotNull(key, "The 'key' is mandatory.");
        checkNotNull(type, "The 'type' is mandatory.");
        return convert(map.get(key), type);
    }

    /**
     * Retrieves an entry and converts it to {@link String}.
     *
     * @param key setting entry key
     * @return the converted entry if found, null otherwise
     */
    public String getEntry(@Nonnull String key) {
        return getEntry(key, String.class);
    }

    /**
     * Retrieves an entry, returns the default value if not found and converts it to the default value type if found.
     *
     * @param key settings entry key
     * @param qualifier key qualifier
     * @param defaultValue default value
     * @param <T> result/default value type
     * @return the converted entry if found, default value otherwise
     */
    @Nonnull public <T> T getQualifiedEntry(@Nonnull String key, @Nonnull String qualifier, @Nonnull T defaultValue) {
        checkNotNull(key, "The 'key' is mandatory.");
        checkNotNull(defaultValue, "The 'defaultValue' is mandatory.");
        T value = getQualifiedEntry(key, qualifier, (Class<T>) defaultValue.getClass());
        return value == null ? defaultValue : value;
    }

    /**
     * Retrieves an entry and converts it to the provided type.
     *
     * @param key settings entry key
     * @param qualifier key qualifier
     * @param type result type
     * @param <T> result type
     * @return the converted entry if found, null otherwise
     */
    public <T> T getQualifiedEntry(@Nonnull String key, @Nonnull String qualifier, @Nonnull Class<T> type) {
        checkNotNull(key, "The 'key' is mandatory.");
        checkNotNull(type, "The 'type' is mandatory.");
        Object value = map.get(format("%s-%s", key, qualifier));
        if (value == null) {
            value = map.get(key);
        }
        return convert(value, type);
    }

    /**
     * Retrieves an entry and converts it to {@link String}.
     *
     * @param key setting entry key
     * @param qualifier key qualifier
     * @return the converted entry if found, null otherwise
     */
    public String getQualifiedEntry(@Nonnull String key, @Nonnull String qualifier) {
        return getQualifiedEntry(key, qualifier, String.class);
    }

    /**
     * Entries without qualifier.
     *
     * @return entries without qualifier
     */
    public ImmutableMap<String, ?> entries() {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (!key.contains("-")) {
                builder.put(key, value);
            }
        }
        return builder.build();
    }

    /**
     * Entries with the specified qualifier.
     * @param qualifier qualifier
     * @return entries with qualifier
     */
    public ImmutableMap<String, ?> qualifiedEntries(String qualifier) {
        Map<String, Object> merged = Maps.newHashMap(entries());
        String suffix = "-" + qualifier;
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (key.endsWith(suffix)) {
                merged.put(key.substring(0, key.length() - suffix.length()), value);
            }
        }
        return ImmutableMap.copyOf(merged);
    }

    @Override
    public String toString() {
        return toString;
    }
}
