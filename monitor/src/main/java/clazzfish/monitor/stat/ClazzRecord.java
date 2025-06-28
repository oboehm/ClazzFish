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

import java.net.URI;

/**
 * The ClazzRecord stores the relation of a class to its classpath and some
 * collected data.
 * <p>
 * NOTE: This was orginally a record class and was backported to Java 11.
 * </p>
 *
 * @author oboehm
 * @since 2.3 (25.11.24)
 */
public final class ClazzRecord implements Comparable<ClazzRecord> {

    private final URI classpath;
    private final String classname;
    private final int count;

    public ClazzRecord(URI classpath, String classname) {
        this(classpath, classname, 0);
    }

    /**
     * Creates a ClazzRecord object.
     *
     * @param classpath the URI of the classpath
     * @param classname the class name
     * @param count     the count how often a class was loaded
     */
    public ClazzRecord(URI classpath, String classname, int count) {
        this.classpath = classpath;
        this.classname = classname;
        this.count = count;
    }

    public String classname() {
        return classname;
    }

    public URI classpath() {
        return classpath;
    }

    public int count() {
        return count;
    }

    public static ClazzRecord fromCSV(String line) {
        String[] parts = line.split(";");
        if (parts.length < 2) {
            throw new IllegalArgumentException(String.format("to less columns (%d) in '%s'", parts.length, line));
        }
        if (parts.length == 2) {
            return new ClazzRecord(null, parts[0], Integer.parseInt(parts[1]));
        } else {
            return new ClazzRecord(URI.create(parts[0]), parts[1], Integer.parseInt(parts[2]));
        }
    }

    public static String toCsvHeadline() {
        return "Classpath;Classname;Count";
    }

    public String toCSV() {
        return classpath + ";" + classname + ";" + count;
    }

    @Override
    public int compareTo(ClazzRecord other) {
        int n = this.classname.compareTo(other.classname);
        if ((n == 0) && (this.classpath != null) && (other.classpath != null)) {
            n = this.classpath.compareTo(other.classpath);
        }
        return n;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof ClazzRecord) {
            return compareTo((ClazzRecord) o) == 0;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return classname.hashCode();
    }

    @Override
    public String toString() {
        return toCSV();
    }

}
