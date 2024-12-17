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

import clazzfish.monitor.jmx.MBeanHelper;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ClazzRecorder}.
 *
 * @author oboehm
 * @since 25.11.24
 */
class ClazzRecorderTest {

    private static final Logger log = LoggerFactory.getLogger(ClazzRecorder.class);
    private final ClazzRecorder recorder = ClazzRecorder.getInstance();

    @Test
    void registerMeAsMBean() {
        recorder.registerMeAsMBean();
        assertTrue(MBeanHelper.isRegistered("clazzfish.monitor:name=ClazzRecorder,type=rec")
                || MBeanHelper.isRegistered("clazzfish:name=rec,type=monitor,monitor=ClazzRecorder"));
    }

    @Test
    void doubleRegistration() {
        registerMeAsMBean();
        registerMeAsMBean();
    }

    @Test
    void getStatistics() {
        Set<ClazzRecord> classes = recorder.getStatistics();
        assertFalse(classes.isEmpty());
        checkClasses(classes, this.getClass().getName(), 1);
    }

    @Test
    void exportCSV() throws IOException {
        File csvFile = recorder.exportCSV();
        assertTrue(csvFile.exists());
    }

    @Test
    void importCSV() throws IOException {
        File csvFile = new File("target/statistics", "import.csv");
        if (csvFile.delete()) {
            log.info("{} is deleted.", csvFile);
        }
        ClazzRecorder rec = new ClazzRecorder(csvFile);
        rec.exportCSV(csvFile);
        rec.importCSV(csvFile);
        checkClasses(rec.getStatistics(), this.getClass().getName(), 2);
        checkClasses(rec.getStatistics(), "clazzfish.monitor.internal.DeadClass", 0);
    }

    private static void checkClasses(Set<ClazzRecord> classes, String classname, int n) {
        for (ClazzRecord record : classes) {
            if (classname.equals(record.classname())) {
                assertEquals(n, record.count());
                return;
            }
        }
        throw new AssertionError(classname + " not found in set of classes");
    }

}