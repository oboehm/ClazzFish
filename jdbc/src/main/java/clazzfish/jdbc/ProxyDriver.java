/*
 * Copyright (c) 2014 by Oliver Boehm
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
 * (c)reated 27.03.2014 by oliver (ob@oasd.de)
 */

package clazzfish.jdbc;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

/**
 * This JDBC driver acts like a proxy between PatternTesting and the real JDBC
 * driver to be able to monitor JDBC access. It was inspired by the JAMonDriver
 * of the JAMon framework.
 * <p>
 * This driver is registered for JDBC URLs beginning with "<em>jdbc:proxy:</em>
 * ...". This prefix must follow the real driver path. E.g. if you want to use
 * HSQL as database your URL make look like
 * "<em>jdbc:proxy:hsqldb:file:/tmp/oli</em>".
 * </p>
 *
 * @author oliver
 * @since 0.9
 */
public class ProxyDriver implements Driver {

	private static final String JDBC_URL_PREFIX = "jdbc:proxy:";
	private static final Logger LOG = LoggerFactory.getLogger(ProxyDriver.class);
	private static final Map<String, String> KNOWN_DRIVERS = new HashMap<>();

	/* Register class as JDBC driver. */
	static {
		register();
		KNOWN_DRIVERS.put("hsqldb", "org.hsqldb.jdbc.JDBCDriver");
		KNOWN_DRIVERS.put("informix-sqli", "com.informix.jdbc.IfxDriver");
		KNOWN_DRIVERS.put("jturbo", "com.newatlanta.jturbo.driver.Driver");
		KNOWN_DRIVERS.put("sqlserver", "com.microsoft.sqlserver.jdbc.SQLServerDriver");
		KNOWN_DRIVERS.put("postgresql", "org.postgresql.Driver");
	}

	private static String getKnownDriverFor(final String jdbcURL) {
		String[] parts = StringUtils.split(jdbcURL + ":x", ':');
		return KNOWN_DRIVERS.get(parts[1].toLowerCase());
	}

	/**
	 * Registers the driver as JDBC driver.
	 * <p>
	 * Because the implementation how the {@link DriverManager} finds the
	 * corresponding driver for a given JDBC-URL is a little stupid - it tries
	 * out all drivers if it can work with the URL. In case of problems (e.g. if
	 * the network is down) it throws the first {@link SQLException} it got and
	 * rethrow it - but this may be not the reason for the error message that
	 * the protocoll "jdbc:proxy:..." can not be understood.
	 * </p>
	 * <p>
	 * This is the reason why first all registered drivers are first
	 * deregistered and the registered again. This guarantees that the
	 * ProxyDriver is the first registered driver.
	 * </p>
	 */
	public static void register() {
		Driver driver = new ProxyDriver();
		try {
			Set<Driver> deregistered = deregisterDrivers();
			DriverManager.registerDriver(driver);
			registerDrivers(deregistered);
			LOG.debug("{} successful registered as JDBC driver.", driver);
		} catch (SQLException ex) {
			DriverManager.println("Cannot register " + driver + " because of " + ex.getMessage() + ".");
			LOG.error("Cannot register {} as JDBC driver.", driver, ex);
		}
	}

	private static Set<Driver> deregisterDrivers() {
		Set<Driver> deregistered = new HashSet<>();
		Enumeration<Driver> registered = DriverManager.getDrivers();
		while (registered.hasMoreElements()) {
			deregistered.add(registered.nextElement());
		}
		for (Driver driver : deregistered) {
			try {
				DriverManager.deregisterDriver(driver);
			} catch (SQLException sex) {
				LOG.debug("Cannot deregister {} from DriverManager.", driver);
				LOG.trace("Details: ", sex);
				deregistered.remove(driver);
			}
		}
		LOG.trace("{} driver(s) deregistered.", deregistered.size());
		return deregistered;
	}

	private static void registerDrivers(final Set<Driver> deregistered) {
		for (Driver driver : deregistered) {
			try {
				DriverManager.registerDriver(driver);
			} catch (SQLException sex) {
				LOG.warn("Cannot register {} again:", driver, sex);
			}
		}
	}

	/**
	 * Gets the real JDBC URL of the underlying driver.
	 *
	 * @param jdbcURL the jdbc url, e.g. "jdbc:proxy:hsqldb:mem:testdb"
	 * @return the real driver name
	 */
	public static String getRealURL(final String jdbcURL) {
		if (jdbcURL.startsWith(JDBC_URL_PREFIX)) {
			return "jdbc:" + StringUtils.substring(jdbcURL, JDBC_URL_PREFIX.length());
		} else {
			return jdbcURL;
		}
	}

	/**
	 * Gets the real driver name of the underlying driver.
	 *
	 * @param jdbcURL the jdbc url, e.g. "jdbc:proxy:hsqldb:mem:testdb"
	 * @return the real driver name
	 */
	public static String getRealDriverName(final String jdbcURL) {
		return getDriverName(getRealURL(jdbcURL));
	}

	private static String getDriverName(final String jdbcURL) {
		String driverName = getKnownDriverFor(jdbcURL);
		if (driverName == null) {
			return getDriver(jdbcURL).getClass().getName();
		}
		return driverName;
	}

