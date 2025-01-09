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
package clazzfish.monitor.stat;

import clazzfish.monitor.jmx.MBeanFinder;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import patterntesting.runtime.junit.CollectionTester;

import java.io.*;
import java.net.URI;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ClazzStatistic}.
 *
 * @author oboehm
 * @since 25.11.24
 */
class ClazzStatisticTest {

    private static final Logger log = LoggerFactory.getLogger(ClazzStatistic.class);
    private final ClazzStatistic recorder = ClazzStatistic.getInstance();

    @Test
    void registerMeAsMBean() {
        recorder.registerMeAsMBean();
        assertTrue(MBeanFinder.isRegistered("clazzfish.monitor:name=ClazzStatistic,type=stat")
                || MBeanFinder.isRegistered("clazzfish:type=monitor,monitor=stat,name=ClazzStatistic"));
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
        ClazzStatistic rec = exportStatistic(csvFile);
        rec.importCSV(csvFile);
        checkClasses(rec.getStatistics(), this.getClass().getName(), 2);
        checkClasses(rec.getStatistics(), "clazzfish.monitor.internal.DeadClass", 0);
    }

    /**
     * Unit test for #21.
     *
     * @throws IOException in case of problems
     */
    @Test
    void importCSVwithUpdatedClasspath() throws IOException {
        File csvFile = new File("target/statistics", "import.csv");
        ClazzStatistic rec = exportStatistic(csvFile);
        Set<URI> classpathes = rec.getClasspathes();
        File updated = updatedDependenciesIn(csvFile);
        rec.importCSV(updated);
        CollectionTester.assertEquals(classpathes, rec.getClasspathes());
    }

    @Test
    void importCorruptCSV() throws IOException {
        File corrupt = new File("src/test/resources/clazzfish/monitor/stat/corrupt.csv");
        recorder.importCSV(corrupt);
        assertFalse(recorder.getStatistics().isEmpty());
    }

    private static ClazzStatistic exportStatistic(File csvFile) throws IOException {
        if (csvFile.delete()) {
            log.info("{} is deleted.", csvFile);
        }
        ClazzStatistic rec = new ClazzStatistic(csvFile);
        rec.exportCSV(csvFile);
        return rec;
    }

    private File updatedDependenciesIn(File csvFile) throws IOException {
        File updated = new File(csvFile + "-updated");
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile));
             PrintWriter writer = new PrintWriter(updated)) {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                writer.println(line.replace("classes", "x"));
            }
        }
        return updated;
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