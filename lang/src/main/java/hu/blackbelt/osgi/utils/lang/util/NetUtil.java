package hu.blackbelt.osgi.utils.lang.util;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Network related utils. E.g.: URL creation
 */
public final class NetUtil {
    private NetUtil() {

    }

    /**
     * Creates an URL without throwing the checked {@link MalformedURLException}.
     * Useful for creating urls from constants which are always valid.
     *
     * @param source url string form
     * @return parsed url
     *
     * @throws IllegalArgumentException in case of {@link MalformedURLException} occurs
     */
    public static URL createUrl(String source) {
        try {
            return new URL(source);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }

    }
}
