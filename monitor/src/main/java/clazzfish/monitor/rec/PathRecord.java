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

import java.net.URI;

/**
 * The PathRecord stores the relation of a class to its classpath and some
 * collected data.
 *
 * @author oboehm
 * @since 2.3 (25.11.24)
 */
public record PathRecord(URI classpath, String classname, int count) implements Comparable<PathRecord> {

    public PathRecord(URI classpath, String classname) {
        this(classpath, classname, 0);
    }

    public String toCSV() {
        return classpath + ";" + classname + ";" + count;
    }

    @Override
    public int compareTo(PathRecord other) {
        int n = this.classpath.compareTo(other.classpath);
        if (n == 0) {
            n = this.classname.compareTo(other.classname);
        }
        if (n == 0) {
            n = this.count - other.count;
        }
        return n;
    }

}
