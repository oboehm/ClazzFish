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

    private final ClasspathMonitor classpathMonitor = ClasspathMonitor.getInstance();

    public Set<PathRecord> collectClasses() {
        Set<PathRecord> classes = new HashSet<>();
        for (String classname : classpathMonitor.getLoadedClasses()) {
            classes.add(toPathRecord(classname, 1));
        }
        for (String classname : classpathMonitor.getUnusedClasses()) {
            classes.add(toPathRecord(classname, 0));
        }
        return classes;
    }

    private PathRecord toPathRecord(String classname, int count) {
        URI uri = classpathMonitor.whichClass(classname);
        return new PathRecord(uri, classname, count);
    }

}
