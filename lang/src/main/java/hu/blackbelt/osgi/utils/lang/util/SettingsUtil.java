package hu.blackbelt.osgi.utils.lang.util;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.StandardSystemProperty;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import hu.blackbelt.osgi.utils.lang.Settings;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.charactersOf;
import static com.google.common.collect.Maps.fromProperties;
import static com.google.common.collect.Maps.newHashMap;
import static java.lang.System.lineSeparator;

/**
 * <p>
 *     Loads {@link Settings} by merging user provided defaults, environment variables and system properties.
 * </p>
 * <p>
 *     <b>Precedence and transformation</b>
 *     <ol>
 *         <li>The user provided defaults gets the least precedence and no transformation performed.</li>
 *         <li>The environment variables overrides the user provided defaults and the keys are transformed:
 *         <ol>
 *             <li>only the "environmentVariablePrefix" prefixed environment variables processed</li>
 *             <li>the "environmentVariablePrefix" prefix is removed</li>
 *             <li>and the upper underscore is converted to lower camelcase</li>
 *             <li>the value is not transformed</li>
 *         </ol>
 *         </li>
 *         <li>The system properties overrides both previous sets and transformed:
 *         <ol>
 *             <li>only the "systemPropertyPrefix". prefixed or the {@link #DEFAULT_SYSTEM_PROPERTIES} system properties processed</li>
 *             <li>the "systemPropertyPrefix". prefix is removed</li>
 *             <li>the lower dot is converted to lower camelcase</li>
 *             <li>the value is not transformed</li>
 *             <li>the "systemPropertyPrefix". prefixed can override the {@link #DEFAULT_SYSTEM_PROPERTIES} system properties
 *             (e.g.: "systemPropertyPrefix"..user.home overrides user.home</li>
 *         </ol>
 *         </li>
 *     </ol>
 * </p>
 * <p>
 *     Qualifiers are allowed after the hyphen ('-') character.
 * </p>
 * <p>
 *     Detailed logging provided for the debug log level.
 * </p>
 */
@Slf4j
public final class SettingsUtil {
    private static final Joiner.MapJoiner MAP_JOINER;
    private static final ImmutableSet<String> DEFAULT_SYSTEM_PROPERTIES;
    // private static final Settings SYSTEM_SETTINGS;
    private static final Pattern VALID_KEY = Pattern.compile("[a-zA-Z0-9]*(-[a-zA-Z0-9]*)?");

    static {
        MAP_JOINER  = Joiner.on(lineSeparator()).withKeyValueSeparator(" = ");
        ImmutableSet.Builder<String> builder = ImmutableSet.builder();
        for (StandardSystemProperty standardSystemProperty : StandardSystemProperty.values()) {
            builder.add(standardSystemProperty.key());
        }
        DEFAULT_SYSTEM_PROPERTIES = builder.build();
        // must be the last line!
        // SYSTEM_SETTINGS = createSettings();
    }

    private SettingsUtil() {
    }

    /* public static Settings systemSettings() {
        return SYSTEM_SETTINGS;
    } */

    @Nonnull public static Settings createSettings(@NonNull String environmentVariablePrefix, @NonNull String systemPropertyPrefix, @Nonnull  Object... defaultValuesList) {
        return createSettings(environmentVariablePrefix, systemPropertyPrefix, mergeDefaultValues(defaultValuesList));
    }

    @Nonnull public static Settings createSettings(@NonNull String environmentVariablePrefix, @NonNull String systemPropertyPrefix, @Nonnull Map<String, ?> defaultValues) {
        return new Settings(merge(environmentVariablePrefix, systemPropertyPrefix, System.getenv(), fromProperties(System.getProperties()), defaultValues));
    }

    @Nonnull public static Settings createSettings(@NonNull String environmentVariablePrefix, @NonNull String systemPropertyPrefix, @Nonnull Properties defaultValues) {
        return createSettings(environmentVariablePrefix, systemPropertyPrefix, fromProperties(defaultValues));
    }

    @Nonnull public static Settings createSettings(@NonNull String environmentVariablePrefix, @NonNull String systemPropertyPrefix, @Nonnull Dictionary<String, ?> defaultValues) {
        return createSettings(environmentVariablePrefix, systemPropertyPrefix, CollectionUtil.fromDictionary(defaultValues));
    }

    @Nonnull public static Settings createSettings(@NonNull String environmentVariablePrefix, @NonNull String systemPropertyPrefix) {
        return createSettings(environmentVariablePrefix, systemPropertyPrefix, new HashMap<String, Object>());
    }

