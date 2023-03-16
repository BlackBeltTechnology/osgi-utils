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

import com.google.common.base.Charsets;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(PaxExam.class)
@Slf4j
public class UnpackFilesFromBundleTest {

    @Inject
    BundleContext bundleContext;

    @Configuration
    public Option[] config() {
        return CoreOptions.options(
                CoreOptions.cleanCaches(),

                CoreOptions.mavenBundle().groupId("javax.annotation").artifactId("javax.annotation-api").version("1.3.1"),
                CoreOptions.mavenBundle().groupId("com.google.guava").artifactId("guava").version("27.1-jre"),
                CoreOptions.mavenBundle().groupId("com.google.guava").artifactId("failureaccess").version("1.0.1"),

                CoreOptions.bundle("reference:file:target/classes"),
                CoreOptions.bundle("reference:file:target/test-classes"),

                // JUnit, Hamcrast
                CoreOptions.mavenBundle().groupId( "org.apache.servicemix.bundles" ).artifactId( "org.apache.servicemix.bundles.hamcrest" ).version( "1.3_1"),
                CoreOptions.junitBundles());
    }


    @Test
    public void testUnpackBundleFileFromBundle() throws IOException {
        Bundle bundle = getBundle(bundleContext, "osgi-api-test");

        assertThat(bundle, notNullValue());

        File f = BundleUtil.copyBundleFileToPersistentStorage(bundle, "test1", "test-dir1/test-content1.txt");

        List<String> lines = Files.readAllLines(Paths.get(f.getAbsolutePath()), Charsets.UTF_8);

        assertThat("test1", equalTo(lines.get(0)));

    }

    @Test
    public void testUnpackBundlePathFromBundle() throws IOException {
        Bundle bundle = getBundle(bundleContext, "osgi-api-test");

        assertThat(bundle, notNullValue());

        File f = BundleUtil.copyBundlePathToPersistentStorage(bundle, "test1", "test-dir1/test-dir2", "*", true);

        assertThat(listDir(new ArrayList<>(), f), hasItems("test-content2.txt", "test-content3.txt"));

        List<String> lines = Files.readAllLines(Paths.get(f.getAbsolutePath() + File.separator + "test1" +  File.separator + "test-content2.txt"), Charsets.UTF_8);

        assertThat("test2", equalTo(lines.get(0)));

    }


    private Bundle getBundle(BundleContext ctx, String symbolicName) {
        for(Bundle b : ctx.getBundles()) {
            if(symbolicName.equals(b.getSymbolicName())) {
                return b;
            }
        }
        return null;
    }


    private List<String> listDir(List<String> res, File dir) {
        for (File file : dir.listFiles()) {
            if (file.isFile()) {
                res.add(file.getName());
            } else {
                listDir(res, file);
            }
        }
        return res;
    }
}
