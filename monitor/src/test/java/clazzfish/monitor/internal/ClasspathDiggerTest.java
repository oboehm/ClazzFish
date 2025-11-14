/*
 * Copyright (c) 2018-2025 by Oliver Boehm
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
 * (c)reated 08.03.2018 by oboehm (ob@oasd.de)
 */
package clazzfish.monitor.internal;

import clazzfish.monitor.loader.CompoundClassLoader;
import clazzfish.monitor.loader.WebappClassLoader;
import clazzfish.monitor.util.Converter;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class ClasspathDiggerTest extends AbstractDiggerTest {
    
    private static final Logger LOG = LoggerFactory.getLogger(ClasspathDiggerTest.class);
    private final ClasspathDigger digger = ClasspathDigger.DEFAULT;

    /**
     * We use the String class as resource for testing. But with this class it
     * happened that it appeared 2 times in the classpath, e.g. if you call the
     * test inside your favorite IDE. In most cases this was the same classpath
     * where the doublet appears. Since 2.0 doublets in the same classpath are
     * not regarded as doublet.
     */
    @Test
    public void testGetResources() {
        String rsc = Converter.toResource(String.class);
        Enumeration<URI> resources = digger.getResources(rsc);
        URI r1 = resources.nextElement();
        if (resources.hasMoreElements()) {
            URI r2 = resources.nextElement();
            assertThat(r1, not(r2));
        }
    }

    /**
     * The tests in this JUnit class are only useful if this test here is ok.
     */
    @Test
    public void testIsClassloaderSupported() {
        assertTrue(digger.isClassloaderSupported(), digger.getClassLoader() + " is not supported");
        LOG.info(digger + " supports the given classloader");
    }

    /**
     * Test get loaded class list.
     */
    @Test
    public void testGetLoadedClassList() {
        Set<Class<?>> classes = digger.getLoadedClasses();
        assertFalse(classes.isEmpty());
        LOG.info("{} classes loaded.", classes.size());
        assertThat(classes, hasItem(this.getClass()));
        for (Class<?> clazz : classes) {
            if ("clazzfish.core.internal.DeadClass".equals(clazz.getName())) {
                fail(clazz + " should be not loaded!");
            }
        }
    }

    @Test
    public void testGetLoadedClassnames() {
        String[] classnames = digger.getLoadedClassnames();
        assertNotNull(classnames);
        LOG.info("{} classes loaded.", classnames.length);
        assertThat(classnames, hasItemInArray(this.getClass().getName()));
        for (String clname : classnames) {
            if ("clazzfish.core.internal.DeadClass".equals(clname)) {
                fail(clname + " should be not loaded!");
            }
        }
    }

    /**
     * Test for {@link ClasspathDigger#isLoaded(String)}.
     */
    @Test
    public void testIsLoaded() {
        if (ClasspathDigger.isAgentAvailable()) {
            checkIsLoaded(this.getClass());
            checkIsLoaded(ClasspathDigger.class);
            checkIsLoaded(Test.class);
        }
    }

    private void checkIsLoaded(final Class<?> testClass) {
        String classname = testClass.getName();
        assertTrue(digger.isLoaded(classname), classname);
    }

    /**
     * Here we test only if we get we get the loaded packages from the
     * ClassLoader.
     */
    @Test
    public void testGetLoadedPackageArray() {
        Package[] packages = digger.getLoadedPackageArray();
        LOG.info(packages.length + " packages loaded");
        assertTrue(packages.length > 0);
    }

    /**
     * Test method for {@link ClasspathDigger#getClasspath()}.
     */
    @Test
    public void testGetClasspath() {
        checkClasspath(digger.getClasspath());
    }

    private static void checkClasspath(final String[] classpath) {
        for (int i = 0; i < classpath.length; i++) {
            File path = new File(classpath[i]);
            assertTrue(path.exists(), "path does not exist: " + path);
            LOG.info("{}. path: {}", i+1, path);
        }
    }

    /**
     * Here we want to test the private getTomcatClasspath(..) method of
     * {@link ClasspathDigger}.
     *
     * @throws IOException the malformed url exception
     */
    @Test
    public void testGetTomcatClasspath() throws IOException {
        File file = new File("/tmp/one");
        ClassLoader tomcat = new WebappClassLoader(file.toURI().toURL());
        ClasspathDigger tomcatDigger = new ClasspathDigger(tomcat);
        String[] classpath = tomcatDigger.getClasspath();
        assertEquals(file.getCanonicalFile(), new File(classpath[0]).getCanonicalFile());
    }

    /**
     * Here we want to test the private getWebspherClasspath(..) method of
     * {@link ClasspathDigger}.
     */
    @Test
    public void testGetWebsphereClasspath() {
        File[] input = { new File("/tmp/bin"), new File("/tmp/web/WEB-INF/classes"),
                new File("/tmp/web"), new File("/tmp/lib/commons-lang.jar")};
        CompoundClassLoader wsLoader = new CompoundClassLoader(input);
        ClasspathDigger wsDigger = new ClasspathDigger(wsLoader);
        String[] classpath = wsDigger.getClasspath();
        assertEquals(input[0], new File(classpath[0]));
        assertEquals(input[1], new File(classpath[1]));
        assertEquals(input[3], new File(classpath[2]));
        assertEquals(3, classpath.length);
    }

    /**
     * Test method for {@link ClasspathDigger#getPackageArray()}.
     */
    @Test
    public void testGetPackageArray() {
        String[] packages = digger.getPackageArray();
        assertThat(packages.length, Matchers.not(0));
        List<String> pkgs = Arrays.asList(packages);
        assertThat(pkgs, hasItem("org/junit/jupiter/api/"));
        assertFalse(pkgs.contains(null), "contains null values: " + pkgs);
        Package[] clPackages = Package.getPackages();
        LOG.info("{} packages found, {} packages loaded.", packages.length, clPackages.length);
        pkgs = Arrays.asList(digger.getPackageArray());
        for (Package pkg : clPackages) {
            assertThat(pkgs, hasItem(pkg.getName().replace('.', '/') + '/'));
        }
    }

    /**
     * Test method for {@link ClasspathDigger#getLoadedResources()}.
     */
    @Test
    public void testGetLoadedResources() {
        List<String> loadedResources = digger.getLoadedResources();
        LOG.info("{} resources loaded.", loadedResources.size());
        assertNotNull(this.getClass().getResource("/log4j2.xml"));
        assertThat(loadedResources, hasItem("/log4j2.xml"));
        assertThat(loadedResources, not(hasItem("/clazzfish")));
        assertThat(loadedResources, not(hasItem("/clazzfish/monitor/ClassloaderType.class")));
    }

    @Test
    public void testClasspathClasses() {
        checkGetClasses(digger, "java.lang.instrument.Instrumentation");
    }

    /**
     * The {@link ClasspathDigger} has problems with executable war's generated
     * by spring-boot (and probably also generated by other tools). In this
     * situatation the following stacktrace was observed:
     * <pre>
     * WARN ClasspathMonitor - Cannot add classes from .../some-artifact.war!/WEB-INF/classes!:
     * java.io.FileNotFoundException: .../some-artifact.war!/WEB-INF/classes! (No such file or directory)
     *     at java.util.zip.ZipFile.open(Native Method) ~[?:1.8.0_45]
     *     at java.util.zip.ZipFile.&lt;init&gt;(ZipFile.java:220) ~[?:1.8.0_45]
     *     at java.util.zip.ZipFile.&lt;init&gt;(ZipFile.java:150) ~[?:1.8.0_45]
     *     at java.util.zip.ZipFile.&lt;init&gt;(ZipFile.java:164) ~[?:1.8.0_45]
     *     at patterntesting.runtime.monitor.internal.ClasspathDigger.addElementsFromArchive(ClasspathDigger.java:312) ~[patterntesting-rt-1.6.3.jar!/:1.6.3]
     *     at patterntesting.runtime.monitor.ClasspathMonitor.addClasses(ClasspathMonitor.java:938) [patterntesting-rt-1.6.3.jar!/:1.6.3]
     *     at patterntesting.runtime.monitor.ClasspathMonitor.createClasspathClassSet(ClasspathMonitor.java:916) [patterntesting-rt-1.6.3.jar!/:1.6.3]
     *     at patterntesting.runtime.monitor.ClasspathMonitor.getClasspathClassArray(ClasspathMonitor.java:922) [patterntesting-rt-1.6.3.jar!/:1.6.3]
     *     at patterntesting.runtime.monitor.ClasspathMonitor.access$0(ClasspathMonitor.java:921) [patterntesting-rt-1.6.3.jar!/:1.6.3]
     *     at patterntesting.runtime.monitor.ClasspathMonitor$1.call(ClasspathMonitor.java:134) [patterntesting-rt-1.6.3.jar!/:1.6.3]
     *     at patterntesting.runtime.monitor.ClasspathMonitor$1.call(ClasspathMonitor.java:1) [patterntesting-rt-1.6.3.jar!/:1.6.3]
     *     ...
     * </pre>
     * <p>
     * We cannot mock {@link ClasspathDigger#getClasspath()} to dig into a web
     * classpath. So we mock a {@link URLClassLoader} to influence the result of
     * {@link ClasspathDigger#getClasspath()}.
     * </p>
     *
     * @throws MalformedURLException as a result of using an
     *         {@link URLClassLoader}
     */
    @Test
    public void testWarClasses() throws MalformedURLException {
        ClasspathDigger warDigger = createClasspathDigger(WORLD_WAR, "!/WEB-INF/classes");
        checkGetClasses(warDigger, "patterntesting.sample.World");
    }

    /**
     * This is the same test as before but now for a JAR inside a WAR. E.g.
     * here we have now a nested JAR hierarchy to parse.
     *
     * @throws MalformedURLException as a result of using an
     *         {@link URLClassLoader}
     */
    @Test
    public void testWarJar() throws MalformedURLException {
        ClasspathDigger warDigger = createClasspathDigger(WORLD_WAR, "!/WEB-INF/lib/patterntesting-agent-1.6.3.jar");
        checkGetClasses(warDigger, "patterntesting.agent.ClasspathAgent");
    }

    /**
     * Here we test the next level of nesting: a JAR inside a WAR inside an EAR.
     *
     * @throws MalformedURLException as a result of using an
     *         {@link URLClassLoader}
     */
    @Test
    public void testEarWarJar() throws MalformedURLException {
        ClasspathDigger warDigger = createClasspathDigger(WORLD_EAR,
                "!/world.war!/WEB-INF/lib/patterntesting-agent-1.6.3.jar");
        checkGetClasses(warDigger, "patterntesting.agent.ClasspathAgent");
    }

    /**
     * Here we test the next level of nesting: a classes directory inside a WAR
     * inside an EAR.
     *
     * @throws IOException as a result of using an {@link URLClassLoader}
     */
    @Test
    public void testEarWarClasses() throws IOException {
        ClasspathDigger warDigger = createClasspathDigger(WORLD_EAR, "!/world.war!/WEB-INF/classes");
        checkGetClasses(warDigger, "patterntesting.sample.World");
    }

    private void checkGetClasses(ClasspathDigger warDigger, String classname) {
        String[] classes = warDigger.getClasses();
        assertThat(classes, hasItemInArray(classname));
    }

    private static ClasspathDigger createClasspathDigger(File jar, String path) throws MalformedURLException {
        URLClassLoader mockedClassLoader = mockURLClassLoader(jar, path);
        return new ClasspathDigger(mockedClassLoader);
    }
    
}
