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

import org.apache.commons.lang3.SystemProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * The class FileXPorter accepts a file URI to import and export CSV data.
 *
 * @author oboehm
 * @since 2.5 (18.02.25)
 */
public class FileXPorter implements CsvXPorter {

    private static final Logger log = LoggerFactory.getLogger(FileXPorter.class);

    @Override
    public void exportCSV(URI uri, String csvHeadLine, List<String> csvLines) throws IOException {
        writeCSV(new File(uri), csvHeadLine, csvLines);
        log.debug("Statistic exported with {} lines to '{}'.", csvLines.size(), uri);
    }

    private void writeCSV(File file, String csvHeadLine, List<String> csvLines) throws IOException {
        File tmpFile = new File(file + "-" + SystemProperties.getUserName() + System.currentTimeMillis());
        log.trace("Statistic is temporary stored in '{}'.", tmpFile);
        try (PrintWriter writer = new PrintWriter(tmpFile)) {
            writer.println(csvHeadLine);
            for (String line : csvLines) {
                writer.println(line);
            }
            writer.flush();
        }
        Files.move(tmpFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        log.trace("New {} is renamed to {}.", tmpFile, file);
    }

}
