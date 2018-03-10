/*
 * Copyright (c) 2018 by Oliver Boehm
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * (c)reated 10.03.2018 by oboehm (ob@oasd.de)
 */
package clazzfish.monitor.util;

import clazzfish.monitor.io.ExtendedFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Historically some parts of this class were developped for a log browser
 * for Log4J. The facility to read (compressed) tar files (using
 * org.apache.commons.compress.tar.*) were removed because we use it here only
 * for zip and jar files.
 *
 * @author oliver (ob@aosd.de)
 */
class ArchivEntryTest {
    
    private static final Logger LOG = LoggerFactory.getLogger(ArchivEntryTest.class);

    private static ArchivEntry zipEntry;
    private static File mavenRepositoryDir;
    private static File log0jar;
    private static File log1jar;

    /**
     * Sets the up before class.
     *
     * @throws URISyntaxException the URI syntax exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @BeforeAll
    public static void setUpBeforeClass() throws URISyntaxException, IOException {
        zipEntry = new ArchivEntry(new URI("zip:file:/tmp/hugo.zip!boss"));
        mavenRepositoryDir = Environment.getLocalMavenRepositoryDir();
        FilenameUtils.separatorsToUnix(Environment.getLocalMavenRepositoryDir().getPath());
        File logDir = new File(mavenRepositoryDir, "commons-logging/commons-logging");
        log0jar = new File(logDir, "1.1/commons-logging-1.1.jar");
        log1jar = new File(logDir, "1.1.1/commons-logging-1.1.1.jar");
    }

    /**
     * Test get file.
     * @throws IOException if e.g. disk is full
     */
    @Test
    public void testGetFile() throws IOException {
        assertEqualsFile(new File("/tmp/hugo.zip"), zipEntry.getFileArchiv());
    }

    /**
     * Here we test the expected IOException of {@link ArchivEntry#getZipFile()};.
     */
    @Test
    public void testGetZipFile() {
        File nonexisting = new File("/does/not/exist");
        ArchivEntry entry = new ArchivEntry(nonexisting);
        try {
            entry.getZipFile();
        } catch (IOException expected) {
            String msg = expected.getMessage();
            assertTrue(msg.contains(nonexisting.getPath()), "file missing in \"" + msg + '"');
        }
    }

    /**
     * Test method for {@link ArchivEntry#getEntry()}.
     */
    @Test
    public void testGetEntryZip() {
        assertEquals("boss", zipEntry.getEntry());
    }

    /**
     * Test method for {@link ArchivEntry#getEntry()}.
     *
     * @throws URISyntaxException the URI syntax exception
     */
    @Test
    public void testGetEntryJar() throws URISyntaxException {
        ArchivEntry jarEntry = new ArchivEntry(new URI("jar:file:/tmp/one.jar!/hello/World.class"));
        assertEquals("hello/World.class", jarEntry.getEntry());
    }

    /**
     * Test method for {@link ArchivEntry#getEntry()} and for schema
     * "wsjar:file". You can see this schema if you work with Websphere.
     *
     * @throws URISyntaxException the URI syntax exception
     */
    @Test
    public void testGetEntryWsJar() throws URISyntaxException {
        ArchivEntry jarEntry = new ArchivEntry(new URI("wsjar:file:/tmp/one.jar!/hello/World.class"));
        assertEquals("hello/World.class", jarEntry.getEntry());
    }

    /**
     * Test method for {@link ArchivEntry#getEntry()}.
     *
     * @throws URISyntaxException the URI syntax exception
     */
    @Test
    public void testGetEntryBundleresource() throws URISyntaxException {
        ArchivEntry jarEntry = new ArchivEntry(new URI("bundleresource://105.fwk-1461480457/javax/ejb/EJBException.class"));
        assertEquals("javax/ejb/EJBException.class", jarEntry.getEntry());
    }

