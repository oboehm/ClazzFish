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

import clazzfish.monitor.io.ExtendedFile;
import clazzfish.monitor.stat.ClazzRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.regex.Pattern;

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
        ExtendedFile.createDir(file.getParentFile());
        File tmpFile = new File(file + "-" + System.currentTimeMillis());
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

    @Override
    public List<String> importCSV(URI uri) throws IOException {
        File file = new File(uri);
        List<String> csvLines = importCSV(file);
        return importTmpFiles(file, csvLines);
    }

    private static List<String> importCSV(File file) throws IOException {
        List<String> csvLines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            while (reader.ready()) {
                String line = reader.readLine();
                csvLines.add(line);
            }
            log.debug("{} lines imported from file '{}'.", csvLines.size(), file);
        }
        return csvLines;
    }

    private static List<String> importTmpFiles(File file, List<String> csvLines) throws IOException {
        Map<String, ClazzRecord> records = toClazzRecordMap(csvLines);
        String filename = file.getName();
        File dir = file.getParentFile();
        FileFilter filter = new FileFilter() {
            Pattern pattern = Pattern.compile(filename + "-.*[0-9]{13}$");
            @Override
            public boolean accept(File pathname) {
                return pattern.matcher(pathname.getName()).matches();
            }
        };
        for (File f : dir.listFiles(filter)) {
            addClazzRecordsTo(records, f);
            if (f.delete()) {
                log.info("Temporary file '{}' is deleted after import.", f);
            }
        }
        return toCsvLines(records);
    }

    private static void addClazzRecordsTo(Map<String, ClazzRecord> records, File file) throws IOException {
        List<String> csvLines = importCSV(file);
        Collection<ClazzRecord> newRecords = toClazzRecordMap(csvLines).values();
        for (ClazzRecord newRec : newRecords) {
            if (newRec.count() > 0) {
                ClazzRecord r = records.get(newRec.classname());
                if (r == null) {
                    records.put(newRec.classname(), newRec);
                } else if (r.count() < newRec.count()) {
                    records.put(newRec.classname(), newRec);
                }
            }
        }
    }

    private static Map<String, ClazzRecord> toClazzRecordMap(List<String> csvLines) {
        Map<String, ClazzRecord> records = new HashMap<>();
        for (String csvLine : csvLines) {
            try {
                ClazzRecord rec = ClazzRecord.fromCSV(csvLine);
                records.put(rec.classname(), rec);
            } catch (IllegalArgumentException ex) {
                log.debug("Line '{}' is ignored ({}).", csvLine, ex.getMessage());
                log.trace("Details:", ex);
            }
        }
        return records;
    }

    private static List<String> toCsvLines(Map<String, ClazzRecord> records) {
        List<String> csvLines = new ArrayList<>();
        csvLines.add(ClazzRecord.toCsvHeadline());
        for (ClazzRecord rec : records.values()) {
            csvLines.add(rec.toCSV());
        }
        return csvLines;
    }

}
