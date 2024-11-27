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
 * (c)reated 25.11.24 by oboehm
 */
package clazzfish.monitor.rec;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link ClazzRecorder}.
 *
 * @author oboehm
 * @since 25.11.24
 */
class ClazzRecorderTest {

    private static final Logger log = LoggerFactory.getLogger(ClazzRecorderTest.class);
    private final ClazzRecorder recorder = ClazzRecorder.getInstance();

    @Test
    void getClasses() {
        Set<PathRecord> classes = recorder.getClasses();
        assertFalse(classes.isEmpty());
        checkClasses(classes, this.getClass().getName());
    }

    private static void checkClasses(Set<PathRecord> classes, String classname) {
        for (PathRecord record : classes) {
            if (classname.equals(record.classname())) {
                assertThat(record.count(), greaterThan(0));
                return;
            }
        }
        throw new AssertionError(classname + " not found in set of classes");
    }

    @Test
    void exportCSV() throws FileNotFoundException {
        File csvFile = new File("target", "clazzes.csv");
        if (!csvFile.exists() && csvFile.delete()) {
            log.info("{} was deleted.", csvFile);
        }
        recorder.exportCSV(csvFile);
        assertTrue(csvFile.exists());
    }

}