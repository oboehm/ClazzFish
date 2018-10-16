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
package clazzfish.monitor;

import clazzfish.monitor.util.Converter;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ResourcepathMonitorTest} class.
 *
 * @author <a href="ob@aosd.de">oliver</a>
 */
public class ResourcepathMonitorTest extends AbstractMonitorTest {

    private static final Logger LOG = LoggerFactory.getLogger(ResourcepathMonitorTest.class);
    private static final String META_INF_LICENSE_TXT = "META-INF/LICENSE.txt";
    private final ResourcepathMonitor monitor = ResourcepathMonitor.getInstance();

    /**
     * Gets the monitor for testing.
     *
     * @return the monitor
     */
    @Override
    protected ResourcepathMonitor getMonitor() {
        return ResourcepathMonitor.getInstance();
    }

    /**
     * Test method for {@link ResourcepathMonitor#whichResource(String)}.
     */
    @Test
    public void testWhichResource() {
        LOG.info("testWhichResource() is started.");
        assertNotNull(monitor.whichResource("/log4j2.xml"));
        assertNotNull(monitor.whichResource("log4j2.xml"));
    }

    /**
     * Test which invalid resource.
     */
    @Test
    public void testWhichInvalidResource() {
        LOG.info("testWhichInvalidResource() is started.");
        assertNull(monitor.whichResource("gibts.net"));
    }

    /**
     * Test method for {@link ResourcepathMonitor#getResources()}.
     * Here we test if we can scan resources like 'log4j.properties' from the
     * normal classpath.
     */
    @Test
    public void testGetResourcesFromDir() {
        List<String> resourceList = getResourcesAsList();
        assertThat(resourceList, hasItem("/log4j2.xml"));
        assertThat(resourceList, not(hasItem("/patterntesting/")));
        assertThat(resourceList, not(hasItem("/patterntesting")));
    }

    /**
     * Test method for {@link ResourcepathMonitor#getResources()}. Here we test
     * if we can scan resources like '/Log4j-config.xsd' from the JAR files.
     *
     * @throws IOException the io exception
     */
    @Test
    public void testGetResourcesFromJar() throws IOException {
        List<String> resourceList = getResourcesAsList();
        assertThat(resourceList, hasItem("/Log4j-config.xsd"));
        assertThat(resourceList, hasItem("/META-INF/MANIFEST.MF"));
        assertThat(resourceList, not(hasItem("/META-INF/")));
        assertThat(resourceList, not(hasItem("/META-INF")));
    }

    private List<String> getResourcesAsList() {
        String[] resources = monitor.getResources();
        assertThat(0, not(resources.length));
        return Arrays.asList(resources);
    }

    /**
     * Test method for {@link ResourcepathMonitor#getNoResources(String)}.
     */
    @Test
    public void testGetNoResources() {
        LOG.info("testGetNoResources() is started.");
        URL url = ResourcepathMonitorTest.class.getResource("test/log4j.properties");
        if (url != null) {
            assertEquals(1, monitor.getNoResources("test/log4j.properties"));
        }
        assertTrue(monitor.getNoResources("log4j2.xml") > 0);
    }

    /**
     * Test method for {@link ResourcepathMonitor#getNoResources(String)}.
     */
    @Test
    public void testGetNoResourcesMultiple() {
        LOG.info("testGetNoResourcesMultiple() is started.");
        int n = monitor.getNoResources("META-INF/MANIFEST.MF");
        assertTrue(n > 0);
        LOG.info("{} META-INF/MANIFEST.MF entries found in classpath.", n);
    }

    /**
     * We use the String class as resource for testing. But with this class it
     * happened that it appeared 2 times in the classpath, e.g. if you call the
     * test inside your favorite IDE. In most cases this was the same classpath
     * where the doublet appears. Since 2.0 doublets in the same classpath are
     * not regarded as doublet.
     */
    @Test
    public void tetsGetNoResourcesClass() {
        String rsc = Converter.toResource(String.class);
        int n = monitor.getNoResources(rsc);
        if (n > 1) {
            assertThat(monitor.getDoublet(rsc, 0), not(monitor.getDoublet(rsc, 1)));
        }
    }

    /**
     * Test method for {@link ResourcepathMonitor#isDoublet(String)}.
     */
    @Test
    public void testIsDoublet() {
        LOG.info("testIsDoublet() is started.");
        assertTrue(monitor.isDoublet(META_INF_LICENSE_TXT));
    }

