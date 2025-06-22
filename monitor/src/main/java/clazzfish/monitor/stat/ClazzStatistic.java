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

import clazzfish.monitor.ClasspathMonitor;
import clazzfish.monitor.Config;
import clazzfish.monitor.jmx.MBeanFinder;
import clazzfish.monitor.spi.CsvXPorter;
import clazzfish.monitor.spi.XPorter;
import clazzfish.monitor.util.Converter;
import clazzfish.monitor.util.Shutdowner;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

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
 * As alternative you can set one of the two environment variables:
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
public class ClazzStatistic extends Shutdowner implements ClazzStatisticMBean {

    private static final Logger log = LoggerFactory.getLogger(ClazzStatistic.class);
    private static final Executor EXECUTOR = Executors.newCachedThreadPool();
    private static final ClazzStatistic INSTANCE = new ClazzStatistic();
    private final ClasspathMonitor classpathMonitor;
    private final FutureTask<SortedSet<ClazzRecord>> allClasses;
    private final URI csvURI;
    private final CsvXPorter xPorter;

    static {
        log.trace("{} will be registered as shudown hook.", INSTANCE);
        INSTANCE.addMeAsShutdownHook();
    }

    public static ClazzStatistic getInstance() {
        return INSTANCE;
    }

    private ClazzStatistic() {
        this(getCsvURI());
    }

    ClazzStatistic(URI csvURI) {
        this(csvURI, ClasspathMonitor.getInstance());
    }

    private ClazzStatistic(URI csvURI, ClasspathMonitor classpathMonitor) {
        this.classpathMonitor = classpathMonitor;
        this.allClasses = collectFutureClasses(classpathMonitor);
        this.csvURI = csvURI;
        this.xPorter = XPorter.createCsvXPorter(csvURI);
        log.debug("Statistics will be imported from / exported to '{}'.", csvURI);
    }

    private static FutureTask<SortedSet<ClazzRecord>> collectFutureClasses(ClasspathMonitor cpmon) {
        FutureTask<SortedSet<ClazzRecord>> classes = new FutureTask<>(() -> collectClasses(cpmon));
        EXECUTOR.execute(classes);
        return classes;
    }

    private static SortedSet<ClazzRecord> collectClasses(ClasspathMonitor cpmon) {
        SortedSet<ClazzRecord> classes = new TreeSet<>();
        for (String classname : cpmon.getClasspathClasses()) {
            URI uri = getUri(cpmon, classname);
            classes.add(new ClazzRecord(uri, classname, 0));
        }
        return classes;
    }

    private static URI getUri(ClasspathMonitor cpmon, String classname) {
        URI uri = cpmon.whichClass(classname);
        String s = Objects.toString(uri, "");
        String resource = Converter.classToResource(classname);
        if (s.endsWith(resource)) {
            s = s.substring(0, s.length() - resource.length() - 1);
            if (s.endsWith("!")) {
                s = s.substring(0, s.length() - 1);
            }
            uri = URI.create(s);
        }
        return uri;
    }

    public void registerMeAsMBean() {
        MBeanFinder.registerMBean(this);
    }

    public SortedSet<ClazzRecord> getAllClasses() {
        try {
            return allClasses.get();
        } catch (ExecutionException | InterruptedException ex) {
            log.info("Cannot get all classes ({}).", ex.getMessage());
            log.debug("Details:", ex);
            return collectClasses(classpathMonitor);
        }
    }

    public SortedSet<ClazzRecord> getStatistics() {
        SortedSet<ClazzRecord> statistics = new TreeSet<>();
        Set<String> loaded = new HashSet<>(classpathMonitor.getLoadedClassnames());
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
            log.info("=== ClazzStatistic ===\n{}", sw);
        } catch (IOException ex) {
            log.error("Cannot log statistic:", ex);
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
            log.debug("Trying to export CSV file '{}':", filename, ex);
            return exportCSV(new File(filename).getAbsoluteFile());
        }
    }

    public URI exportCSV(File csvFile) throws IOException {
        log.debug("Exporting statistics to '{}'...", csvFile);
        if (csvFile.exists()) {
            importCSV();
        }
        exportDirect(csvFile);
        log.info("Statistics exported to '{}'.", csvFile);
        return csvFile.toURI();
    }

    public URI exportCSV(URI uri) throws IOException {
        importCSV(uri);
        log.info("Exporting statistics to '{}'...", uri);
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
        log.debug("Statistics exported with {} lines.", statistics.size());
    }

    private void importCSV() {
        URI csvURI = getExportURI();
        if (exists(csvURI)) {
            importCSV(csvURI);
        } else {
            log.debug("No '{}' for import available.", csvURI);
        }
    }

    private static boolean exists(URI uri) {
        if ("file".equalsIgnoreCase(uri.getScheme())) {
            File file = new File(uri);
            return file.exists();
        } else {
            log.trace("Can't check if {} exists.", uri);
            return true;
        }
    }

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
                log.debug("URI '{}' is empty and not imported.", csvURI);
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
                    log.debug("Line {} ({}) is ignored ({}).", i + 1, line, ex.getMessage());
                    log.trace("Details:", ex);
                }
            }
            log.debug("Class records from {} imported.", csvURI);
        } catch (IOException ex) {
            log.info("File '{}' cannot be imported ({}).", csvURI, ex.getMessage());
            log.debug("Details:", ex);
        }
    }

    @Override
    public void run() {
        try {
            exportCSV();
        } catch (IOException ex) {
            log.info("The class statistics could not be exported ({}).", ex.getMessage());
            log.debug("Details:", ex);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "-" + csvURI;
    }

    private static URI getCsvURI() {
        String filename = Config.getEnvironment("clazzfish.statistics.file");
        if (StringUtils.isNotBlank(filename)) {
            return new File(filename).toURI();
        } else {
            URI dumpURI = Config.DEFAULT.getDumpURI();
            return URI.create(dumpURI + "/ClazzStatistic.csv");
        }
    }

}
