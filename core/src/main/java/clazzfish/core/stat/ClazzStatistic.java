/*
 * Copyright (c) 2024,2025 by Oli B.
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
package clazzfish.core.stat;

import clazzfish.core.Digger;
import clazzfish.core.jmx.MBeanFinder;
import clazzfish.core.spi.CsvXPorter;
import clazzfish.core.util.ShutdownHook;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The ClazzStatistic collects classes and resources to find classes which are
 * likely to be dead. At the end a little statistics is reported to a file
 * 'clazzfish/a.b.MyMain/ClazzStatistic.csv' in the temp directory.
 * If you want another directory or filename where this statistics should be
 * stored you can use one of the system properties
 * <ol>
 *     <li>clazzfish.dump.dir</li>
 *     <li>clazzfish.statistics.file</li>
 * </ol>
 * Please use only one of this environment options.
 * <p>
 * As an alternative you can set one of the two environment variables:
 * </p>
 * <ol>
 *     <li>CLAZZFISH_STATISTICS_DIR</li>
 *     <li>CLAZZFISH_STATISTICS_FILE</li>
 * </ol>
 * <p>
 * NOTE: If a system property like 'appname' is set which looks like a program
 * name this property is used instead of the Main classname.
 * </p>
 *
 * @author oboehm
 * @since 2.3 (25.11.24)
 */
public class ClazzStatistic extends ShutdownHook implements ClazzStatisticMBean {

    private static final Logger log = Logger.getLogger(ClazzStatistic.class.getName());
    private static final Executor EXECUTOR = Executors.newCachedThreadPool();
    private static final Map<CsvXPorter, ClazzStatistic> INSTANCES = new ConcurrentHashMap<>();
    private final Digger classpathDigger = new Digger();
    private final FutureTask<Set<ClazzRecord>> allClasses;
    private final URI csvURI;
    private final CsvXPorter xPorter;

    public static ClazzStatistic of(CsvXPorter xPorter) {
        URI csvURI = xPorter.getURI();
        if (!csvURI.toString().endsWith(".csv")) {
            csvURI = java.net.URI.create(csvURI + "/ClazzStatistic.csv");
            xPorter = xPorter.withURI(csvURI);
        }
        ClazzStatistic cached = INSTANCES.get(xPorter);
        if (cached == null) {
            cached = new ClazzStatistic(csvURI, xPorter);
            INSTANCES.put(xPorter, cached);
        }
        return cached;
    }

    private ClazzStatistic(URI csvURI, CsvXPorter xPorter) {
        this.xPorter = xPorter;
        this.allClasses = collectFutureClasses(classpathDigger);
        this.csvURI = csvURI;
        log.log(Level.FINE, "Statistics will be imported from / exported to \"{0}\".", csvURI);
    }

    private static FutureTask<Set<ClazzRecord>> collectFutureClasses(Digger digger) {
        FutureTask<Set<ClazzRecord>> classes = new FutureTask<>(digger::getClassRecords);
        EXECUTOR.execute(classes);
        return classes;
    }

    public void registerMeAsMBean() {
        MBeanFinder.registerMBean(this);
    }

    public Set<ClazzRecord> getAllClasses() {
        try {
            return allClasses.get();
        } catch (ExecutionException | InterruptedException ex) {
            log.log(Level.INFO, "Cannot get all classes ({0}).", ex.getMessage());
            log.log(Level.FINE, "Details:", ex);
            return classpathDigger.getClassRecords();
        }
    }

    public CsvXPorter getXPorter() {
        return xPorter;
    }

    public SortedSet<ClazzRecord> getStatistics() {
        SortedSet<ClazzRecord> statistics = new TreeSet<>();
        Set<String> loaded = Set.of(classpathDigger.getLoadedClassnames());
        for (ClazzRecord record : getAllClasses()) {
            if (loaded.contains(record.classname())) {
                statistics.add(new ClazzRecord(record.classpath(), record.classname(), record.count()+1));
            } else {
                statistics.add(record);
            }
        }
        return statistics;
    }

    public Set<URI> getClasspathes() {
        Set<URI> classpathes = new TreeSet<>();
        for (ClazzRecord record : getAllClasses()) {
            classpathes.add(record.classpath());
        }
        return classpathes;
    }

    /**
     * Prints the statistic as CSV to the log output.
     */
    @Override
    public void logMe() {
        importCSV();
        try (StringWriter sw = new StringWriter();
             PrintWriter writer = new PrintWriter(sw)) {
            writeCSV(writer);
            sw.flush();
            log.log(Level.INFO, "=== ClazzStatistic ===\n{0}", sw);
        } catch (IOException ex) {
            log.log(Level.SEVERE, "Cannot log statistic:", ex);
        }
    }

    @Override
    public URI getExportURI() {
        return csvURI;
    }

    @Override
    public URI exportCSV() throws IOException {
        return exportCSV(csvURI);
    }

    @Override
    public URI exportCSV(String filename) throws IOException {
        try {
            return exportCSV(new URI(filename));
        } catch (URISyntaxException ex) {
            log.log(Level.FINE, String.format("Trying to export CSV file '%s':", filename), ex);
            return exportCSV(new File(filename).getAbsoluteFile());
        }
    }

    public URI exportCSV(File csvFile) throws IOException {
        log.log(Level.FINE, "Exporting statistics to \"{0}\"...", csvFile);
        if (csvFile.exists()) {
            importCSV();
        }
        exportDirect(csvFile);
        log.log(Level.INFO, "Statistics exported to \"{0}\".", csvFile);
        return csvFile.toURI();
    }

