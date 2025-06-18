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
 * (c)reated 05.12.24 by oboehm
 */
package clazzfish.monitor;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * All config stuff is encapsulated in this class.
 *
 * @author oboehm
 * @since 2.3 (05.12.24)
 */
public final class Config {

    private static final Logger log = LoggerFactory.getLogger(Config.class);
    private final Properties properties;

    public static Config DEFAULT = new Config();

    private Config() {
        this(readProperties());
    }

    private Config(Properties properties) {
        this.properties = properties;
        log.debug("Config with {} is created.", properties);
    }

    private static Properties readProperties() {
        Properties props = new Properties();
        for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
            if (entry.getKey().toString().startsWith("clazzfish.")) {
                props.setProperty((String) entry.getKey(), (String) entry.getValue());
            }
        }
        for (Map.Entry<String, String> entry : System.getenv().entrySet()) {
            String envKey = entry.getKey().toLowerCase();
            if (envKey.startsWith("clazzfish")) {
                String key = envKey.replace('_', '.');
                props.setProperty(key, entry.getValue());
            }
        }
        return props;
    }

    /**
     * Gets the system property for the given key. If it is not set as system
     * property it tries to find it as environment variable (in uppercase and
     * with underscores instead of dots).
     *
     * @param key name of the property or environment variable (in lowercase)
     * @return found value or null
     */
    public static String getEnvironment(String key) {
        String value = System.getProperty(key);
        if (StringUtils.isBlank(value)) {
            String envKey = key.replace('.', '_').toUpperCase();
            log.debug("System property '{}' is not set, trying environment variable '{}'.", key, envKey);
            value = System.getenv(envKey);
        }
        return value;
    }

    /**
     * Get the directory where the collected dates and statistics are dumped
     * to.
     *
     * @return directory for export
     */
    public File getDumpDir() {
        String dirname = properties.getProperty("clazzfish.dump.dir");
        if (StringUtils.isNotBlank(dirname)) {
            return new File(dirname);
        } else {
            File dir = new File(SystemUtils.getJavaIoTmpDir(), "ClazzFish");
            return new File(dir, getAppName());
        }
    }

    /**
     * Here you can set the directory where the collected dates and statistics
     * should be dumped to.
     *
     * @param dir directory for export
     */
    public void setDumpDir(File dir) {
        setDumpURI(dir.toURI());
    }

    /**
     * Get the directory or URI where the collected dates and statistics are
     * dumped to.
     *
     * @return directory or URI for export
     */
    public URI getDumpURI() {
        String dumpUri = properties.getProperty("clazzfish.dump.uri");
        if (StringUtils.isBlank(dumpUri)) {
            return getDumpDir().toURI();
        } else {
            return URI.create(dumpUri);
        }
    }

    /**
     * Here you can set the directory or locaction where the collected dates
     * and statistics should be dumped to.
     *
     * @param uri directory or URI for export
     */
    public void setDumpURI(URI uri) {
        if (!Objects.equals(uri, this.getDumpURI())) {
            properties.setProperty("clazzfish.dump.uri", uri.toString());
            log.info("Dump-URI is set to '{}'.", uri);
        }
    }

    public String getProperty(String name) {
        return getEnvironment(name);
    }

    private static String getAppName() {
        String[] keys = { "appname", "app.name", "progname", "prog.name", "application.name", "spring.application.name" };
        for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
            for (String k : keys) {
                if (k.equals(entry.getKey().toString().toLowerCase())) {
                    log.debug("Using {} for name of CSV file.", entry);
                    return entry.getValue().toString();
                }
            }
        }
        log.trace("Using main class as application name.");
        return getStartClassname();
    }

    // from https://stackoverflow.com/questions/939932/how-to-determine-main-class-at-runtime-in-threaded-java-application
    private static String getStartClassname() {
        // find the class that called us, and use their "target/classes"
        final Map<Thread, StackTraceElement[]> traces = Thread.getAllStackTraces();
        for (Map.Entry<Thread, StackTraceElement[]> trace : traces.entrySet()) {
            if ("main".equals(trace.getKey().getName())) {
                // Using a thread named main is best...
                final StackTraceElement[] els = trace.getValue();
                for (int i = els.length - 1; i >= 0; i--) {
                    String cls = els[i].getClassName();
                    if (!isSystemClass(cls)) {
                        return cls;
                    }
                }
                return els[els.length-1].getClassName();
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