    /**
     * If a class or resource is not found you should get a
     * "NoSuchElementException" here.
     */
    @Test
    public void testIsDoubletNirwana() {
        LOG.info("testIsDoubletNirwana() is started.");
        assertThrows(NoSuchElementException.class, () -> {
            monitor.isDoublet("Nirwana");
        });
    }

    /**
     * Test method for {@link ResourcepathMonitor#getFirstDoublet(String)}.
     */
    @Test
    public void testGetFirstDoublet() {
        URI firstDoublet = monitor.getFirstDoublet(META_INF_LICENSE_TXT);
        assertNotNull(firstDoublet);
        LOG.info("firstDoublet = {}", firstDoublet);
        assertEquals(firstDoublet, monitor.getFirstDoublet("/" + META_INF_LICENSE_TXT));
    }

    /**
     * Test method for {@link ResourcepathMonitor#getDoublets()}.
     */
    @Test
    public void testGetDoublets() {
        String someDoublet = findDoublet();
        List<String> doublets = Arrays.asList(monitor.getDoublets());
        LOG.info(doublets.size() + " doublets found");
        assertThat(doublets, hasItem(someDoublet));
        assertThatMetaInfResourcesAreExclucded(doublets);
    }

    private void assertThatMetaInfResourcesAreExclucded(List<String> resources) {
        assertThat(resources, not(hasItem("/META-INF")));
        assertThat(resources, not(hasItem("/META-INF/MANIFEST.MF")));
    }

    private String findDoublet() {
        String[] resources = monitor.getResources();
        for (String rsc : resources) {
            if (monitor.isDoublet(rsc)) {
                return rsc;
            }
        }
        throw new NoSuchElementException("no doublet found for testing");
    }

    /**
     * Test method for {@link ResourcepathMonitor#getDoubletResourcepath()}.
     */
    @Test
    public void testGetDoubletResourcepath() {
        String[] doubletPath = monitor.getDoubletResourcepath();
        assertTrue(doubletPath.length > 0, "no empty array expected");
        LOG.info("{} pathes with doublet resources found.", doubletPath.length);
    }

    /**
     * Test method for {@link ResourcepathMonitor#getIncompatibleResources()}.
     */
    @Test
    public void testGetIncompatibleResources() {
        String[] incompatibleResources = monitor.getIncompatibleResources();
        assertNotNull(incompatibleResources);
        LOG.info("{} incompatible resources found.", incompatibleResources.length);
        assertThatMetaInfResourcesAreExclucded(Arrays.asList(incompatibleResources));
    }

    /**
     * Test method for {@link ResourcepathMonitor#getIncompatibleResourcepath()}.
     */
    @Test
    public void testGetIncompatibleResourcepath() {
        String[] incompatibleResourcepath = monitor.getIncompatibleResourcepath();
        assertNotNull(incompatibleResourcepath);
        LOG.info("{} incompatible classpath entries found.", incompatibleResourcepath.length);
    }

    /**
     * Test method for {@link ResourcepathMonitor#getIncompatibleResourcepathURIs()}.
     */
    @Test
    public void testGetIncompatibleResourcepathURIs() {
        String[] incompatibleResourcepath = monitor.getIncompatibleResourcepath();
        URI[] uris = monitor.getIncompatibleResourcepathURIs();
        assertEquals(incompatibleResourcepath.length, uris.length);
    }

    /**
     * Incompatible resources are also a kind of doublets. So all incompatible
     * resources must be part of the doublet path (but not the other way
     * around).
     */
    @Test
    public void testIncompatiblePathInDoubletPath() {
        String[] incompatibleResourcepath = monitor.getIncompatibleResourcepath();
        String[] doubletPath = monitor.getDoubletResourcepath();
        assertThat(Arrays.asList(doubletPath), hasItems(incompatibleResourcepath));
    }

    /**
     * Test method for {@link ResourcepathMonitor#registerAsMBean()}.
     */
    @Test
    public void testRegisterAsMBean() {
        LOG.info("testRegisterAsMBean() is started.");
        ResourcepathMonitor.registerAsMBean();
        assertTrue(ResourcepathMonitor.isRegisteredAsMBean(), monitor + " is not registered as MBean");
        ResourcepathMonitor.unregisterAsMBean();
    }

    /**
     * Test method for {@link ResourcepathMonitor#dumpMe(File)}.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    @Test
    public void testDumpMe() throws IOException {
        checkDumpMe(monitor, 8);
    }

}