    /**
     * Test method for {@link ArchivEntry#ArchivEntry(File)}.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testFileArchiv() throws IOException {
        File file = new File("/tmp/klein");
        ArchivEntry fileEntry = new ArchivEntry(file.toURI());
        assertEquals(file.getCanonicalFile(), fileEntry.getFileArchiv().getCanonicalFile());
        assertTrue(StringUtils.isEmpty(fileEntry.getEntry()), fileEntry.getEntry());
    }

    /**
     * Test method for {@link ArchivEntry#getFileArchiv()}.
     */
    @Test
    public void testGetFileArchiv() {
        checkFileArchiv(new File("/a", "b.c"));
    }

    /**
     * Test method for {@link ArchivEntry#getFileArchiv()}. Here we test with
     * a windows path like "C:/...".
     */
    @Test
    public void testGetWindowsFileArchiv() {
        checkFileArchiv(new File("D:/dir/fil.e"));
    }

    private void checkFileArchiv(final File testFile) {
        ArchivEntry archiv = new ArchivEntry(testFile.toURI());
        assertEqualsFile(testFile, archiv.getFileArchiv());
    }

    /**
     * The class org.apache.commons.logging.Log has a size of 626 bytex.
     *
     * @throws URISyntaxException the URI syntax exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testGetSize() throws URISyntaxException, IOException {
        if (log0jar.exists()) {
            ArchivEntry entry = getArchivEntry(log0jar,
                    "org/apache/commons/logging/Log.class");
            assertEquals(626, entry.getSize());
        } else {
            LOG.warn(log0jar + " does not exist => TEST SKIPPED");
        }
    }

    /**
     * This is the test case for <a
     * href="https://sourceforge.net/p/patterntesting/bugs/29/">bug 29</a>.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws URISyntaxException the URI syntax exception
     * @since 1.5
     */
    @Test
    public void testGetSize4Bug29() throws IOException, URISyntaxException {
        ArchivEntry entry = new ArchivEntry(new URI("bundleresource://75.fwk581954975"));
        entry.getSize();
    }

    /**
     * Test method for {@link ArchivEntry#getBytes()}. For testing we use a
     * simple test file which is deleted afterwards.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testGetBytesFile() throws IOException {
        File testFile = File.createTempFile("test", ".jar");
        try {
            FileUtils.writeStringToFile(testFile, "hello world", StandardCharsets.UTF_8);
            byte[] bytes = new ArchivEntry(testFile.toURI()).getBytes();
            assertEquals("hello world", new String(bytes));
        } finally {
            testFile.deleteOnExit();
        }
    }

    /**
     * Test method for {@link ArchivEntry#getBytes()}. For testing we use an
     * existing JAR file.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws URISyntaxException the URI syntax exception
     */
    @Test
    public void testGetBytesZipEntry() throws IOException, URISyntaxException {
        ZipFile zipFile = new ZipFile(log1jar);
        ZipEntry entry = zipFile.getEntry("org/apache/commons/logging/Log.class");
        InputStream istream = zipFile.getInputStream(entry);
        try {
            byte[] expected = IOUtils.toByteArray(istream);
            ArchivEntry archivEntry = new ArchivEntry(toJarFileURI(log1jar, "!/org/apache/commons/logging/Log.class"));
            assertThat(archivEntry.getBytes(), equalTo(expected));
        } finally {
            istream.close();
            zipFile.close();
        }
    }

    /**
     * The class org.apache.commons.logging.Log has different size:
     * version 1.0 has a size of 479 bytes, version 1.1 has a size of
     * 626 bytes.
     *
     * @throws URISyntaxException the URI syntax exception
     */
    @Test
    public void testNotEquals() throws URISyntaxException {
        checkEquals(false, log0jar, log1jar);
    }

    /**
     * The class org.apache.commons.logging.LogSource has 3,5 kB in the
     * jar file so I guess they are identical. Let's test it.
     *
     * @throws URISyntaxException the URI syntax exception
     */
    @Test
    public void testEquals() throws URISyntaxException {
        checkEquals(true, log0jar, log0jar);
    }

    private static void checkEquals(final boolean expected, final File jar0, final File jar1) throws URISyntaxException {
        String resource = "org/apache/commons/logging/Log.class";
        if (!jar0.exists() && !jar1.exists()) {
            LOG.warn(jar0 + " or " + jar1 + " does not exist => TEST SKIPPED");
            return;
        }
        ArchivEntry log0class = getArchivEntry(jar0, resource);
        ArchivEntry log1class = getArchivEntry(jar1, resource);
        assertEquals(expected, log0class.equals(log1class));
        if (expected) {
            assertEquals(log0class, log1class);
        }
    }

