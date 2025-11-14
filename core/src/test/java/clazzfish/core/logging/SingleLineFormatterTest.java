/*
 * Copyright (c) 2025 by Oli B.
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
 * (c)reated 14.11.25 by oboehm
 */
package clazzfish.core.logging;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Unit tests for {@link SingleLineFormatter}.
 *
 * @author oboehm
 * @since 14.11.25
 */
class SingleLineFormatterTest {

    @Test
    void format() {
        // GIVEN
        Logger logger = Logger.getLogger("testLogger");
        logger.setUseParentHandlers(false);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StreamHandler handler = new StreamHandler(out, new SingleLineFormatter());
        handler.setLevel(Level.ALL);
        logger.addHandler(handler);
        logger.setLevel(Level.ALL);

        // WHEN
        logger.log(Level.INFO, "Hello \"{0}\"!", "World");
        handler.flush();

        // THEN
        String result = out.toString(StandardCharsets.UTF_8).trim();
        assertFalse(result.contains("\n"), result);
        assertFalse(result.contains("{0}"), result);
    }

}