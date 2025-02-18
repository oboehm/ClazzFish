/*
 * Copyright (c) 2024 by Oli B.
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
package clazzfish.monitor.internal;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit-Test fuer {@link Config} ...
 *
 * @author oboehm
 * @since 19.12.24
 */
class ConfigTest {

    private static final Logger log = LoggerFactory.getLogger(ConfigTest.class);

    @Test
    void getDumpDir() {
        File dir = Config.DEFAULT.getDumpDir();
        assertNotEquals("unknown", dir.getName());
        log.info("dumpDir = '{}'", dir);
    }

    @Test
    void getDumpURI() {
        URI uri = Config.DEFAULT.getDumpURI();
        assertNotNull(uri);
        log.info("dumpURI = '{}'", uri);
    }

}