    /**
     * Another test case to reproduce <a
     * href="https://sourceforge.net/p/patterntesting/bugs/29/">bug 29</a>.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws URISyntaxException the URI syntax exception
     */
    @Test
    public void testEquals4Bug29() throws IOException, URISyntaxException {
        URI uri = new URI("bundleresource://77.fwk581954975");
        ArchivEntry e1 = new ArchivEntry(uri);
        ArchivEntry e2 = new ArchivEntry(uri);
        assertEquals(e1, e2);
    }

    /**
     * Two identical classes in different jar files should be identified as
     * equal entries.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testEqualsWithDifferentJars() throws IOException {
        assertTrue(log1jar.exists(), log1jar + " for testing is missing");
        File copiedJar = File.createTempFile("copiedLog1", ".jar");
        String entry = "!/org/apache/commons/logging/Log.class";
        ArchivEntry log1Entry = new ArchivEntry(toJarFileURI(log1jar, entry));
        ArchivEntry copiedEntry = new ArchivEntry(toJarFileURI(copiedJar, entry));
        FileUtils.copyFile(log1jar, copiedJar);
        try {
            assertEquals(log1Entry, copiedEntry);
        } finally {
            copiedJar.deleteOnExit();
        }
    }

    private static URI toJarFileURI(final File file, final String entry) {
        String path = separatorsToUnix(file.getPath());
        return URI.create("jar:file:" + path + entry);
    }

    private static String separatorsToUnix(String path) {
        if (path.startsWith("C:\\")) {
            path = path.substring(2);
        }
        path = FilenameUtils.separatorsToUnix(path);
        return path;
    }

    /**
     * Another test case to reproduce <a
     * href="https://sourceforge.net/p/patterntesting/bugs/29/">bug 29</a>.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws URISyntaxException the URI syntax exception
     */
    @Test
    public void testNotEquals4Bug29() throws IOException, URISyntaxException {
        ArchivEntry e1 = new ArchivEntry(new URI("bundleresource://167.fwk581954975"));
        ArchivEntry e2 = new ArchivEntry(new URI("bundleresource://80.fwk581954975"));
        assertThat(e1, not(equalTo(e2)));
    }

    /**
     * Here we want to know if we can read a class as resource from the
     * archive. Because commons-logging is no longer used we now use a class
     * from commons-lang where we can be sure that it is in the local Maven
     * repository.
     *
     * @throws URISyntaxException in case of a wrong URI
     * @throws IOException if e.g. disk is full
     */
    @Test
    public void testArchivEntry() throws URISyntaxException, IOException {
        File jarFile = new File(mavenRepositoryDir, "org/apache/commons/commons-lang3/3.7/commons-lang3-3.7.jar");
        String resource = "org/apache/commons/lang3/StringUtils.class";
        ArchivEntry entry = getArchivEntry(jarFile, resource);
        LOG.info("entry=" + entry + ", size=" + entry.getSize());
        assertEquals(resource, entry.getEntry());
    }

    private static ArchivEntry getArchivEntry(final File jarfile,
                                              final String resource) throws URISyntaxException {
        URI jarURI = new URI("jar:" + jarfile.toURI() + "!/" + resource);
        return new ArchivEntry(jarURI);
    }