    @VisibleForTesting
    @Nonnull static ImmutableMap<String, ?> mergeDefaultValues(@Nonnull Object... defaultValuesList) {
        Map<String, Object> mergedDefaultValues = newHashMap();
        log.debug("Merging {} default value maps.", defaultValuesList.length);
        for (Object defaultValues : defaultValuesList) {
            log.debug("Merging default value map.");
            if (defaultValues instanceof Map) {
                Map map = (Map) defaultValues;
                log.debug(MAP_JOINER.join(map));
                mergedDefaultValues.putAll(map);
            } else if (defaultValues instanceof Dictionary) {
                Map map = CollectionUtil.fromDictionary((Dictionary) defaultValues);
                log.debug(MAP_JOINER.join(map));
                mergedDefaultValues.putAll(map);
            } else {
                throw new IllegalArgumentException("The argument must contain Map, Properties or Dictionary instances only.");
            }
        }
        return ImmutableMap.copyOf(mergedDefaultValues);
    }

    @VisibleForTesting
    @Nonnull static Map<String, ?> merge(String environmentVariablePrefix, String systemPropertyPrefix, @Nonnull Map<String, String> environmentVariables, @Nonnull Map<String, String> systemProperties,
                                   @Nonnull Map<String, ?> defaultValues) {
        if (log.isDebugEnabled()) {
            log.debug("Default values:{}{}", lineSeparator(), MAP_JOINER.join(defaultValues));
        }
        if (log.isDebugEnabled()) {
            log.debug("Environment variables:{}{}", lineSeparator(), MAP_JOINER.join(environmentVariables));
        }
        Map<String, String> transformedEnvironmentVariables = filterAndTransformEnvironmentVariables(environmentVariables, environmentVariablePrefix);
        if (log.isDebugEnabled()) {
            log.debug("Filtered and transformed environment variables:{}{}", lineSeparator(), MAP_JOINER.join(transformedEnvironmentVariables));
        }
        if (log.isDebugEnabled()) {
            log.debug("System properties:{}{}", lineSeparator(), MAP_JOINER.join(systemProperties));
        }
        Map<String, String> transformedSystemProperties = filterAndTransformSystemProperties(systemProperties, systemPropertyPrefix);
        if (log.isDebugEnabled()) {
            log.debug("Filtered and transformed system properties:{}{}", lineSeparator(), MAP_JOINER.join(transformedSystemProperties));
        }
        Map<String, Object> result = newHashMap();
        result.putAll(checkDefaultValueKeys(defaultValues));
        result.putAll(transformedEnvironmentVariables);
        result.putAll(transformedSystemProperties);
        if (log.isDebugEnabled()) {
            log.debug("Merged settings:{}{}", lineSeparator(), MAP_JOINER.join(result));
        }
        return result;
    }

    private static Map<String, ?> checkDefaultValueKeys(Map<String, ?> defaultValues) {
        for (String key : defaultValues.keySet()) {
            checkArgument(VALID_KEY.matcher(key).matches(), "Invalid default value key %s", key);
        }
        return defaultValues;
    }

    private static Map<String, String> filterAndTransformEnvironmentVariables(Map<String, String> environmentVariables, String environmentVariablePrefix) {
        Map<String, String> result = newHashMap();
        for (Map.Entry<String, String> environmentVariable : environmentVariables.entrySet()) {
            String key = environmentVariable.getKey();
            String originalKey = key;
            String value = environmentVariable.getValue();
            key = key.toLowerCase();
            if (key.startsWith(environmentVariablePrefix + "_")) {
                // JUDO_ prefix removed
                key = key.substring((environmentVariablePrefix + "_").length());
                // converted to camel case
                key = UPPER_UNDERSCORE.to(LOWER_CAMEL, key);
                checkArgument(VALID_KEY.matcher(key).matches(), "Invalid environment variable key %s", originalKey);
                result.put(key, value);
                continue;
            }
        }
        return result;
    }

    private static Map<String, String> filterAndTransformSystemProperties(Map<String, String> systemProperties, String systemPropertyPrefix) {
        Map<String, String> result = newHashMap();
        for (Map.Entry<String, String> systemProperty : systemProperties.entrySet()) {
            String key = systemProperty.getKey();
            String value = systemProperty.getValue();
            if (DEFAULT_SYSTEM_PROPERTIES.contains(key)) {
                // change dot to camel case
                result.put(dotToCamelCase(key), value);
                continue;
            }
        }
        // judo. can override system defaults
        for (Map.Entry<String, String> systemProperty : systemProperties.entrySet()) {
            String key = systemProperty.getKey();
            String originalKey = key;
            String value = systemProperty.getValue();
            if (key.startsWith(systemPropertyPrefix + ".")) {
                // judo. prefix removed
                key = key.substring((systemPropertyPrefix + ".").length());
                key = dotToCamelCase(key);
                checkArgument(VALID_KEY.matcher(key).matches(),
                        "Invalid system property key %s", originalKey);
                result.put(key, value);
                continue;
            }
        }
        return result;
    }

    private static String dotToCamelCase(String input) {
        StringBuilder sb = new StringBuilder(input.length());
        boolean previousWasDot = false;
        for (char c : charactersOf(input)) {
            if (c == '.') {
                previousWasDot = true;
            } else {
                if (previousWasDot) {
                    previousWasDot = false;
                    sb.append(Character.toUpperCase(c));
                } else {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }
}
