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
import clazzfish.monitor.util.Converter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.*;

/**
 * The ClazzRecorder collects classes and resources to find classes which are
 * likely to be dead.
 *
 * @author oboehm
 * @since 2.3 (25.11.24)
 */
public class ClazzRecorder {

    private static final ClazzRecorder INSTANCE = new ClazzRecorder();
    private final ClasspathMonitor classpathMonitor;
    private final SortedSet<ClazzRecord> classes;

    public static ClazzRecorder getInstance() {
        return INSTANCE;
    }

    private ClazzRecorder() {
        this(ClasspathMonitor.getInstance());
    }

    private ClazzRecorder(ClasspathMonitor classpathMonitor) {
        this.classpathMonitor = classpathMonitor;
        this.classes = collectClasses(classpathMonitor);
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

    public SortedSet<ClazzRecord> getStatistics() {
        SortedSet<ClazzRecord> statistics = new TreeSet<>();
        for (ClazzRecord record : classes) {
            if (classpathMonitor.isLoaded(record.classname())) {
                statistics.add(new ClazzRecord(record.classpath(), record.classname(), record.count()+1));
            } else {
                statistics.add(record);
            }
        }
        return statistics;
    }

    public void exportCSV(File csvFile) throws FileNotFoundException {
        try (PrintWriter writer = new PrintWriter(csvFile)) {
            for (ClazzRecord rec : getStatistics()) {
                writer.println(rec.toCSV());
            }
        }
    }

}