    /**
     * There was reported a {@link StringIndexOutOfBoundsException} in
     * the private method 'initArchiv' of {@link ArchivEntry}. Here part of
     * the stacktrace (v1.4.4):
     * <pre>
     * Caused by: java.lang.StringIndexOutOfBoundsException
     *   at java.lang.String.substring(String.java:1148)
     *   at patterntesting.runtime.util.ArchivEntry.initArchiv(ArchivEntry.java:128)
     *   at patterntesting.runtime.util.ArchivEntry.&lt;init&gt;(ArchivEntry.java:102)
     *   at patterntesting.runtime.monitor.ClasspathMonitor.getIncompatibleClassList(ClasspathMonitor.java:1326)
     *   at patterntesting.runtime.monitor.ClasspathMonitor.getIncompatibleClasses(ClasspathMonitor.java:1346)
     *   at patterntesting.runtime.monitor.ClasspathMonitor.dumpMe(ClasspathMonitor.java:1523)
     *   at patterntesting.runtime.monitor.ClasspathMonitor.dumpMe(ClasspathMonitor.java:1502)
     *   at patterntesting.runtime.monitor.ClasspathMonitor.dumpMe(ClasspathMonitor.java:1487)
     * </pre>
     * This test tried to reconstruct this exception.
     * <p>
     * Note: In the meantime the initArchiv method was refactored and does not
     * exist any longer. But the name of the unit test remains.
     * </p>
     *
     * @throws URISyntaxException the URI syntax exception
     */
    @Test
    public void testInitArchiv() throws URISyntaxException {
        checkInitArchiv("http://central.maven.org/maven2/commons-logging/commons-logging/1.1/commons-logging-1.1.jar");
    }

    /**
     * On some application servers (like Websphere) you can see
     * "bundleresource://..." as resource URL. This is ignored - watch the log
     * to see if it is really ignored.
     *
     * @throws URISyntaxException the URI syntax exception
     */
    @Test
    public void testInitArchivWithBundleresource() throws URISyntaxException {
        checkInitArchiv("bundleresource://167.fwk-450682364/javax/ejb/EJBException.class");
    }

    private void checkInitArchiv(final String testUri) throws URISyntaxException {
        URI uri = new URI(testUri);
        ArchivEntry entry = new ArchivEntry(uri);
        LOG.debug("{} was successful created with URI '{}'.", entry, uri);
    }

    /**
     * Test method for {@link ArchivEntry#toURI()}.
     *
     * @throws URISyntaxException the URI syntax exception
     */
    @Test
    public void testToURI() throws URISyntaxException {
        File ab = new File("/a/b");
        ArchivEntry file = new ArchivEntry(ab);
        assertEqualsFile(ab, new File(file.toURI()));
    }

    public static void assertEqualsFile(File file1, File file2) {
        assertEquals(new ExtendedFile(file1).normalize(), new ExtendedFile(file2).normalize());
    }

    /**
     * Another test method for {@link ArchivEntry#toURI()} and constructor
     * {@link ArchivEntry#ArchivEntry(String, File, String)}-.
     *
     * @throws URISyntaxException the URI syntax exception
     */
    @Test
    public void testConstructor() throws URISyntaxException {
        ArchivEntry jarEntry = new ArchivEntry("jar:file", new File("/a/bc.jar"), "d/E.class");
        assertEquals("jar:file:/a/bc.jar!/d/E.class", jarEntry.toURI().toString());
    }

    /**
     * With nested JAR files (JAR files which are embedded in a WAR file) the
     * {@link ArchivEntry#getSize()} produces a {@link NullPointerException}.
     * This can happen if you use executable WAR files, e.g. from Spring:
     * <pre>
     * java.lang.NullPointerException
     *      at patterntesting.runtime.util.ArchivEntry.getSize(ArchivEntry.java:251)
     *      at patterntesting.runtime.util.ArchivEntry.isEqualsWith(ArchivEntry.java:298)
     *      at patterntesting.runtime.util.ArchivEntry.equals(ArchivEntry.java:287)
     *      ...
     * </pre>
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testNestedEntries() throws IOException {
        ArchivEntry one = createWorldWarAchivEntry("jcl-over-slf4j-1.7.21.jar!/org/apache/commons/logging/LogFactory.class");
        assertEquals(4720, one.getSize());
    }

    private static ArchivEntry createWorldWarAchivEntry(String postfix) {
        File warFile = new File("src/test/resources/clazzfish/util/world.war");
        assertTrue(warFile.exists(), "should exist: " + warFile);
        URI uri = URI.create("jar:" + warFile.toURI() + "!/WEB-INF/lib/" + postfix);
        return new ArchivEntry(uri);
    }


}
