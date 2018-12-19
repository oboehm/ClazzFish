/*
 * Copyright (c) 2012 by Oliver Boehm
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
 * (c)reated 07.10.2012 by oliver (ob@oasd.de)
 */

package clazzfish.jdbc;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

/**
 * Common super class for JUnit tests which need a database for testing.
 *
 * @author oliver (ob@aosd.de)
 * @version $Revision: 1.4 $
 * @since 1.3 (07.10.2012)
 */
public abstract class AbstractDbTest {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractDbTest.class);

    /** The JDBC URL for testing. */
    protected static String JDBC_URL = System.getProperty("jdbcURL", "jdbc:hsqldb:mem:testdb");

    /** The connection for testing. */
    protected Connection connection;

    /** The proxy for testing. */
    protected Connection proxy;

    /**
     * Requires an object for testing.
     *
     * @return the object
     */
    protected abstract Object getObject();

    /**
     * We set up a small database with one table to have something for
     * testing. The following tables will be created:
     * <pre>
     * PERSONS:
     * +-------------------------------------+
     * | ID   | NAME   | CITY      | COUNTRY |
     * |------+--------+-----------+---------|
     * | 1001 | Oli B. | Stuttgart | DE      |
     * +-------------------------------------+
     *
     * COUNTRY:
     * +---------------------------+-------+----------+
     * | LANG | NAME | CALLINGCODE | IMAGE | CONTRACT |
     * +---------------------------+-------+----------+
     * </pre>
     *
     * @throws ClassNotFoundException the class not found exception
     */
    @BeforeAll
    public static void setUpDB() throws ClassNotFoundException {
        if (JDBC_URL.startsWith("jdbc:jamon")) {
            Class.forName("com.jamonapi.proxy.JAMonDriver");
            LOG.debug("JDBC driver for JAMon loaded.");
        }
        try (Connection con = DriverManager.getConnection(JDBC_URL); Statement stmt = con.createStatement()) {
            stmt.executeUpdate(
                    "CREATE TABLE country (lang CHAR(2), name VARCHAR(50), callingcode SMALLINT, image BLOB, contract CLOB)");
            stmt.executeUpdate(
                    "create table persons(ID decimal(5), NAME varchar(50), CITY varchar(50), COUNTRY char(2))");
            stmt.executeUpdate(
                    "insert into persons (ID, NAME, CITY, COUNTRY) VALUES (1001, 'Oli B.', 'Stuttgart', 'DE')");
            LOG.info("Table 'persons' and 'country' created for testing.");
        } catch (SQLException ex) {
            LOG.info("Table 'persons' and 'country' not created because {}.", ex.getMessage());
            LOG.trace("Table creation failed.", ex);
        }
    }

    /**
     * Sets up the connection.
     *
     * @throws SQLException the sQL exception
     */
    @BeforeEach
    public void setUpConnection() throws SQLException {
        connection = DriverManager.getConnection(JDBC_URL);
        LOG.info("Got {} for URL \"{}\".", connection, JDBC_URL);
        proxy = ProxyConnection.newInstance(connection);
    }

    /**
     * Test method for the toString() implementation. We don't want the
     * default implemention of the {@link Object} class.
     */
    @Test
    public void testToString() {
        String s = this.getObject().toString();
        assertThat("looks like default implementation", s, not(containsString("@")));
        LOG.info("s = \"{}\"", s);
    }

    /**
     * Close the connection which was opened in the setup method.
     *
     * @throws SQLException the sQL exception
     */
    @AfterEach
    public void closeConnection() throws SQLException {
        proxy.close();
    }

}

