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
 * (c)reated 03.10.25 by oboehm
 */
package clazzfish.core;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Unit tests for {@link AbstractDigger}.
 *
 * @author oboehm
 * @since 03.10.25
 */
class AbstractDiggerTest {

    @Test
    void readElementsFromNestedArchive() throws IOException {
        File file = new File("../monitor/src/test/resources/clazzfish/monitor/util/world.ear");
        Collection<String> elements = AbstractDigger.readElementsFromNestedArchive(file);
        assertFalse(elements.isEmpty());
    }

}