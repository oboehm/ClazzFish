/*
 * Copyright (c) 2025 by Oli B.
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
 * (c)reated 24.09.25 by oboehm
 */
package clazzfish.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * The class SystemInfo provides some info about the VM.
 *
 * @author oboehm
 * @since 3.0 (24.09.25)
 */
public class SystemInfo {

    private static final Logger log = Logger.getLogger(SystemInfo.class.getName());

    /**
     * To get the boot classpath the sytem property "sun.boot.class.path" is
     * used to get them. This will work of course only for the SunVM.
     *
     * @return the boot classpath as String array
     */
    public static String[] getBootClasspath() {
        return getClasspath("sun.boot.class.path");
    }

    /**
     * To get the classpath the sytem property "java.class.path" is
     * used to get them.
     *
     * @return the classpath as String array
     */
    public static String[] getClasspath() {
        return getClasspath("java.class.path");
    }

    static String[] getClasspath(final String key) {
        String classpath = System.getProperty(key);
        if (classpath == null) {
            log.finest(key + "is not set (not a SunVM or JDK 9+)");
            return new String[0];
        }
        String[] cp = splitClasspath(classpath);
        return validatedClasspath(cp);
    }

    private static String[] validatedClasspath(String[] classpathes) {
        List<String> validated = new ArrayList<>();
        for (String name : classpathes) {
            File path = new File(name);
            if (name.contains("!") || path.exists()) {
                validated.add(name);
            } else {
                log.fine(String.format("'%s' in classpath is ignored because it does not exists.", path));
            }
        }
        return validated.toArray(new String[0]);
    }

    private static String[] splitClasspath(final String classpath) {
        String[] cp = classpath.split(File.pathSeparator);
        for (int i = 0; i < cp.length; i++) {
            if (cp[i].endsWith(File.separator)) {
                cp[i] = cp[i].substring(0, (cp[i].length() - 1));
            }
        }
        return cp;
    }

}