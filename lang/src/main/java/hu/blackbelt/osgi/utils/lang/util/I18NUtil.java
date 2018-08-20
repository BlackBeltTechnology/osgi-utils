package hu.blackbelt.osgi.utils.lang.util;

import java.util.Locale;

public class I18NUtil {

    private I18NUtil() {
    }

    public static Locale getLocaleFromBCP47(String localeString) {
        // Locale.toString() generates strings like "en_US" and "zh_CN_#Hans".
        // Locale.toLanguageTag() generates strings like "en-US" and "zh-Hans-CN".
        // We can only parse language tags.
        if (localeString != null && localeString.indexOf('_') == -1) {
            final String[] locales = localeString.split(";");
            return Locale.forLanguageTag(locales[0]);
        }
        return null;
    }
}
