/*
 * Copyright (c) 2024, 2025 by Oli B.
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
 * (c)reated 19.12.24 by oboehm
 */
package clazzfish.core;

import clazzfish.monitor.util.Environment;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-Test fuer {@link Config} ...
 *
 * @author oboehm
 * @since 19.12.24
 */
class ConfigTest {

    @Test
    void getDumpDir() {
        File dir = Config.DEFAULT.getDumpDir();
        assertNotEquals("unknown", dir.getName());
    }

    /**
     * If you want to test the behaviour with a set environment variable add e.g.
     * 'CLAZZFISH_STATISTICS_FILE=/tmp/test.csv'
     * to your environment.
     */
    @Test
    void getDumpURI() {
        URI uri = Config.DEFAULT.getDumpURI();
        assertNotNull(uri);
        String filename = Config.getEnvironment("clazzfish.statistics.file");
        if ((filename != null) && !filename.isBlank()) {
            assertEquals(new File(filename).toURI(), uri);
        }
    }

    @Test
    void getPatternExclude() {
        String pattern = Config.DEFAULT.getProperty(Config.PATTERN_EXCLUDE);
        assertNotNull(pattern);
    }

    @Test
    void ofResource() {
        Config config = Config.of("clazzfish/core/test.properties");
        assertFalse(config.getProperties().isEmpty());
    }

    @Test
    void clazzfishProperties() {
        String defaultResource = "clazzfish/core/test.properties";
        Config config = Config.of(defaultResource);
        Config defaultConfig = Config.of("clazzfish-default.properties", defaultResource);
        assertEquals(defaultConfig,  config);
    }

    /**
     * Test method for {@link Config#loadProperties(String)}.
     * @throws IOException if poperties can't be loaded
     */
    @Test
    void loadProperties() throws IOException {
        Config.loadProperties("test.properties");
        assertTrue(Environment.isPropertyEnabled("my.little.test.property"), "see test.properties");
        unsetSystemProperty("my.little.test.property");
    }

    /**
     * Test method for {@link Config#loadProperties(String)}.
     *
     * @throws IOException if poperties can't be loaded
     */
    @Test
    void loadPropertiesViaClassloader() throws IOException {
        Config.loadProperties("/clazzfish/core/test.properties");
        assertTrue(Environment.isPropertyEnabled("my.little.test.property"), "see test.properties");
        unsetSystemProperty("my.little.test.property");
    }

    private static void unsetSystemProperty(final String name) {
        Properties props = System.getProperties();
        props.remove(name);
        assertFalse(Environment.isPropertyEnabled(name), name + " is not set");
    }

}