	/**
	 * Gets the real driver.
	 *
	 * @param jdbcURL the jdbc url, e.g. "jdbc:proxy:hsqldb:mem:testdb"
	 * @return the real driver
	 */
	public static Driver getRealDriver(final String jdbcURL) {
		String realURL = getRealURL(jdbcURL);
		return getDriver(realURL);
	}

	private static Driver getDriver(final String url) {
		try {
			return DriverManager.getDriver(url);
		} catch (SQLException ex) {
			LOG.trace("Cannot get driver from DriverManager.", ex);
			LOG.debug("Must first load driver for \"{}\" because {}.", url, ex.getMessage());
			return loadDriverFor(url);
		}
	}

	@SuppressWarnings("unchecked")
	private static Driver loadDriverFor(final String jdbcURL) {
		String driverName = getKnownDriverFor(jdbcURL);
		try {
			if (driverName != null) {
				Class<? extends Driver> driverClass = (Class<? extends Driver>) Class.forName(driverName);
				LOG.debug("Driver {} for URL \"{}\" loaded.", driverName, jdbcURL);
				Driver driver = driverClass.getDeclaredConstructor().newInstance();
				DriverManager.registerDriver(driver);
				LOG.debug("Driver {} for URL \"{}\" registered.", driver, jdbcURL);
			}
			return DriverManager.getDriver(jdbcURL);
		} catch (SQLException ex) {
			throw new IllegalArgumentException("unregistered URL: \"" + jdbcURL + '"', ex);
		} catch (ClassNotFoundException ex) {
			throw new IllegalArgumentException("cannot load driver for \"" + jdbcURL + '"', ex);
		} catch (ReflectiveOperationException ex) {
			throw new IllegalArgumentException(
					"driver '" + driverName + "' for URL \"{}\" loaded but registration failed", ex);
		}
	}

	/**
	 * Retrieves whether the driver thinks that it can open a connection to the
	 * given URL. Accepted URLs are URLs beginning with:
	 * <ul>
	 * <li><i>jdbc:proxy:</i>...</li>
	 * <li><i>jdbc:jamon:</i>... (if JAMon is in the classpath)</li>
	 * </ul>
	 *
	 * @param url the JDBC URL
	 * @return true, if successful
	 * @see Driver#acceptsURL(String)
	 */
	@Override
	public boolean acceptsURL(final String url) {
		String prefix = url.toLowerCase();
		return StringUtils.startsWith(prefix, JDBC_URL_PREFIX);
	}

	/**
	 * Attempts to make a database connection to the given URL. The driver
	 * returns "null" if it realizes it is the wrong kind of driver to connect
	 * to the given URL. This will be common, as when the JDBC driver manager is
	 * asked to connect to a given URL it passes the URL to each loaded driver
	 * in turn.
	 *
	 * @param url the url
	 * @param info the info (e.g. user/password)
	 * @return the connection
	 * @throws SQLException
	 *             the sQL exception
	 * @see Driver#connect(String, Properties)
	 */
	@Override
	public Connection connect(final String url, final Properties info) throws SQLException {
		LOG.trace("Connecting to URL \"{}\"...", url);
		if (!acceptsURL(url)) {
			LOG.trace("{} does not accept \"{}\" as URL.", this, url);
			return null;
		}
		String realURL = getRealURL(url);
		Driver realDriver = getDriver(realURL);
		Connection connection = realDriver.connect(realURL, info);
		LOG.trace("Connected to real URL \"{}\".", realURL);
		return ProxyConnection.newInstance(connection);
	}

	/**
	 * Gets the major version.
	 *
	 * @return major version
	 */
	@Override
	public int getMajorVersion() {
		return 1;
	}

	/**
	 * Gets the minor version.
	 *
	 * @return the minor version
	 */
	@Override
	public int getMinorVersion() {
		return 4;
	}

	/**
	 * Gets the property info.
	 *
	 * @param url
	 *            the url
	 * @param info
	 *            the info
	 * @return the property info
	 * @throws SQLException
	 *             the SQL exception
	 */
	@Override
	public DriverPropertyInfo[] getPropertyInfo(final String url, final Properties info) throws SQLException {
		String realURL = getRealURL(url);
		Driver driver = getDriver(realURL);
		return driver.getPropertyInfo(realURL, info);
	}

	/**
	 * Jdbc compliant.
	 *
	 * @return true, if successful
	 */
	@Override
	public boolean jdbcCompliant() {
		return true;
	}

	/**
	 * Gets the parent logger. This method is needed for the support of Java 5.
	 *
	 * @return the parent logger
	 * @throws SQLFeatureNotSupportedException
	 *             the SQL feature not supported exception
	 */
	@Override
	public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
		throw new SQLFeatureNotSupportedException("not yet implemented");
	}

	/**
	 * Better toString implementation which supports logging and debugging.
	 *
	 * @return the string
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " " + this.getMajorVersion() + "." + this.getMinorVersion() + " for \""
				+ JDBC_URL_PREFIX + "...\"";
	}

}
