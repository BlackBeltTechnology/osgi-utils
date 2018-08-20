package hu.blackbelt.osgi.utils.osgi.api;

import com.google.common.base.Splitter;
import org.osgi.framework.Bundle;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Bundle related utils (eg: header query).
 */
public final class BundleUtil {
    public static final String HEADER_VALUE_SEPARATOR = ",";

    private BundleUtil() {
    }

    /**
     * Checks that the header is set on the bundle.
     * @param bundle bundle
     * @param headerName the header's name
     * @return if the header is set on the bundle
     */
    public static boolean hasHeader(Bundle bundle, String headerName) {
        return bundle.getHeaders().get(headerName) != null;
    }

    /**
     * Returns the comma separated header values as list.
     * @param bundle bundle
     * @param headerName the header's name
     * @return list of values or an empty list if no value found
     */
    public static List<String> getHeaderValues(Bundle bundle, String headerName) {
        List<String> results = newArrayList();
        String value = bundle.getHeaders().get(headerName);
        if (value != null) {
            results = newArrayList(Splitter
                    .on(HEADER_VALUE_SEPARATOR)
                    .trimResults()
                    .split(value));
        }
        return results;
    }

    /**
     * Checks non-recursively for the file pattern in the supplied path.
     * @param bundle bundle
     * @param path path
     * @param filePattern file pattern (* and trailing slashes for directories)
     * @return
     */
    public static boolean hasResources(Bundle bundle, String path, String filePattern) {
        return hasResources(bundle, path, filePattern, false);
    }

    /**
     * Checks for the file pattern in the supplied path.
     * @param bundle bundle
     * @param path path
     * @param filePattern file pattern (* and trailing slashes for directories)
     * @param recursive the check should be recursive or not
     * @return
     */
    public static boolean hasResources(Bundle bundle, String path, String filePattern, boolean recursive) {
        return bundle.findEntries(path, filePattern, recursive) != null;
    }
}
