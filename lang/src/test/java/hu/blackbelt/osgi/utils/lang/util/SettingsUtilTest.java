package hu.blackbelt.osgi.utils.lang.util;

import com.google.common.collect.ImmutableMap;
import hu.blackbelt.osgi.utils.lang.Settings;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static hu.blackbelt.osgi.utils.lang.util.SettingsUtil.merge;
import static org.junit.Assert.assertEquals;

public class SettingsUtilTest {
    public static final HashMap<String, Object> EMPTY_DEFAULT_VALUES = new HashMap<>();
    public static final HashMap<String, String> EMPTY_ENVIRONMENT_VARIABLES = new HashMap<>();
    public static final HashMap<String, String> EMPTY_SYSTEM_PROPERTIES = new HashMap<>();

    public static final String ENV_PREFIX = "envprefix";
    public static final String SYSTEM_PREFIX = "systemprefix";

    Map<String, String> environmentVariables = ImmutableMap.of("UPPER_LOWER_CASE1", "value_e1", "ENVPREFIX_UPPER_LOWER_CASE2", "value_e2");
    Map<String, String> systemProperties = ImmutableMap.of("key1", "value_s1", "systemprefix.key2", "value_s2", "user.home", "value_s3");
    Map<String, String> defaultValues =
            ImmutableMap.of("upperLowerCase1", "value_d1", "upperLowerCase2", "value_d2", "key1", "value_d3", "key2", "value_d4");

    @Test
    public void testNoDefaults() {
        Settings settings = new Settings(merge(ENV_PREFIX, SYSTEM_PREFIX, environmentVariables, systemProperties, EMPTY_DEFAULT_VALUES));
        ImmutableMap<String, String> expected = ImmutableMap.<String, String>builder()
                .put("upperLowerCase2", "value_e2")
                .put("key2", "value_s2")
                .put("userHome", "value_s3")
                .build();
        assertEquals(new Settings(expected), settings);
    }

    @Test
    public void testDefaults() {
        Settings settings = new Settings(merge(ENV_PREFIX, SYSTEM_PREFIX, environmentVariables, systemProperties, defaultValues));
        ImmutableMap<String, String> expected = ImmutableMap.<String, String>builder()
                .put("upperLowerCase1", "value_d1")
                .put("upperLowerCase2", "value_e2")
                .put("key1", "value_d3")
                .put("key2", "value_s2")
                .put("userHome", "value_s3")
                .build();
        assertEquals(new Settings(expected), settings);
    }

    @Test
    public void testValidSystemProperties() {
        Settings settings = new Settings(merge(ENV_PREFIX, SYSTEM_PREFIX, EMPTY_ENVIRONMENT_VARIABLES, ImmutableMap.of(
                "systemprefix.camel.case1", "value1",
                "systemprefix.camel.case1-qualifier1", "value2",
                "systemprefix.camel.case1-qualifier.1", "value3",
                "systemprefix.camelCase2", "value4"
        ), EMPTY_DEFAULT_VALUES));
        ImmutableMap<String, String> expected = ImmutableMap.<String, String>builder()
                .put("camelCase1", "value1")
                .put("camelCase1-qualifier1", "value3")
                .put("camelCase2", "value4")
                .build();
        assertEquals(new Settings(expected), settings);
    }

    @Test
    public void testStandardSystemProperties() {
        Settings settings = new Settings(merge(ENV_PREFIX, SYSTEM_PREFIX, EMPTY_ENVIRONMENT_VARIABLES, ImmutableMap.of("user.home", "value1"), EMPTY_DEFAULT_VALUES));
        ImmutableMap<String, String> expected = ImmutableMap.<String, String>builder()
                .put("userHome", "value1")
                .build();
        assertEquals(new Settings(expected), settings);
    }
    @Test(expected = IllegalArgumentException.class)
    public void testInvalidSystemPropertiesDoubleHyphen() {
        merge(ENV_PREFIX, SYSTEM_PREFIX, EMPTY_ENVIRONMENT_VARIABLES, ImmutableMap.of("systemprefix.alpha-beta-delta", "value1"), EMPTY_DEFAULT_VALUES);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidSystemPropertiesUnderscore() {
        merge(ENV_PREFIX, SYSTEM_PREFIX, EMPTY_ENVIRONMENT_VARIABLES, ImmutableMap.of("systemprefix.alpha_beta", "value1"), EMPTY_DEFAULT_VALUES);
    }

    @Test
    public void testValidEnvironmentVariables() {
        Settings settings = new Settings(merge(ENV_PREFIX, SYSTEM_PREFIX, ImmutableMap.of(
                "ENVPREFIX_CAMEL_CASE_1", "value1",
                "ENVPREFIX_CAMEL_CASE1-QUALIFIER_1", "value2",
                "ENVPREFIX_CAMEL_CASE_1-QUALIFIER_1", "value3",
                "ENVPREFIX_camelCase2", "value4"),
                EMPTY_SYSTEM_PROPERTIES, EMPTY_DEFAULT_VALUES));
        ImmutableMap<String, String> expected = ImmutableMap.<String, String>builder()
                .put("camelCase1", "value1")
                .put("camelCase1-qualifier1", "value3")
                .put("camelcase2", "value4")
                .build();
        assertEquals(new Settings(expected), settings);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidEnvironmentVariableDot() {
        merge(ENV_PREFIX, SYSTEM_PREFIX, ImmutableMap.of("ENVPREFIX_A.B", "value1"), EMPTY_SYSTEM_PROPERTIES, EMPTY_DEFAULT_VALUES);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidEnvironmentVariableDoubleHyphen() {
        merge(ENV_PREFIX, SYSTEM_PREFIX, ImmutableMap.of("ENVPREFIX_ALPHA-BETA-DELTA", "value1"), EMPTY_SYSTEM_PROPERTIES, EMPTY_DEFAULT_VALUES);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidDefaultValuesDot() {
        merge(ENV_PREFIX, SYSTEM_PREFIX, EMPTY_ENVIRONMENT_VARIABLES, EMPTY_SYSTEM_PROPERTIES, ImmutableMap.of("a.b", "value1"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidDefaultValuesUnderscore() {
        merge(ENV_PREFIX, SYSTEM_PREFIX, EMPTY_ENVIRONMENT_VARIABLES, EMPTY_SYSTEM_PROPERTIES, ImmutableMap.of("a_b", "value1"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidDefaultValuesDoubleHyphen() {
        merge(ENV_PREFIX, SYSTEM_PREFIX, EMPTY_ENVIRONMENT_VARIABLES, EMPTY_SYSTEM_PROPERTIES, ImmutableMap.of("a-b-c", "value1"));
    }

    @Test
    public void testStandardSystemPropertiesOverride() {
        Settings settings = new Settings(merge(ENV_PREFIX, SYSTEM_PREFIX, EMPTY_ENVIRONMENT_VARIABLES,
                ImmutableMap.of("user.home", "value1", "systemprefix.user.home", "value2"), EMPTY_DEFAULT_VALUES));
        assertEquals(new Settings(ImmutableMap.of("userHome", "value2")), settings);
    }
}
