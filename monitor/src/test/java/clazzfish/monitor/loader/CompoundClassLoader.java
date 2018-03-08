/*
 * Copyright (c) 2014-2018 by Oliver Boehm
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
 * (c)reated 26.08.2014 by oliver (ob@oasd.de)
 */

package clazzfish.monitor.loader;

import java.io.File;

/**
 * This is a simple implementation of {@link ClassLoader} to be able to
 * simulate the WebSphere classloader for testing. It was copied from the
 * PatternTesting project for testing.
 *
 * @author oliver
 * @since 1.5 (26.08.2014)
 */
public final class CompoundClassLoader extends ClassLoader {

    private final String localClassPath;

    /**
     * Instantiates a new compound class loader.
     *
     * @param classpath the classpath
     */
    public CompoundClassLoader(final File... classpath) {
        StringBuilder buf = new StringBuilder("");
        if (classpath.length > 0) {
            buf.append(classpath[0]);
            for (int i = 1; i < classpath.length; i++) {
                buf.append(File.pathSeparator);
                buf.append(classpath[i]);
            }
        }
        this.localClassPath = buf.toString();
    }

    /**
     * Gets the classpath.
     *
     * @return the classpath
     */
    public String getClasspath() {
        return this.localClassPath;
    }

}

