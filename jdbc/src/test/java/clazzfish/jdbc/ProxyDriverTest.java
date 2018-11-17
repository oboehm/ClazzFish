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
 * (c)reated 30.03.2014 by oliver (ob@oasd.de)
 */

package clazzfish.jdbc;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Enumeration;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ProxyDriver} class.
 *
 * @author oliver
 */
public final class ProxyDriverTest extends AbstractDbTest {

    private static final String JDBC_PROXY_HSQLDB = "jdbc:proxy:hsqldb:mem:testdb";
    private static final Logger log = LoggerFactory.getLogger(ProxyDriverTest.class);
    private static final ConnectionMonitorMBean connectionMonitor = ConnectionMonitor.getInstance();
    private static final ProxyDriver driver = new ProxyDriver();

    /**
     * Creates an object for testing.
     *
     * @return the object
     */
    @Override
    protected ProxyDriver getObject() {
        return new ProxyDriver();
    }

    /**
     * Test method for {@link ProxyDriver#acceptsURL(String)}.
     */
    @Test
    public void testAcceptsURL() {
        String url = JDBC_PROXY_HSQLDB;
        assertTrue(driver.acceptsURL(url), driver + " accepts not \"" + url + "\" as URL");
        log.info("{} accepts \"{}\" as URL.", driver, url);
    }

    /**
     * Test method for {@link ProxyDriver#connect(String, Properties)}.
     *
     * @throws SQLException the sQL exception
     */
    @Test
    public void testConnect() throws SQLException {
        try (Connection conn = driver.connect(JDBC_PROXY_HSQLDB, new Properties())) {
            checkCallerOf(conn, "testConnect");
        }
    }

    /**
     * Here we use the {@link DriverManager} to get a connection. As caller of
     * {@link ProxyDriver#connect(String, Properties)} we do not want to see the
     * {@link DriverManager} as caller but the caller of
     * {@link DriverManager#getConnection(String)}.
     *
     * @throws SQLException the sQL exception
     */
    @Test
    public void testGetConnection() throws SQLException {
        try (Connection conn = DriverManager.getConnection(JDBC_PROXY_HSQLDB)) {
            checkCallerOf(conn, "testGetConnection");
        }
    }

    private static void checkCallerOf(final Connection connection, final String methodName) {
        assertNotNull(connection);
        log.info("connection = {}", connection);
        StackTraceElement caller = connectionMonitor.getLastCaller();
        assertEquals(ProxyDriverTest.class.getName(), caller.getClassName());
        assertEquals(methodName, caller.getMethodName());
    }

    /**
     * Here we want to test if the driver itself was registered correct at
     * the {@link DriverManager}.
     *
     * @throws SQLException the sQL exception
     */
    @Test
    public void testDriverRegistration() throws SQLException {
        Connection conn = DriverManager.getConnection(JDBC_PROXY_HSQLDB);
        assertNotNull(conn);
        conn.close();
    }

    /**
     * Test method for {@link ProxyDriver#getPropertyInfo(String, Properties)}.
     *
     * @throws SQLException the sQL exception
     */
    @Test
    public void testGetPropertyInfo() throws SQLException {
        DriverPropertyInfo[] infos = driver.getPropertyInfo(JDBC_PROXY_HSQLDB, new Properties());
        assertNotNull(infos);
        assertThat("no property info received", infos.length, greaterThan(0));
        for (int i = 0; i < infos.length; i++) {
            log.info("info[{}] = \"{}\"", i, infos[i]);
        }
    }

    /**
     * Test method for {@link ProxyDriver#getMajorVersion()}.
     */
    @Test
    public void testGetMajorVersion() {
        int version = driver.getMajorVersion();
        assertThat("invalid major version: " + version, version, greaterThan(0));
    }

    /**
     * Test method for {@link ProxyDriver#getMinorVersion()}.
     */
    @Test
    public void testGetMinorVersion() {
        int version = driver.getMinorVersion();
        assertThat("invalid minor version: " + version, version, greaterThan(0));
    }

