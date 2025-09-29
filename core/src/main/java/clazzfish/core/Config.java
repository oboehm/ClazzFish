/*
 * Copyright (c) 2024,2025 by Oli B.
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
package clazzfish.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * All config stuff is encapsulated in this class.
 *
 * @author oboehm
 * @since 2.3 (05.12.24)
 */
public final class Config {

    private static final Logger log = Logger.getLogger(Config.class.getName());
    /** Property name of the directory where the class statistic is dumped to. */
    public static final String DUMP_DIR = "clazzfish.dump.dir";
    /** Property name of URI where the class statistic is dumped to. */
    public static final String DUMP_URI = "clazzfish.dump.uri";
    /** Pattern of the resources (and classes) which should be filtered out. */
    public static final String PATTERN_EXCLUDE = "clazzfish.pattern.exclude";
    private final Properties properties;

    public static Config DEFAULT = Config.of("clazzfish-default.properties", "clazzfish.properties");

    private Config(Properties properties) {
        this.properties = properties;
        log.fine(String.format("Config with %s is created.", properties));
    }

    public static Config of(String... resources) {
        Properties properties = new Properties();
        for (String rsc : resources) {
            Properties props = readProperties(rsc);
            properties.putAll(props);
        }
        return Config.of(properties);
    }

    public static Config of(Properties props) {
        return new Config(props);
    }

    private static Properties readProperties(String resource) {
        try {
            Properties props = loadProperties(resource);
            Properties sysProps = readProperties();
            props.putAll(sysProps);
            return props;
        } catch (IOException ex) {
            throw new IllegalArgumentException("not a resource: " + resource, ex);
        }
    }

    private static Properties readProperties() {
        Properties props = new Properties();
        for (Map.Entry<String, String> entry : System.getenv().entrySet()) {
            String envKey = entry.getKey().toLowerCase();
            if (envKey.startsWith("clazzfish")) {
                String key = envKey.replace('_', '.');
                props.setProperty(key, entry.getValue());
            }
        }
        log.finer(props.size() + " properties loaded from environment.");
        for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
            if (entry.getKey().toString().startsWith("clazzfish.")) {
                props.setProperty((String) entry.getKey(), (String) entry.getValue());
            }
        }
        log.finer(props.size() + " properties loaded from environment and system properties.");
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
        if (value == null) {
            String envKey = key.replace('.', '_').toUpperCase();
            log.finer(String.format("System property '%s' is not set, trying environment variable '%s'.", key, envKey));
            value = System.getenv(envKey);
        }
        return value;
    }

    /**
     * Loads the properties from the classpath and provides them as system
     * properties.
     *
     * @param resource
     *            the name of the classpath resource
     * @return the loaded properties
     * @throws IOException
     *             if properties can't be loaded
     * @see #loadProperties(InputStream)
     */
    public static Properties loadProperties(final String resource) throws IOException {
        ClassLoader cloader = getClassLoader();
        InputStream istream = cloader.getResourceAsStream(resource);
        if ((istream == null) && resource.startsWith("/")) {
            istream = cloader.getResourceAsStream(resource.substring(1));
        }
        if (istream == null) {
            log.fine(String.format("Using Config.class to get %s...", resource));
            istream = Config.class.getResourceAsStream(resource);
        }
        if (istream == null) {
            log.info(String.format("Resource '%s' is not available, using internal defaults.", resource));
            return new Properties();
        } else {
            Properties props = loadProperties(istream);
            istream.close();
            log.fine(String.format("%d properties loaded from '%s'.", props.size(), resource));
            return props;
        }
    }

    /**
     * Gets the class loader.
     *
     * @return a valid classloader
     */
    public static ClassLoader getClassLoader() {
        ClassLoader cloader = Thread.currentThread().getContextClassLoader();
        if (cloader == null) {
            cloader = getClassLoader();
            log.warning(String.format("No ContextClassLoader found - using %s.", cloader));
        }
        return cloader;
    }

    /**
	 * Loads the properties from the given InputStream and provides them as
	 * system properties.
	 * <p>
	 * Note: Setting it as system property is not guaranteed to run in a cluster
	 * or cloud. E.g. on Google's App Engine this seems not to work.
	 * </p>
	 *
	 * @param istream
	 *            from here the properties are loaded
	 * @return the loaded properties
	 * @throws IOException
	 *             if properties can't be loaded
	 */
	public static Properties loadProperties(final InputStream istream) throws IOException {
		Properties props = new Properties();
		props.load(istream);
		Properties systemProps = System.getProperties();
		for (Map.Entry<Object, Object> entry : props.entrySet()) {
			systemProps.setProperty((String) entry.getKey(), (String) entry.getValue());
		}
		log.fine(String.format("%d properties loaded from %s.", props.size(), istream));
		return props;
	}

    /**
     * Get the directory where the collected dates and statistics are dumped
     * to.
     *
     * @return directory for export
     */
    public File getDumpDir() {
        String dirname = properties.getProperty(DUMP_DIR);
        if (dirname != null) {
            return new File(dirname);
        } else {
            File dir = new File(System.getProperty("java.io.tmpdir", "/tmp"), "ClazzFish");
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
        String dumpUri = properties.getProperty(DUMP_URI);
        if (dumpUri == null) {
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
            properties.setProperty(DUMP_URI, uri.toString());
            log.info(String.format("Dump-URI is set to '%s'.", uri));
        }
    }

    public Properties getProperties() {
        return properties;
    }

    public String getProperty(String name) {
        return getEnvironment(name);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + properties;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Config)) return false;
        Config config = (Config) o;
        return Objects.equals(properties, config.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(properties);
    }

    private static String getAppName() {
        String[] keys = { "appname", "app.name", "progname", "prog.name", "application.name", "spring.application.name" };
        for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
            for (String k : keys) {
                if (k.equals(entry.getKey().toString().toLowerCase())) {
                    log.fine(String.format("Using %s for name of CSV file.", entry));
                    return entry.getValue().toString();
                }
            }
        }
        log.fine("Using main class as application name.");
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