    public URI exportCSV(URI uri) throws IOException {
        importCSV(uri);
        log.log(Level.INFO, "Exporting statistics to \"{0}\"...", uri);
        List<String> csvLines = getCsvLines();
        xPorter.exportCSV(uri, ClazzRecord.toCsvHeadline(), csvLines);
        return uri;
    }

    private void exportDirect(File file) throws IOException {
        List<String> csvLines = getCsvLines();
        xPorter.exportCSV(file.toURI(), ClazzRecord.toCsvHeadline(), csvLines);
    }

    private List<String> getCsvLines() {
        SortedSet<ClazzRecord> statistics = getStatistics();
        List<String> csvLines = new ArrayList<>();
        for (ClazzRecord rec : statistics) {
            csvLines.add(rec.toCSV());
        }
        return csvLines;
    }

    private void writeCSV(PrintWriter writer) {
        SortedSet<ClazzRecord> statistics = getStatistics();
        writer.println(ClazzRecord.toCsvHeadline());
        for (ClazzRecord rec : statistics) {
            writer.println(rec.toCSV());
        }
        writer.flush();
        log.log(Level.FINE, "Statistics exported with {0} lines.", statistics.size());
    }

    private void importCSV() {
        URI csvURI = getExportURI();
        if (exists(csvURI)) {
            importCSV(csvURI);
        } else {
            log.log(Level.FINE, "No \"{0}\" for import available.", csvURI);
        }
    }

    private static boolean exists(URI uri) {
        if ("file".equalsIgnoreCase(uri.getScheme())) {
            File file = new File(uri);
            return file.exists();
        } else {
            log.log(Level.FINER, "Can't check if {0} exists.", uri);
            return true;
        }
    }

    /**
     * Importes the statistics form the given filename or URI.
     *
     * @param filename filename or URI
     * @since 2.7
     */
    @Override
    public void importCSV(String filename) {
        log.log(Level.FINER, "Statistic will be imported from \"{0}\"...", filename);
        URI uri = (filename.contains(":")) ? URI.create(filename) : new File(filename).toURI();
        importCSV(uri);
        log.log(Level.INFO, "Statistic was imported from \"{0}\".", uri);
    }

    /**
     * Importes the statistics form the given file.
     *
     * @param csvFile import file
     * @deprecated use {@link #importCSV(URI)} with csvFile.toURI() as parameter
     */
    @Deprecated(forRemoval = true)
    public void importCSV(File csvFile) {
        importCSV(csvFile.toURI());
    }

    /**
     * Imports the statistics from the given URI. With issue #26 an internal
     * {@link Map} is now used to find a {@link ClazzRecord}. The speeds up
     * the import by a factor of 5:
     * <ul>
     *     <li>import of 10,000 lines: 130 - 150 ms (old), 20 - 30 ms (now)</li>
     *     <li>import of 60,000 lines: 200 - 230 ms (old), 50 - 80 ms (now)</li>
     * </ul>
     * The times were measured on an MBP from 2020.
     *
     * @param csvURI URI where the statistic should be imported from
     */
    public void importCSV(URI csvURI) {
        try {
            List<String> csvLines = xPorter.importCSV(csvURI);
            if (csvLines.isEmpty()) {
                log.log(Level.FINE, "URI \"{0}\" is empty and not imported.", csvURI);
                return;
            }
            Map<String, ClazzRecord> loaded = new HashMap<>();
            for (ClazzRecord record : getAllClasses()) {
                loaded.put(record.classname(), record);
            }
            int start = csvLines.get(0).equals(ClazzRecord.toCsvHeadline()) ? 1 : 0;
            for (int i = start; i < csvLines.size(); i++) {
                String line = csvLines.get(i);
                try {
                    ClazzRecord r = ClazzRecord.fromCSV(line);
                    if (r.count() == 0) {
                        continue;
                    }
                    String classname = r.classname();
                    ClazzRecord clazzRecord = loaded.get(classname);
                    if (clazzRecord != null) {
                        getAllClasses().remove(clazzRecord);
                        r = new ClazzRecord(clazzRecord.classpath(), clazzRecord.classname(),
                                r.count() + clazzRecord.count());
                    }
                    getAllClasses().add(r);
                } catch (IllegalArgumentException ex) {
                    log.log(Level.FINE, "Line {0} ({1}) is ignored ({2}).", new Object[]{i + 1, line, ex.getMessage()});
                    log.log(Level.FINER, "Details:", ex);
                }
            }
            log.log(Level.FINE, "Class records from {0} imported.", csvURI);
        } catch (IOException ex) {
            log.log(Level.INFO, "URI \"{0}\" cannot be imported ({0}).", new Object[]{csvURI, ex.getMessage()});
            log.log(Level.FINE, "Details:", ex);
        }
    }

    @Override
    public String getSummary() {
        Set<ClazzRecord> clazzRecords = getStatistics();
        long lc = clazzRecords.stream().filter(cr -> cr.count() > 0).count();
        long ac = clazzRecords.size();
        long dc = ac - lc;
        return String.format("%d classes: %d loaded (%d%%), %d dead (%d%%)", ac,
                lc, (lc * 100 + ac/2) / ac, dc, (dc * 100 + dc/2) / ac);
    }

    @Override
    public void run() {
        try {
            exportCSV();
        } catch (IOException ex) {
            log.log(Level.INFO, "The class statistics could not be exported ({0}).", ex.getMessage());
            log.log(Level.FINE, "Details:", ex);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "-" + csvURI;
    }

}
