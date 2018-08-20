package hu.blackbelt.osgi.utils.lang.util;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.util.Dictionary;

import static hu.blackbelt.osgi.utils.lang.util.CollectionUtil.fromDictionary;
import static hu.blackbelt.osgi.utils.lang.util.CollectionUtil.fromPropertiesText;
import static hu.blackbelt.osgi.utils.lang.util.Dictionaries.dictionary;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class CollectionUtilTest {
    @Test
    public void fromDictionaryNull() {
        assertThat(fromDictionary(null), nullValue());
    }

    @Test
    public void fromDictionaryNormal() {
        Dictionary<String, Integer> input = dictionary("k1", 1, "k2", 2);
        assertThat(fromDictionary(input), equalTo(ImmutableMap.of("k1", 1, "k2", 2)));
    }

    @Test
    public void fromPropertiesTextNull() {
        assertThat(fromPropertiesText(null), nullValue());
    }

    @Test
    public void fromPropertiesTextEmptyString() {
        assertThat(fromPropertiesText(""), equalTo(ImmutableMap.of()));
    }

    @Test
    public void fromPropertiesTextNormal() {
        String input = "\n\r"
                + "# comment1\n"
                + "k1 : v1\r\n"
                + "! comment2\r"
                + "k2 = v\\\n"
                + "2";
        assertThat(fromPropertiesText(input), equalTo(ImmutableMap.of("k1", "v1", "k2", "v2")));
    }

    @Test
    public void fromPropertiesTextI18n() {
        String lowerStandard = "árvíztűrőtükörfúrógép";
        String upperStandard = "ÁRVÍZTŰRŐTÜKÖRFÚRÓGÉP";
        String lowerEscaped = "\u00e1rv\u00edzt\u0171r\u0151t\u00fck\u00f6rf\u00far\u00f3g\u00e9p";
        String upperEscaped = "\u00c1RV\u00cdZT\u0170R\u0150T\u00dcK\u00d6RF\u00daR\u00d3G\u00c9P";
        String inputStandard = "kl = " + lowerStandard + "\n"
                + "ku = " + upperStandard + "\n"
                + lowerStandard + " = vl\n"
                + upperStandard + " = vu\n";
        String inputEscaped = "kl = " + lowerEscaped + "\n"
                + "ku = " + upperEscaped + "\n"
                + lowerEscaped + " = vl\n"
                + upperEscaped + " = vu\n";
        ImmutableMap<Object, Object> expected = ImmutableMap.builder()
                .put("kl", lowerStandard)
                .put("ku", upperStandard)
                .put(lowerStandard, "vl")
                .put(upperStandard, "vu")
                .build();
        assertThat(fromPropertiesText(inputStandard), equalTo(expected));
        assertThat(fromPropertiesText(inputEscaped), equalTo(expected));
    }
}