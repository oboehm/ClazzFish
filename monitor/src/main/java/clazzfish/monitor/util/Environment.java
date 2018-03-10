/*
 * Copyright (c) 2010-2018 by Oliver Boehm
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
 * (c)reated 01.02.2010 by oliver (ob@oasd.de)
 */

package clazzfish.monitor.util;

import clazzfish.monitor.ClasspathMonitor;
import clazzfish.monitor.io.FileInputStreamReader;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * This class provides some utilities for the access to the environment (e.g.
 * the system properties).
 * <p>
 * If you need different environments (e.g. for testing) this class is
 * no longer final to be able to derive from it. This class was copied from
 * PatternTesting.
 * </p>
 *
 * @author oliver
 */
public class Environment {

	private static final Logger LOG = LoggerFactory.getLogger(Environment.class);
	private static final String FALSE = "false";

	/** The only instance of this class. */
	public static final Environment INSTANCE = new Environment();

	/** System property to disable multithreading. */
	public static final String DISABLE_THREADS = "patterntesting.disableThreads";

	/** System property to enable integration tests. */
	public static final String INTEGRATION_TEST = "patterntesting.integrationTest";

	/** System property for annotation RunTestsParallel. */
	public static final String RUN_TESTS_PARALLEL = "patterntesting.runTestsParallel";

	/** System property for annotation SmokeTest. */
	public static final String RUN_SMOKE_TESTS = "patterntesting.runSmokeTests";

	/** True if property for integration test is set. */
	public static final boolean INTEGRATION_TEST_ENABLED = Environment.isPropertyEnabled(Environment.INTEGRATION_TEST);

	/** True if SmokeTest property is set. */
	public static final boolean SMOKE_TEST_ENABLED = Environment.isPropertyEnabled(Environment.RUN_SMOKE_TESTS);

	/**
	 * To be able to use different "environment" this class can now be
	 * sub-classed.
	 */
	protected Environment() {
	}

	/**
	 * Gets the os name.
	 *
	 * @return e.g. "Mac OS X"
	 */
	public String getOsName() {
		return SystemUtils.OS_NAME;
	}

	/**
	 * Gets the os arch.
	 *
	 * @return e.g. "x86_64"
	 */
	public String getOsArch() {
		return SystemUtils.OS_ARCH;
	}

	/**
	 * Gets the os version.
	 *
	 * @return e.g. "10.10.2"
	 */
	public String getOsVersion() {
		return SystemUtils.OS_VERSION;
	}

	/**
	 * Gets the java version.
	 *
	 * @return e.g. "1.6.0_17"
	 */
	public String getJavaVersion() {
		return SystemUtils.JAVA_VERSION;
	}

	/**
	 * Gets the java vendor.
	 *
	 * @return e.g. "Apple Inc."
	 */
	public String getJavaVendor() {
		return SystemUtils.JAVA_VENDOR;
	}

	/**
	 * Gets the user name.
	 *
	 * @return e.g. "oliver"
	 */
	public String getUserName() {
		return SystemUtils.USER_NAME;
	}

	/**
	 * The name of an environment is derived from the classloader. I.e. on a
	 * Tomcat server the name is "org.apache.catalina".
	 *
	 * @return e.g. "com.google.apphosting" for Google App Enginge
	 */
	public static String getName() {
		String name = getClassLoader().getClass().getName();
		String[] packages = name.split("\\.|\\$", 4);
		return packages[0] + "." + packages[1] + "." + packages[2];
	}

	/**
	 * Gets the class loader.
	 *
	 * @return a valid classloader
	 */
	public static ClassLoader getClassLoader() {
		ClassLoader cloader = Thread.currentThread().getContextClassLoader();
		if (cloader == null) {
			cloader = Environment.class.getClassLoader();
			LOG.warn("no ContextClassLoader found - using " + cloader);
		}
		return cloader;
	}

