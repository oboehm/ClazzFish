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
package clazzfish.core.spi;

import clazzfish.core.stat.ClazzRecord;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * The class FileXPorter accepts a file URI to import and export CSV data.
 *
 * @author oboehm
 * @since 2.5 (18.02.25)
 */
public class FileXPorter implements CsvXPorter {

    private static final Logger log = Logger.getLogger(FileXPorter.class.getName());
    private final URI uri;

    public FileXPorter(File file) {
        this(file.toURI());
    }

    public FileXPorter(URI uri) {
        this.uri = uri;
    }

    @Override
    public URI getURI() {
        return uri;
    }

    @Override
    public FileXPorter withURI(URI csvURI) {
        if (csvURI.equals(getURI())) {
            return this;
        } else {
            log.log(Level.FINER, "A new FileXPorter for URI {0} will be created.", csvURI);
            return new FileXPorter(csvURI);
        }
    }

    @Override
    public void exportCSV(URI uri, String csvHeadLine, List<String> csvLines) throws IOException {
        writeCSV(new File(uri), csvHeadLine, csvLines);
        log.log(Level.FINE, "Statistic exported with {0} lines to \"{0}\".", new Object[] { csvLines.size(), uri});
    }

    private void writeCSV(File file, String csvHeadLine, List<String> csvLines) throws IOException {
        createDir(file.getParentFile());
        File tmpFile = new File(file + "-" + System.currentTimeMillis());
        log.log(Level.FINER, "Statistic is temporary stored in \"{0}\".", tmpFile);
        try (PrintWriter writer = new PrintWriter(tmpFile)) {
            writer.println(csvHeadLine);
            for (String line : csvLines) {
                writer.println(line);
            }
            writer.flush();
        }
        Files.move(tmpFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        log.log(Level.FINER, "New {0} is renamed to {1}.", new Object[] { tmpFile, file });
    }

    private static void createDir(File dir) {
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                log.log(Level.FINE, "Directory \"{0}\" successful created.", dir);
            } else {
                log.log(Level.WARNING, "Cannot create dir \"{0}\" and will give up.", dir);
            }
        }
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
            log.log(Level.FINE, "{0} lines imported from file {1}.", new Object[] { csvLines.size(), file });
        }
        return csvLines;
    }

    private static List<String> importTmpFiles(File file, List<String> csvLines) throws IOException {
        Map<String, ClazzRecord> records = toClazzRecordMap(csvLines);
        String filename = file.getName();
        File dir = file.getParentFile();
        FileFilter filter = new FileFilter() {
            final Pattern pattern = Pattern.compile(filename + "-.*[0-9]{13}$");
            @Override
            public boolean accept(File pathname) {
                return pattern.matcher(pathname.getName()).matches();
            }
        };
        for (File f : dir.listFiles(filter)) {
            addClazzRecordsTo(records, f);
            if (f.delete()) {
                log.log(Level.INFO, "Temporary file \"{0}\" is deleted after import.", f);
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
                log.log(Level.FINE, "Line \"{0}\" is ignored ({1}).", new Object[] { csvLine, ex.getMessage() });
                log.log(Level.FINER, "Details:", ex);
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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FileXPorter)) return false;
        FileXPorter xPorter = (FileXPorter) o;
        return Objects.equals(uri, xPorter.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uri);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "-" + uri;
    }

}
