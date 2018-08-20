package hu.blackbelt.osgi.utils.lang.util;

import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class CsvUtilTest {

    @Test
    public void testCsvOutput() throws IOException {
        StringWriter sw = new StringWriter();
        CsvUtil.writeLine(sw, asList("a", "a,a", "A", "a\"a", null));
        String value = "\"a\",\"a,a\",\"A\",\"a\"\"a\",\"null\"\r\n";
        assertThat(sw.toString(), is(value));
    }
}