	/**
	 * Looks if one of the given properties matches a system property. If one
	 * system property is found and this system property is not "false" true is
	 * returned.
	 *
	 * @param props
	 *            the properties to be checked
	 * @return true if one of the given properties exist (and are not "false")
	 */
	public static boolean matchesOneOf(final String[] props) {
		if (StringUtils.isEmpty(props[0])) {
			LOG.debug("Empty properties are ignored for matching of system properties.");
			return true;
		}
		for (int i = 0; i < props.length; i++) {
			if (props[i].contains("*") || props[i].contains("?")) {
				LOG.warn("Wildcard in property \"" + props[i] + "\" is not supported!");
			}
			String prop = System.getProperty(props[i], FALSE);
			if (!FALSE.equalsIgnoreCase(prop)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns true if the given property is set as System property and the
	 * value of it is not false.
	 *
	 * @param key
	 *            e.g. "patterntesting.runTestsParallel"
	 * @return true if property is set
	 */
	public static boolean isPropertyEnabled(final String key) {
		String prop = System.getProperty(key, FALSE);
		return !FALSE.equalsIgnoreCase(prop);
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
			LOG.debug("using Environment.class to get " + resource + "...");
			istream = Environment.class.getResourceAsStream(resource);
		}
		Properties props = loadProperties(istream);
		istream.close();
		return props;
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
		for (Entry<Object, Object> entry : props.entrySet()) {
			systemProps.setProperty((String) entry.getKey(), (String) entry.getValue());
		}
		LOG.debug("{} properties loaded from {}.", props.size(), istream);
		return props;
	}

	/**
	 * In some JEE environment like Google's App Engine (GAE) it is not allowed
	 * to use multi threading. But you can also disable multi threading by
	 * setting the system property {@link #DISABLE_THREADS}.
	 *
	 * @return normally true
	 */
	public static boolean areThreadsAllowed() {
		if (isGoogleAppEngine() || isPropertyEnabled(DISABLE_THREADS)) {
			return false;
		}
		return true;
	}

	/**
	 * If we are in a Google App Engine (GAE) environment we will return true
	 * here.
	 *
	 * @return true if application runs on GAE
	 */
	public static boolean isGoogleAppEngine() {
		return System.getProperty("com.google.appengine.runtime.version") != null;
	}

	/**
	 * If we are inside a Weblogic Server (WLS) we will return true here. We
	 * look only for the system property "weblogic.Name" here. Another
	 * possibility (not realized) would be to look at the classloader.
	 *
	 * @return true inside Weblogic Server
	 */
	public static boolean isWeblogicServer() {
		return System.getProperty("weblogic.Name") != null;
	}

	/**
	 * Normally you'll find the local Maven repository at ~/.m2/repository. But
	 * on cloudbees it is the local .repository directory. So we try to find out
	 * here which one we should use.
	 *
	 * @return the root dir of the local Maven repository
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static File getLocalMavenRepositoryDir() throws IOException {
		File[] repoDirs = { getLocalRepository(), new File(".repository").getAbsoluteFile(),
				new File("../.repository").getAbsoluteFile() };
		for (int i = 0; i < repoDirs.length; i++) {
			if (repoDirs[i].exists() && repoDirs[i].isDirectory()) {
				return repoDirs[i];
			}
		}
		throw new IOException("local maven repository not found in " + Converter.toString(repoDirs));
	}

	private static File getLocalRepository() {
		File m2Dir = new File(SystemUtils.getUserHome(), ".m2");
		File settings = new File(m2Dir, "settings.xml");
		if (settings.exists()) {
			return getLocalRepositoryFrom(settings);
		}
		return new File(m2Dir, "repository");
	}

	private static File getLocalRepositoryFrom(File settings) {
		File m2Repo = new File(settings.getParentFile(), "repository");
		try (Reader reader = new FileInputStreamReader(settings, StandardCharsets.UTF_8)) {
			XMLInputFactory factory = XMLInputFactory.newInstance();
			XMLStreamReader xmlReader = factory.createXMLStreamReader(reader);
			while (xmlReader.next() != XMLStreamConstants.END_DOCUMENT) {
				if (xmlReader.isStartElement()) {
					QName name = xmlReader.getName();
					if ("localRepository".equalsIgnoreCase(name.getLocalPart())) {
						String content = xmlReader.getElementText();
						return new File(content);
					}
				}
			}
			LOG.debug("No <localRepository> found in {}.", settings);
		} catch (IOException | XMLStreamException ex) {
			LOG.warn("Will return {} as default because cannot read {}:", m2Repo, settings, ex);
		}
		return m2Repo;
	}

	/**
	 * It is only tested for Jamon 2.4 and 2.7 so we look for it
	 *
	 * @return true if Jamon 2.4 or 2.7 (or greater) was found
	 */
	public static boolean isJamonAvailable() {
		String resource = "/com/jamonapi/MonitorFactory.class";
		URL classURL = Environment.class.getResource(resource);
		if (classURL == null) {
			LOG.debug("JAMon and {} not available, using simple profiling.", resource);
			return false;
		}
		try (JarFile jarfile = ClasspathMonitor
				.whichResourceJar(ClasspathHelper.getParent(classURL.toURI(), resource))) {
			Manifest manifest = jarfile.getManifest();
			Attributes attributes = manifest.getMainAttributes();
			String version = attributes.getValue("version");
			if (version == null) {
				LOG.info("JAMon in {} available for profiling.", jarfile.getName());
				return true;
			} else if ("JAMon 2.4".equalsIgnoreCase(version) || (version.compareTo("JAMon 2.7") >= 0)) {
				LOG.info("{} available for profiling.", version);
				return true;
			} else {
				LOG.info("{} not supported (only JAMon 2.4 and 2.7 or higher), using simple profiling.", version);
			}
		} catch (IOException | URISyntaxException ex) {
			LOG.info("Will use simple profiling because cannot read manifest for {}:", classURL, ex);
		}
		return false;
	}

}
