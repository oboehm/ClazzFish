package clazzfish.monitor.util;
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
 * (c)reated 11.03.2018 by oboehm (ob@oasd.de)
 */

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Environment} class.
 */
public class EnvironmentTest {
    
    private static final Logger LOG = LoggerFactory.getLogger(EnvironmentTest.class);

    /**
     * Test method for {@link Environment#getName()}.
     */
    @Test
    public void testGetName() {
        String name = Environment.getName();
        LOG.info("environment name: " + name);
        String cloader = Environment.getClassLoader().getClass().getName();
        assertTrue(cloader.startsWith(name), name + " does not match " + cloader);
        assertFalse(name.indexOf('$') >= 0, name + " has a '$' inside");
    }

    /**
     * Test method for {@link Environment#matchesOneOf(java.lang.String[])}.
     */
    @Test
    public void testMatchesOneOf() {
        String[] props = {"java.home"};
        assertTrue(Environment.matchesOneOf(props), "'java.home' should exist");
    }
    
    @Test
    public void testMatchesOneOfWithWildcard() {
        assertTrue(Environment.matchesOneOf("hello*", "java.io.tmpdir"));
        assertTrue(Environment.matchesOneOf("world?", "java.io.tmpdir"));
    }
    
    @Test
    public void testMatchesOnOfEmpty() {
        assertTrue(Environment.matchesOneOf(""));
    }

    /**
     * Test method for {@link Environment#matchesOneOf(java.lang.String[])}.
     */
    @Test
    public void testDoesNotMatches() {
        String[] props = {"nir.wana"};
        if (System.getProperty(props[0]) == null) {
            assertFalse(Environment.matchesOneOf(props), "'nir.wana' should not exist");
        }
    }

    /**
     * Test method for {@link Environment#isPropertyEnabled(String)}.
     */
    @Test
    public void testIsPropertyEnabled() {
        String testProp = "testStupidProperty";
        unsetSystemProperty(testProp);
        System.setProperty(testProp, "");
        assertTrue(Environment.isPropertyEnabled(testProp), "empty property should be considered as 'true'");
        System.setProperty(testProp, "true");
        assertTrue(Environment.isPropertyEnabled(testProp), testProp + "=true");
        unsetSystemProperty(testProp);
    }

    private static void unsetSystemProperty(final String name) {
        Properties props = System.getProperties();
        props.remove(name);
        assertFalse(Environment.isPropertyEnabled(name), name + " is not set");
    }

    /**
     * Test method for {@link Environment#loadProperties(String)}.
     * @throws IOException if poperties can't be loaded
     */
    @Test
    public synchronized void testLoadProperties() throws IOException {
        Environment.loadProperties("test.properties");
        assertTrue(Environment.isPropertyEnabled("my.little.test.property"), "see test.properties");
        unsetSystemProperty("my.little.test.property");
    }

    /**
     * Test method for {@link Environment#loadProperties(String)}.
     * @throws IOException if poperties can't be loaded
     */
    @Test
    public synchronized void testLoadPropertiesViaClassloader() throws IOException {
        Environment.loadProperties("/clazzfish/monitor/util/test.properties");
        assertTrue(Environment.isPropertyEnabled("my.little.test.property"), "see test.properties");
        unsetSystemProperty("my.little.test.property");
    }

    /**
     * Test method for {@link Environment#isGoogleAppEngine()}.
     * We use the classloader to guess if we are in a Google App Engine
     * environment or not.
     */
    @Test
    public void testIsGoogleAppEngine() {
        String classLoaderName = this.getClass().getClassLoader().getClass().getName();
        LOG.info("classloader for testing is " + classLoaderName);
        boolean expected = classLoaderName.startsWith("com.google");
        assertEquals(expected, Environment.isGoogleAppEngine());
    }

    /**
     * Test method for {@link Environment#getLocalMavenRepositoryDir()}.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testGetLocalMavenRepositoryDir() throws IOException {
        File repoDir = Environment.getLocalMavenRepositoryDir();
        assertTrue(repoDir.isDirectory(), "not a directory: " + repoDir);
        LOG.debug("Local maven repository is at {}.", repoDir);
    }

    /**
     * Both things can't be true: the environment is a WebLogic server
     * <i>and</i> a Google AppEngine.
     */
    @Test
    public void testIsSpecialServer() {
        boolean isWeblogic = Environment.isWeblogicServer();
        boolean isAppEngine = Environment.isGoogleAppEngine();
        if (isWeblogic || isAppEngine) {
            assertEquals(!isWeblogic, Environment.isGoogleAppEngine());
        }
    }

    /**
     * This test has only informative character.
     */
    @Test
    public void testAreThreadsAllowed() {
        boolean threadsAllowed = Environment.areThreadsAllowed();
        LOG.info("Threads are {}.",  threadsAllowed ? "allowed" : "not allowed");
    }
    
}
