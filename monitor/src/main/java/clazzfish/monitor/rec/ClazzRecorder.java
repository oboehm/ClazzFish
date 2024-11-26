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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

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
    private final Set<PathRecord> classes;

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

    private static Set<PathRecord> collectClasses(ClasspathMonitor cpmon) {
        Set<PathRecord> classes = new HashSet<>();
        for (String classname : cpmon.getClasspathClasses()) {
            URI uri = cpmon.whichClass(classname);
            classes.add(new PathRecord(uri, classname, 0));
        }
        return classes;
    }

    public Set<PathRecord> getClasses() {
        return classes;
    }

    public void exportCSV(File csvFile) throws FileNotFoundException {
        try (PrintWriter writer = new PrintWriter(csvFile)) {
            for (PathRecord rec : classes) {
                writer.println(rec.toCSV());
            }
        }
    }

}
