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
 * (c)reated 25.02.25 by oboehm
 */
package clazzfish.monitor.spi;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for {@link XPorter}.
 *
 * @author oboehm
 * @since 25.02.25
 */
class XPorterTest {

    @Test
    void getProviders() {
        List<CsvXPorterProvider> providers = XPorter.getProviders();
        assertFalse(providers.isEmpty());
        CsvXPorter csvXPorter = providers.get(0).create();
        assertNotNull(csvXPorter);
    }

    @Test
    void createCsvXPorter() {
        URI fileURI = new File("src/test/resources/test.csv").toURI();
        CsvXPorter xporter = XPorter.createCsvXPorter(fileURI);
        assertNotNull(xporter);
    }

}