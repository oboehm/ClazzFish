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
 * (c)reated 05.04.2014 by oliver (ob@oasd.de)
 */

package clazzfish.jdbc.internal;

import clazzfish.jdbc.AbstractDbTest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link StasiPreparedStatement} class.
 *
 * @author oliver
 */
public class StasiPreparedStatementTest extends AbstractDbTest {

    private static final Logger LOG = LoggerFactory.getLogger(StasiPreparedStatementTest.class);

    /**
     * Returns an object for testing.
     *
     * @return the object
     */
    @Override
    protected PreparedStatement getObject() {
        try {
            return this.proxy.prepareStatement("SELECT * FROM country");
        } catch (SQLException sex) {
            throw new UnsupportedOperationException("cannot provide object for testing", sex);
        }
    }

    /**
     * Test method for {@link StasiPreparedStatement#execute(String)}.
     *
     * @throws SQLException the sQL exception
     */
    @Test
    public void testExecuteUpdate() throws SQLException {
        try (PreparedStatement stmt = this.proxy
                .prepareStatement("INSERT INTO country (lang, name, callingcode) " + "VALUES(?, ?, ?)")) {
            assertEquals(StasiPreparedStatement.class, stmt.getClass());
            setCountryRow(stmt, "de", "Germany", 49);
            int ret = stmt.executeUpdate();
            assertEquals(1, ret);
        }
    }

    /**
     * Test method for {@link StasiPreparedStatement#execute(String)}.
     * If the exceution fails we expect the wrong SQL as part of the
     * {@link SQLException}.
     */
    @Test
    public void testExecuteFailing() {
        try (PreparedStatement stmt = this.proxy
                .prepareStatement("INSERT INTO country (lang, name, callingcode) " + "VALUES(?, ?, ?)")) {
            stmt.setInt(1, 44);
            stmt.executeUpdate();
            fail("SQLException expected");
        } catch (SQLException expected) {
            LOG.debug("SQLException expected", expected);
            String msg = expected.getMessage();
            assertThat(msg, containsString("INSERT INTO country"));
        }
    }

    @Test
    public void testAddBatch() throws SQLException {
        try (PreparedStatement stmt = this.proxy
                .prepareStatement("INSERT INTO country (lang, name, callingcode) " + "VALUES(?, ?, ?)")) {
            stmt.clearWarnings();
            stmt.clearParameters();
            stmt.clearBatch();
            setCountryRow(stmt, "ch", "Suiss", 41);
            stmt.addBatch();
            setCountryRow(stmt, "fr", "France", 33);
            stmt.addBatch();
            int[] ret = stmt.executeBatch();
            assertEquals(1, ret[0]);
            assertEquals(1, ret[1]);
        }
    }

    private void setCountryRow(PreparedStatement stmt, String lang, String country, int callingcode) throws SQLException {
        stmt.setString(1, lang);
        stmt.setString(2, country);
        stmt.setInt(3, callingcode);
    }

    /**
     * Test method for {@link StasiPreparedStatement#executeQuery()} and
     * {@link StasiPreparedStatement#getWrappedPreparedStatement()}.
     *
     * @throws SQLException the SQL exception
     */
    @Test
    public void testExcecuteQuery() throws SQLException {
        try (StasiPreparedStatement stmt = (StasiPreparedStatement) this.proxy
                .prepareStatement("SELECT * FROM persons WHERE id = ?")) {
            checkExcecuteQuery(stmt);
            checkExcecuteQuery(stmt.getWrappedPreparedStatement());
        }
    }

    private static void checkExcecuteQuery(PreparedStatement stmt) throws SQLException {
        stmt.setInt(1, 1001);
        try (ResultSet query = stmt.executeQuery()) {
            assertTrue(query.next(), "result expected");
            assertEquals(1001, query.getInt("id"));
        }
    }
    
    @Test
    public void testExcecute() throws SQLException {
        try (PreparedStatement stmt = this.proxy.prepareStatement("SELECT * FROM persons")) {
            assertTrue(stmt.execute());
            assertNotNull(stmt.getResultSet());
            assertEquals(stmt.getUpdateCount(), stmt.getLargeUpdateCount());
        }
    }

    /**
     * The toString implementation should contain the real SQL.
     *
     * @throws SQLException the sQL exception
     */
    @Test
    public void testToStringWithSQL() throws SQLException {
        try (PreparedStatement stmt = this.proxy
                .prepareStatement("SELECT * FROM country WHERE lang = ? OR callingcode = ?")) {
            stmt.setString(1, "de");
            stmt.setInt(2, 42);
            String sql = stmt.toString();
            assertEquals("SELECT * FROM country WHERE lang = 'de' OR callingcode = 42", sql);
            LOG.info("sql = \"{}\"", sql);
        }
    }
    
    @Test
    public void testGetMetaData() throws SQLException {
        try (PreparedStatement stmt = this.proxy.prepareStatement("SELECT * FROM persons WHERE id = ?")) {
            assertNotNull(stmt.getMetaData());
            assertNotNull(stmt.getParameterMetaData());
        }
    }
    
    @Test
    public void testArray() throws SQLException {
        try (PreparedStatement stmt = this.proxy.prepareStatement("CREATE TABLE arraytest(a INTEGER ARRAY)")) {
            stmt.executeUpdate();
        }
        try (PreparedStatement stmt = this.proxy.prepareStatement("INSERT INTO arraytest VALUES ?")) {
            Object[] objects = new Object[] { 1, 2, 3 };
            Array array = connection.createArrayOf("INTEGER", objects);
            stmt.setArray(1, array);
            stmt.execute();
        }
        try (PreparedStatement stmt = this.proxy.prepareStatement("SELECT  * FROM arraytest");
                ResultSet rs = stmt.executeQuery()) {
            assertTrue(rs.next());
            Array a = rs.getArray(1);
            assertNotNull(a);
        }
    }
    
    @Test
    public void testSetAsciiStream() throws SQLException, IOException {
        try (InputStream asciiStream = new ByteArrayInputStream("DEATCH".getBytes(StandardCharsets.US_ASCII));
                PreparedStatement stmt = this.proxy
                        .prepareStatement("UPDATE persons SET country = ? WHERE country = 'DE'")) {
            stmt.setAsciiStream(1, asciiStream, 2);
            stmt.setAsciiStream(1, asciiStream, 2L);
            stmt.setAsciiStream(1, asciiStream);
            int ret = stmt.executeUpdate();
            assertEquals(1, ret);
        }
    }

}

