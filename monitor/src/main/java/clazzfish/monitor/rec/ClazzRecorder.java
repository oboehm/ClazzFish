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

import clazzfish.monitor.ClasspathMonitor;
import clazzfish.monitor.jmx.MBeanHelper;
import clazzfish.monitor.util.Converter;
import clazzfish.monitor.util.Shutdowner;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The ClazzRecorder collects classes and resources to find classes which are
 * likely to be dead. At the end a little statistics is eported to a file
 * 'clazzfish/a.b.MyMain/statistics.csv' in the temp direktory.
 * If you want another directory or filename where this statistics should be
 * stored you can use one of the system properties
 * <ol>
 *     <li>clazzfish.statistics.dir</li>
 *     <li>clazzfish.statistics.file</li>
 * </ol>
 * Please use only one of this environment options.
 *
 * @author oboehm
 * @since 2.3 (25.11.24)
 */
public class ClazzRecorder extends Shutdowner implements ClazzRecorderMBean {

    private static final Logger log = LoggerFactory.getLogger(ClazzRecorder.class);
    private static final ClazzRecorder INSTANCE = new ClazzRecorder();
    private final ClasspathMonitor classpathMonitor;
    private final SortedSet<ClazzRecord> classes;
    private final File csvFile;

    static {
        log.trace("{} will be registered as shudown hook.", INSTANCE);
        INSTANCE.addMeAsShutdownHook();
    }

    public static ClazzRecorder getInstance() {
        return INSTANCE;
    }

    private ClazzRecorder() {
        this(ClasspathMonitor.getInstance());
    }

    private ClazzRecorder(ClasspathMonitor classpathMonitor) {
        this.classpathMonitor = classpathMonitor;
        this.classes = collectClasses(classpathMonitor);
        csvFile = getCsvFile();
        log.debug("Statistics will be imported from / exported to '{}'.", csvFile);
        if (csvFile.exists()) {
            try {
                importCSV(csvFile);
            } catch (IOException ex) {
                log.info("History could not be imported from {} ({}).", csvFile, ex.getMessage());
                log.debug("Details:", ex);
            }
        }
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
        MBeanHelper.registerMBean(MBeanHelper.getAsObjectName(this.getClass()), this);
    }

    public SortedSet<ClazzRecord> getStatistics() {
        SortedSet<ClazzRecord> statistics = new TreeSet<>();
        Set<String> loaded = classpathMonitor.getLoadedClassList().stream().map(Class::getName).collect(
                Collectors.toSet());
        for (ClazzRecord record : classes) {
            if (loaded.contains(record.classname())) {
                statistics.add(new ClazzRecord(record.classpath(), record.classname(), record.count()+1));
            } else {
                statistics.add(record);
            }
        }
        return statistics;
    }

    public File exportCSV() throws FileNotFoundException {
        File dir = csvFile.getParentFile();
        if (dir.mkdirs()) {
            log.info("Export dir {} was created.", dir);
        }
        return exportCSV(csvFile);
    }

    @Override
    public File exportCSV(String filename) throws FileNotFoundException {
        return exportCSV(new File(filename)).getAbsoluteFile();
    }

    public File exportCSV(File csvFile) throws FileNotFoundException {
        SortedSet<ClazzRecord> statistics = getStatistics();
        try (PrintWriter writer = new PrintWriter(csvFile)) {
            for (ClazzRecord rec : statistics) {
                writer.println(rec.toCSV());
            }
        }
        log.debug("{} class records to {} exported.", statistics.size(), csvFile);
        return csvFile;
    }

    public void importCSV(File csvFile) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            while (reader.ready()) {
                String line = reader.readLine();
                ClazzRecord r = ClazzRecord.fromCSV(line);
                String classname = r.classname();
                Optional<ClazzRecord> any = classes.stream().filter(cr -> classname.equals(cr.classname())).findAny();
                if (any.isPresent()) {
                    classes.remove(any.get());
                    r = new ClazzRecord(r.classpath(), r.classname(), r.count()+any.get().count());
                    classes.add(r);
                }
            }
        }
        log.debug("Class records from {} imported.", csvFile);
    }

    @Override
    public void run() {
        try {
            exportCSV();
        } catch (FileNotFoundException ex) {
            log.info("The class statistics could not be exported ({}).", ex.getMessage());
            log.debug("Details:", ex);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "-" + csvFile;
    }

    private static File getCsvFile() {
        String mainClass = getMainClass();
        String filename = System.getProperty("clazzfish.statistics.file");
        if (StringUtils.isBlank(filename)) {
            File dir = getCsvDir(mainClass);
            return new File(dir, "statistics.csv");
        } else {
            return new File(filename);
        }
    }

    private static File getCsvDir(String mainClass) {
        File dir = new File(SystemUtils.getJavaIoTmpDir(), "ClazzFish/" + mainClass);
        String dirname = System.getProperty("clazzfish.statistics.dir");
        if (StringUtils.isNotBlank(dirname)) {
            dir = new File(dirname);
        }
        return dir;
    }

    // from https://stackoverflow.com/questions/939932/how-to-determine-main-class-at-runtime-in-threaded-java-application
    private static String getMainClass() {
        // find the class that called us, and use their "target/classes"
        final Map<Thread, StackTraceElement[]> traces = Thread.getAllStackTraces();
        for (Map.Entry<Thread, StackTraceElement[]> trace : traces.entrySet()) {
            if ("main".equals(trace.getKey().getName())) {
                // Using a thread named main is best...
                final StackTraceElement[] els = trace.getValue();
                int i = els.length - 1;
                StackTraceElement best = els[--i];
                String cls = best.getClassName();
                while (i > 0 && isSystemClass(cls)) {
                    // if the main class is likely an ide,
                    // then we should look higher...
                    while (i-- > 0) {
                        if ("main".equals(els[i].getMethodName())) {
                            best = els[i];
                            cls = best.getClassName();
                            break;
                        }
                    }
                }
                if (isSystemClass(cls)) {
                    i = els.length - 1;
                    best = els[i];
                    while (isSystemClass(cls) && i --> 0) {
                        best = els[i];
                        cls = best.getClassName();
                    }
                }
                return best.getClassName();
            }
        }
        return "unknown";
    }

    private static boolean isSystemClass(String cls) {
        return cls.startsWith("java.") ||
                cls.startsWith("jdk.") ||
                cls.startsWith("sun.") ||
                cls.startsWith("org.apache.maven.") ||
                cls.contains(".intellij.") ||
                cls.startsWith("org.junit") ||
                cls.startsWith("junit.") ||
                cls.contains(".eclipse") ||
                cls.contains("netbeans");
    }

}
