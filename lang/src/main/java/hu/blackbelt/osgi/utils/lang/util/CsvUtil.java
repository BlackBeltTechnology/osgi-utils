package hu.blackbelt.osgi.utils.lang.util;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

public final class CsvUtil {

    private CsvUtil() {
    }

    private static final String DEFAULT_QUOTE = "\"";
    private static final String DEFAULT_SEPARATOR = ",";

    private static String followCVSformat(String value) {
        String result = value;
        if (result.contains(DEFAULT_QUOTE)) {
            result = result.replace(DEFAULT_QUOTE, "\"\"");
        }
        return result;

    }

    public static void writeLine(Writer w, List<String> values) throws IOException {
        boolean first = true;
        StringBuilder sb = new StringBuilder();
        for (String value : values) {
            if (!first) {
                sb.append(DEFAULT_SEPARATOR);
            }
            sb.append(DEFAULT_QUOTE).append(followCVSformat(String.valueOf(value))).append(DEFAULT_QUOTE);

            first = false;
        }
        sb.append("\r\n");
        w.append(sb.toString());
    }
}
