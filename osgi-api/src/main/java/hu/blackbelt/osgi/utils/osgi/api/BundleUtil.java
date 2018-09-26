package hu.blackbelt.osgi.utils.osgi.api;

import com.google.common.base.Splitter;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import org.osgi.framework.Bundle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Enumeration;
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


    /**
     * Copy the given files from bundle to the bundle's persistent storage.
     * @param bundle The bundle extracted from
     * @param targetName The target path in persistent storage extract to
     * @param fileInBundle The file extracted from the bundle
     * @return The file handle of the target file
     * @throws IOException
     */
    public static File copyBundleFileToPersistentStorage(Bundle bundle, String targetName, String fileInBundle) throws IOException {
        File outFile = bundle.getDataFile(targetName);
        try (
                InputStream is = bundle.getEntry(fileInBundle).openStream();
                OutputStream os = new FileOutputStream(outFile)) {
            ByteStreams.copy(is, os);
            return outFile;
        }
    }

    /**
     * Copy the given folder from bundle to the bundle's persistent storage.
     * @param bundle The bundle extract from
     * @param targetName The target path in persistent storage extract to
     * @param pathInBundle The patch extracted from the bundle
     * @param filePattern The file pattern to filter. If null or * all files is extracted
     * @param recurse Recurse extracting, all subdirectories extracted too.
     * @return The file handle of persistent storage extracted to
     * @throws IOException
     */
    public static File copyBundlePathToPersistentStorage(Bundle bundle, String targetName, String pathInBundle, String filePattern, boolean recurse) throws IOException {
        File outFile = bundle.getDataFile("");

        String pathName = pathInBundle;
        if (!pathName.startsWith("/")) {
            pathName = "/" + pathName;
        }
        Enumeration<URL> paths = bundle.findEntries(pathInBundle, filePattern, recurse);
        while (paths.hasMoreElements()) {
            URL u = paths.nextElement();

            try (InputStream is = u.openStream()) {
                String relFileName = u.getFile().substring(pathName.length());
                File targetFile = new File(outFile.getAbsoluteFile() + File.separator + targetName + File.separator + relFileName);
                if (is.available() > 0) {
                    targetFile.getParentFile().mkdirs();
                    try (OutputStream os = new FileOutputStream(targetFile)) {
                        ByteStreams.copy(is, os);
                    }
                }
            } catch (FileNotFoundException e) {
            }
        }
        return outFile;
    }

}