    /**
     * For the {@link ProxyDriver#toString()} we expect that the version info
     * is part of it.
     */
    @Override
    @Test
    public void testToString() {
        String version = driver.getMajorVersion() + "." + driver.getMinorVersion();
        String s = driver.toString();
        assertThat(s, containsString(version));
    }

    /**
     * Test method for {@link ProxyDriver#jdbcCompliant()}.
     */
    @Test
    public void testJdbcCompliant() {
        assertTrue(driver.jdbcCompliant(),driver + " is not JDBC compliant");
    }

    /**
     * Test method for {@link ProxyDriver#getRealURL(String)}.
     */
    @Test
    public void testGetRealURL() {
        String url = ProxyDriver.getRealURL("jdbc:proxy:hsqldb:file:/tmp/oli");
        assertEquals("jdbc:hsqldb:file:/tmp/oli", url);
    }

    /**
     * Test method for {@link ProxyDriver#getRealDriverName(String)}.
     */
    @Test
    public void testGetRealDriverName() {
        String driverName = ProxyDriver.getRealDriverName(JDBC_PROXY_HSQLDB);
        assertEquals("org.hsqldb.jdbc.JDBCDriver", driverName);
    }

    /**
     * Here we want to see if the exception handling works correct and we'll
     * got no {@link StackOverflowError}.
     */
    @Test
    public void testGetUnknownDriverName() {
        assertThrows(IllegalArgumentException.class, () -> ProxyDriver.getRealDriverName("jdbc:proxy:unknown"));
    }

    /**
     * Here we test if we can execute SQL statements with the
     * {@link ProxyDriver}.
     *
     * @throws SQLException the SQL exception
     */
    @Test
    public void testSQL() throws SQLException {
        try (Connection conn = DriverManager.getConnection(JDBC_PROXY_HSQLDB);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("insert into PERSONS (ID, NAME, CITY) VALUES (1, 'Dagobert Duck', 'Ducktales')");
            stmt.execute("select NAME, CITY from PERSONS where CITY = 'Ducktales'");
            try (ResultSet rs = stmt.getResultSet()) {
                while (rs.next()) {
                    assertEquals("Ducktales", rs.getString(2));
                    log.info("name = {}", rs.getString(1));
                }
            }
        }
    }

    /**
     * If autocommit is off and we forgot a commit after an update statement
     * we want to see a warning in the log. But only if there was not a
     * rollback before.
     *
     * @throws SQLException the SQL exception
     */
    @Test
    public void testCloseWithWarning() throws SQLException {
        try (Connection conn = DriverManager.getConnection(JDBC_PROXY_HSQLDB);
             Statement stmt = conn.createStatement()) {
            conn.setAutoCommit(false);
            int rc = stmt.executeUpdate("insert into PERSONS (ID, NAME, CITY) VALUES (2, 'Donald Duck', 'Ducktales')");
            assertEquals(1, rc);
        }
    }

    /**
     * Here we test if the ProxyDriver is the first registered driver. Why?
     * Because the implementation how the {@link DriverManager} finds the
     * corresponding driver for a given JDBC-URL is a little stupid - it tries
     * out all drivers if it can work with the URL. In case of problems (e.g.
     * if the network is down) it throws the first {@link SQLException} it got
     * and rethrow it - but this may be not the rease but the error message
     * that the protocoll "jdbc:proxy:..." can not be understood.
     * <p>
     * To avoid this irritating error message and got the thrown exception
     * from {@link ProxyDriver} it must be registeres as first driver.
     * </p>
     */
    @Test
    public void testRegistration() {
    	ProxyDriver.register();
    	Enumeration<Driver> drivers = DriverManager.getDrivers();
    	assertTrue(drivers.hasMoreElements(), "no driver registered");
    	Driver firstDriver = drivers.nextElement();
    	assertEquals(ProxyDriver.class, firstDriver.getClass(), "ProxyDriver is not the first driver");
    }

}

