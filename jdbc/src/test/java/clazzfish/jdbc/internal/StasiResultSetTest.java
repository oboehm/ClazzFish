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
 * (c)reated 15.04.2014 by oliver (ob@oasd.de)
 */

package clazzfish.jdbc.internal;

import clazzfish.jdbc.AbstractDbTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link StasiResultSet} class.
 *
 * @author oliver
 */
public final class StasiResultSetTest extends AbstractDbTest {

    private static Logger LOG = LoggerFactory.getLogger(StasiResultSetTest.class);
    private ResultSet resultSet;

    /**
     * Returns an object for testing.
     *
     * @return the object
     */
    @Override
    protected ResultSet getObject() {
        try {
            return getResultSetFor("SELECT * FROM country");
        } catch (SQLException sex) {
            throw new UnsupportedOperationException("cannot provide object for testing", sex);
        }
    }

    /**
     * Test method for {@link StasiResultSet#getObject(int)} and other
     * getXxx(int) methods.
     *
     * @throws SQLException the SQL exception
     */
    @Test
    public void testGetValues() throws SQLException {
        resultSet = getResultSetFor("SELECT * FROM persons WHERE country = 'DE'");
        while (resultSet.next()) {
            int id = resultSet.getInt(1);
            assertTrue(id > 0, "not > 0: " + id);
            String name = resultSet.getString(2);
            Object obj = resultSet.getObject(2);
            assertEquals(name, obj);
            LOG.info("id = {}, name = \"{}\"", id, name);
        }
    }

    /**
     * Test method for {@link StasiResultSet#isFirst()} and
     * {@link StasiResultSet#getWrappedResultSet()}.
     *
     * @throws SQLException the SQL exception
     */
    @Test
    public void testIsFirst() throws SQLException {
        resultSet = getResultSetFor("SELECT * FROM persons WHERE id = 1001");
        StasiResultSet srs = (StasiResultSet) resultSet;
        ResultSet wrapped = srs.getWrappedResultSet();
        assertTrue(srs.isBeforeFirst(), "should be before first");
        assertTrue(wrapped.isBeforeFirst(), "should be before first");
        assertTrue(srs.next(), "result expected");
        assertTrue(srs.isFirst(), "should be first");
        assertTrue(srs.isLast(), "should be last");
        assertFalse(srs.next(), "no result expected");
        assertTrue(srs.isAfterLast(), "should be after last");
    }

    /**
     * Test method for {@link Object#toString()}.
     *
     * @throws SQLException the SQL exception
     */
    @Test
    public void testToStringWithSQL() throws SQLException {
        resultSet = getResultSetFor("SELECT * FROM country");
        String s = resultSet.toString();
        assertFalse(s.contains("@"), "looks like default implementation: " + s);
        LOG.info("s = \"{}\"", s);
    }

    private ResultSet getResultSetFor(final String sql) throws SQLException {
        Statement statement = this.proxy.createStatement();
        statement.execute(sql);
        return statement.getResultSet();
    }

    /**
     * Close result set.
     *
     * @throws SQLException the SQL exception
     */
    @AfterEach
    public void closeResultSet() throws SQLException {
        if (this.resultSet != null) {
            this.resultSet.close();
        }
    }

}

