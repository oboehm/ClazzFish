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
 * (c)reated 18.02.25 by oboehm
 */
package clazzfish.monitor.spi;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link FileXPorter}.
 *
 * @author oboehm
 * @since 18.02.25
 */
class FileXPorterTest {

    private final CsvXPorter xPorter = new FileXPorter();

    @Test
    void exportCSV() throws IOException {
        File file = new File("target/statistics/test.csv");
        String header = "C1;C2;C3";
        List<String> lines = new ArrayList<>();
        for (int i = 1; i < 5; i++) {
            lines.add(String.format("line %d;%d-2;%d", i, i, i));
        }
        xPorter.exportCSV(file.toURI(), header, lines);
        assertTrue(file.exists());
        List<String> readLines = FileUtils.readLines(file, StandardCharsets.UTF_8);
        assertEquals(5, readLines.size());
    }

}