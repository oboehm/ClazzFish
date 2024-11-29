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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link ClazzRecorder}.
 *
 * @author oboehm
 * @since 25.11.24
 */
class ClazzRecorderTest {

    private final ClazzRecorder recorder = ClazzRecorder.getInstance();

    @Test
    void getStatistics() {
        Set<ClazzRecord> classes = recorder.getStatistics();
        assertFalse(classes.isEmpty());
        checkClasses(classes, this.getClass().getName(), 1);
    }

    @Test
    void exportCSV() throws FileNotFoundException {
        File csvFile = recorder.exportCSV();
        assertTrue(csvFile.exists());
    }

    @Test
    void importCSV() throws IOException {
        File csvFile = new File("target", "clazzes.csv");
        recorder.exportCSV(csvFile);
        recorder.importCSV(csvFile);
        checkClasses(recorder.getStatistics(), this.getClass().getName(), 2);
    }

    private static void checkClasses(Set<ClazzRecord> classes, String classname, int n) {
        for (ClazzRecord record : classes) {
            if (classname.equals(record.classname())) {
                assertThat(record.count(), greaterThanOrEqualTo(n));
                return;
            }
        }
        throw new AssertionError(classname + " not found in set of classes");
    }

}