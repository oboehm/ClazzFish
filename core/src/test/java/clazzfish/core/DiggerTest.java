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

import clazzfish.core.stat.ClazzRecord;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Digger}.
 *
 * @author oboehm
 * @since 03.10.25
 */
class DiggerTest {

    private final Digger digger = new Digger();

    @Test
    void readElementsFromNestedArchive() throws IOException {
        File file = new File("../monitor/src/test/resources/clazzfish/monitor/util/world.ear");
        Collection<String> elements = Digger.readElementsFromNestedArchive(file);
        assertFalse(elements.isEmpty());
    }

    @Test
    void testGetClasses() {
        String[] classes = digger.getClasses();
        assertThat(classes, hasItemInArray(this.getClass().getName()));
        assertThat(classes, hasItemInArray(Instrumentation.class.getName()));
    }

    @Test
    void testGetLoadedClassnames() {
        String[] classes = digger.getLoadedClassnames();
        assertThat(classes, hasItemInArray(this.getClass().getName()));
    }

    @Test
    void testGetClassRecords() {
       Set<ClazzRecord> clazzRecords = digger.getClassRecords();
       assertFalse(clazzRecords.isEmpty());
       assertEquals(clazzRecords.size(), digger.getClasses().length);
       assertIsUnique(clazzRecords);
    }

    private void assertIsUnique(Set<ClazzRecord> clazzRecords) {
        Set<String> classnames = new HashSet<>();
        for (ClazzRecord cr : clazzRecords) {
            assertFalse(classnames.contains(cr.classname()), "not unique class: " + cr);
            classnames.add(cr.classname());
        }
        assertEquals(clazzRecords.size(), classnames.size());
    }

    @Test
    void testGetShadedRecords() {
        Set<ClazzRecord> clazzRecords = digger.getShadedRecords();
        assertNotNull(clazzRecords);
    }

}