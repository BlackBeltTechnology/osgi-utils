package hu.blackbelt.osgi.utils.osgi.api;

/*-
 * #%L
 * OSGi utils API
 * %%
 * Copyright (C) 2018 - 2022 BlackBelt Technology
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Bundle related utils (eg: header query).
 */
public final class BundleUtil {
    public static final String HEADER_VALUE_SEPARATOR  = ",(?=(?:[^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)";
    public static final String HEADER_PARAM_SEPARATOR  = ";(?=(?:[^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)";
    public static final String HEADER_KEYVAL_SEPARATOR = "=(?=(?:[^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)";

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
                    .on(",")
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
                if (is != null && is.available() > 0) {
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


    /**
     * Parese the given manifest header entries to a list of entries. The format of entry is key1_1=val1_1;key1_2=val2_1,key2_1=val2_1;key2_1=val2_2,
     * where the coma separated entries will be one entry in list, and the key values came as the map entries. If the header is not found returns null.
     * @param bundle The bundle which is used to gen header
     * @param headerEntryName
     * @return list of entries when header is presented or null when header is not found
     */
    public static List<Map<String, String>> getHeaderEntries(Bundle bundle, String headerEntryName) {

        if (!hasHeader(bundle, headerEntryName)) {
            return null;
        }
        List<Map<String, String>> headerEntries = new ArrayList<>();
        String value = bundle.getHeaders().get(headerEntryName);
        for (String headerKeyValue : value.split(HEADER_VALUE_SEPARATOR)) {
            Map<String, String> entry = new HashMap<>();
            if (headerKeyValue != null && !"".equals(headerKeyValue.trim())) {
                for (String keyVal : headerKeyValue.split(HEADER_PARAM_SEPARATOR)) {
                    if (keyVal != null && !"".equals(keyVal.trim())) {
                        String[] keyAndVal = keyVal.split(HEADER_KEYVAL_SEPARATOR);
                        if (keyAndVal.length != 2) {
                            throw new IllegalArgumentException(headerEntryName + "header have to be in the following format: key1_1=val1_1;key1_2=val2_1,key2_1=val2_1;key2_1=val2_2");
                        }
                        entry.put(keyAndVal[0].trim(), keyAndVal[1].trim());
                    }
                }
                headerEntries.add(entry);
            }
        }
        return headerEntries;
    }

}
