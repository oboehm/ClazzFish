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
 * (c)reated 09.03.2018 by oboehm (ob@oasd.de)
 */
package clazzfish.monitor.internal;

import clazzfish.monitor.util.Converter;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link ResourcepathDigger}.
 */
public class ResourcepathDiggerTest extends AbstractDiggerTest {

    /**
     * Returns a {@link ResourcepathDigger} for testing.
     *
     * @return digger
     */
    @Override
    protected AbstractDigger getDigger() {
        return new ResourcepathDigger();
    }

    /**
     * Here we test the digging of a classpath inside a WAR.
     *
     * @throws MalformedURLException from using an {@link URLClassLoader}
     */
    @Test
    public void testWarClasses() throws MalformedURLException {
        ResourcepathDigger warDigger = createResourcepathDigger(WORLD_WAR, "!/WEB-INF/classes");
        checkGetResources(warDigger, "/patterntesting/sample/default.properties");
    }

    /**
     * Here we test the digging of a JAR a WAR. E.g. here we have now a nested
     * JAR hierarchy to parse.
     *
     * @throws MalformedURLException from using an {@link URLClassLoader}
     */
    @Test
    public void testWarJar() throws MalformedURLException {
        ResourcepathDigger warDigger = createResourcepathDigger(WORLD_WAR,
                "!/WEB-INF/lib/patterntesting-agent-1.6.3.jar");
        checkGetResources(warDigger, "/patterntesting/agent/logging.properties");
    }

    /**
     * Here we test the next level of nesting: a JAR inside a WAR inside an EAR.
     *
     * @throws MalformedURLException from using an {@link URLClassLoader}
     */
    @Test
    public void testEarWarJar() throws MalformedURLException {
        ResourcepathDigger earDigger = createResourcepathDigger(WORLD_EAR,
                "!/world.war!/WEB-INF/lib/patterntesting-agent-1.6.3.jar");
        checkGetResources(earDigger, "/patterntesting/agent/logging.properties");
    }

    /**
     * Here we test the next level of nesting: a classes directory inside a WAR
     * inside an EAR.
     *
     * @throws IOException if ear file cannot be read
     */
    @Test
    public void testEarWarClasses() throws IOException {
        ResourcepathDigger earDigger = createResourcepathDigger(WORLD_EAR, "!/world.war!/WEB-INF/classes");
        checkGetResources(earDigger, "/patterntesting/sample/default.properties");
    }

    private static ResourcepathDigger createResourcepathDigger(File jar, String path) throws MalformedURLException {
        URLClassLoader mockedClassLoader = mockURLClassLoader(jar, path);
        return new ResourcepathDigger(mockedClassLoader);
    }

    private void checkGetResources(ResourcepathDigger warDigger, String resourcename) {
        Set<String> resources = warDigger.getResources();
        assertThat(resources, hasItem(resourcename));
    }

    /**
     * Directories are no files and therefore no resources.
     *
     * @throws MalformedURLException from using an {@link URLClassLoader}
     */
    @Test
    public void testNoDirRessoures() throws MalformedURLException {
        ResourcepathDigger warDigger = createResourcepathDigger(WORLD_WAR,
                "!/WEB-INF/lib/patterntesting-agent-1.6.3.jar");
        Set<String> resources = warDigger.getResources();
        assertThat(resources, not(hasItem("META-INF")));
        assertThat(resources, not(hasItem("META-INF/")));
        assertThat(resources, not(hasItem("/META-INF")));
        assertThat(resources, not(hasItem("/META-INF/")));
    }

    /**
     * We use the String class as resource for testing. But with this class it
     * happened that it appeared 2 times in the classpath, e.g. if you call the
     * test inside your favorite IDE. In most cases this was the same classpath
     * where the doublet appears. Since 2.0 doublets in the same classpath are
     * not regarded as doublet.
     */
    @Override
    @Test
    public void testGetResources() {
        ResourcepathDigger digger = new ResourcepathDigger();
        String rsc = Converter.toResource(String.class);
        Enumeration<URL> resources = digger.getResources(rsc);
        URL r1 = resources.nextElement();
        if (resources.hasMoreElements()) {
            URL r2 = resources.nextElement();
            assertThat(r1, not(r2));
        }
    }

    /**
     * Unit test for {@link ResourcepathDigger#whichResource(String, ClassLoader)}.
     */
    @Test
    public void testWhichResource() {
        URI uri = ResourcepathDigger.whichResource("log4j2.xml", this.getClass().getClassLoader());
        assertThat(uri, not(nullValue()));
        assertEquals(new File(uri).toURI(), uri);
    }

}
