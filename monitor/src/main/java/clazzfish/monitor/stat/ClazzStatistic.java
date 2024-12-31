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
import clazzfish.monitor.internal.Config;
import clazzfish.monitor.io.ExtendedFile;
import clazzfish.monitor.jmx.MBeanFinder;
import clazzfish.monitor.util.Converter;
import clazzfish.monitor.util.Shutdowner;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.stream.Collectors;

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
    private final File csvFile;

    static {
        log.trace("{} will be registered as shudown hook.", INSTANCE);
        INSTANCE.addMeAsShutdownHook();
    }

    public static ClazzStatistic getInstance() {
        return INSTANCE;
    }

    private ClazzStatistic() {
        this(getCsvFile());
    }

    ClazzStatistic(File csvFile) {
        this(csvFile, ClasspathMonitor.getInstance());
    }

    private ClazzStatistic(File csvFile, ClasspathMonitor classpathMonitor) {
        this.classpathMonitor = classpathMonitor;
        this.allClasses = collectFutureClasses(classpathMonitor);
        this.csvFile = csvFile;
        log.debug("Statistics will be imported from / exported to '{}'.", csvFile);
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
            log.info("Trying again to get all classes ({}).", ex.getMessage());
            log.debug("Details:", ex);
            return getAllClasses();
        }
    }

    public SortedSet<ClazzRecord> getStatistics() {
        SortedSet<ClazzRecord> statistics = new TreeSet<>();
        Set<String> loaded = classpathMonitor.getLoadedClassList().stream().map(Class::getName).collect(
                Collectors.toSet());
        for (ClazzRecord record : getAllClasses()) {
            if (loaded.contains(record.classname())) {
                statistics.add(new ClazzRecord(record.classpath(), record.classname(), record.count()+1));
            } else {
                statistics.add(record);
            }
        }
        return statistics;
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
            writer.flush();
            sw.flush();
            log.info("=== ClazzStatistic ===\n{}", sw);
        } catch (IOException ex) {
            log.error("Cannot log statistic:", ex);
        }
    }

    @Override
    public File getExportFile() {
        return csvFile;
    }

    @Override
    public File exportCSV() throws IOException {
        File dir = csvFile.getParentFile();
        if (dir.mkdirs()) {
            log.debug("Export dir {} was created.", dir);
        }
        return exportCSV(csvFile);
    }

    @Override
    public File exportCSV(String filename) throws IOException {
        return exportCSV(new File(filename)).getAbsoluteFile();
    }

    public File exportCSV(File csvFile) throws IOException {
        log.debug("Exporting statistics to '{}'...", csvFile);
        if (csvFile.exists()) {
            importCSV();
        } else {
            ExtendedFile.createDir(csvFile.getParentFile());
        }
        try (PrintWriter writer = new PrintWriter(csvFile)) {
            writeCSV(writer);
        }
        log.info("Statistics exported to '{}'.", csvFile);
        return csvFile;
    }

    private void writeCSV(PrintWriter writer) {
        SortedSet<ClazzRecord> statistics = getStatistics();
        writer.println(ClazzRecord.toCsvHeadline());
        for (ClazzRecord rec : statistics) {
            writer.println(rec.toCSV());
        }
        log.debug("Statistics exported with {} lines.", statistics.size());
    }

    private void importCSV() {
        if (csvFile.exists()) {
            try {
                importCSV(csvFile);
            } catch (IOException ex) {
                log.info("History could not be imported from {} ({}).", csvFile, ex.getMessage());
                log.debug("Details:", ex);
            }
        } else {
            log.debug("No CSV file '{}' for import available.", csvFile);
        }
    }

    public void importCSV(File csvFile) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            while (reader.ready()) {
                String line = reader.readLine();
                if (line.startsWith(ClazzRecord.toCsvHeadline())) {
                    continue;
                }
                ClazzRecord r = ClazzRecord.fromCSV(line);
                if (r.count() == 0) {
                    continue;
                }
                String classname = r.classname();
                Optional<ClazzRecord> any = getAllClasses().stream().filter(cr -> classname.equals(cr.classname())).findAny();
                if (any.isPresent()) {
                    getAllClasses().remove(any.get());
                    r = new ClazzRecord(r.classpath(), r.classname(), r.count()+any.get().count());
                    getAllClasses().add(r);
                }
            }
        }
        log.debug("Class records from {} imported.", csvFile);
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
        return getClass().getSimpleName() + "-" + csvFile;
    }

    private static File getCsvFile() {
        String filename = Config.getEnvironment("clazzfish.statistics.file");
        if (StringUtils.isNotBlank(filename)) {
            return new File(filename);
        } else {
            return new File(Config.DEFAULT.getDumpDir(), "ClazzStatistic.csv");
        }
    }

}
