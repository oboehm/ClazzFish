/*
 * Copyright (c) 2017-2018 by Oliver Boehm
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express orimplied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * (c)reated 24.01.2017 by oboehm (ob@oasd.de)
 */
package clazzfish.monitor.util;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link NestedZipFile} class.
 *
 * @author oboehm
 */
class NestedZipFileTest {

    private static final Logger LOG = LoggerFactory.getLogger(NestedZipFileTest.class);
    private static final File WORLD_WAR = new File("src/test/resources/clazzfish/monitor/util/world.war");
    private static final File WORLD_EAR = new File("src/test/resources/clazzfish/monitor/util/world.ear");

    /**
     * Test method for {@link NestedZipFile#toString()}.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testToString() throws IOException {
        try (NestedZipFile zipFile = new NestedZipFile("src/test/resources/clazzfish/monitor/util/world.war")) {
            String s = zipFile.toString();
            LOG.info("s = \"{}\"", s);
            assertThat(s, containsString(zipFile.getName()));
        }
    }

    /**
     * Here we test if we can read a normal ZIP file
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testNormalZip() throws IOException {
        listContentOf(WORLD_WAR);
    }

    /**
     * Here we test if we can read a JAR file inside a WAR file.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testNestedWar() throws IOException {
        File nestedZipFile = new File(WORLD_WAR, "!/WEB-INF/lib/jcl-over-slf4j-1.7.21.jar");
        listContentOf(nestedZipFile);
    }

    /**
     * Here we test if we can read a JAR file inside a WAR inside an EAR file.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testNestedEar() throws IOException {
        File nestedZipFile = new File(WORLD_EAR, "!/world.war!/WEB-INF/lib/patterntesting-agent-1.6.3.jar");
        listContentOf(nestedZipFile);
    }

    /**
     * For a non existing entry in a nested ZIP file a
     * {@link FileNotFoundException} is expected
     */
    @Test
    public void testNestedZipNotExisting() {
        assertThrows(FileNotFoundException.class, () -> {
            File notExistingZip = new File(WORLD_WAR, "!/not/existing.zip");
            listContentOf(notExistingZip);
        });
    }

    private static void listContentOf(File zipFile) throws IOException {
        try (NestedZipFile nested = new NestedZipFile(zipFile)) {
            LOG.info("Reading {}...", nested);
            Enumeration<? extends ZipEntry> entries = nested.entries();
            assertTrue(entries.hasMoreElements(), "entries in " + nested + " expected");
            while(entries.hasMoreElements()) {
                LOG.info("ZipFile has entry {}.", entries.nextElement());
            }
            LOG.info("Reading {} successfully finished.", nested);
        }
    }

    /**
     * This method for {@link NestedZipFile#NestedZipFile(File, int)}.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testNestedZipFileMode() throws IOException {
        File warFile = new File(WORLD_EAR, "!/world.war");
        try (NestedZipFile nestedWarFile = new NestedZipFile(warFile, ZipFile.OPEN_READ)) {
            assertTrue(nestedWarFile.entries().hasMoreElements(), "entries in " + nestedWarFile + " expected");
        }
    }

}
