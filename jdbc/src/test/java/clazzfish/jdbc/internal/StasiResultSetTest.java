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
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link StasiResultSet} class.
 *
 * @author oliver
 */
public final class StasiResultSetTest extends AbstractDbTest {

    private static Logger LOG = LoggerFactory.getLogger(StasiResultSetTest.class);
    private final ResultSet resultSet = mock(ResultSet.class);
    private final StasiResultSet stasiResultSet = new StasiResultSet(resultSet);

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
        try (ResultSet rs = getResultSetFor("SELECT * FROM persons WHERE country = 'DE'")) {
            while (rs.next()) {
                int id = rs.getInt(1);
                assertTrue(id > 0, "not > 0: " + id);
                String name = rs.getString(2);
                Object obj = rs.getObject(2);
                assertEquals(name, obj);
                LOG.info("id = {}, name = \"{}\"", id, name);
            }
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
        try (ResultSet rs = getResultSetFor("SELECT * FROM persons WHERE id = 1001")) {
            StasiResultSet srs = (StasiResultSet) rs;
            ResultSet wrapped = srs.getWrappedResultSet();
            assertTrue(srs.isBeforeFirst(), "should be before first");
            assertTrue(wrapped.isBeforeFirst(), "should be before first");
            assertTrue(srs.next(), "result expected");
            assertTrue(srs.isFirst(), "should be first");
            assertTrue(srs.isLast(), "should be last");
            assertFalse(srs.next(), "no result expected");
            assertTrue(srs.isAfterLast(), "should be after last");
        }
    }
    
    @Test
    public void testMoveCursor() throws SQLException {
        stasiResultSet.afterLast();
        stasiResultSet.beforeFirst();
        assertFalse(stasiResultSet.next());
        assertFalse(stasiResultSet.previous());
        assertFalse(stasiResultSet.absolute(-1));
        assertFalse(stasiResultSet.relative(1));
    }

    /**
     * Test method for {@link Object#toString()}.
     *
     * @throws SQLException the SQL exception
     */
    @Test
    public void testToStringWithSQL() throws SQLException {
        try (ResultSet rs = getResultSetFor("SELECT * FROM country")) {
            String s = rs.toString();
            assertFalse(s.contains("@"), "looks like default implementation: " + s);
            LOG.info("s = \"{}\"", s);
        }
    }

    private ResultSet getResultSetFor(final String sql) throws SQLException {
        Statement statement = this.proxy.createStatement();
        statement.execute(sql);
        return statement.getResultSet();
    }

    @Test
    public void testToStringWithRuntimeException() {
        checkToString(new IllegalStateException("bumm"));
    }

    @Test
    public void testToStringWithLinkageError() {
        checkToString(new LinkageError("bad error"));
    }

    private void checkToString(Throwable provocated) {
        when(resultSet.toString()).thenThrow(provocated);
        String s = stasiResultSet.toString();
        LOG.info("s = \"{}\"", s);
    }

    @Test
    public void testToStringWithSqlException() throws SQLException {
        when(resultSet.toString()).thenReturn(resultSet.getClass().getName() + "@12345");
        when(resultSet.isClosed()).thenThrow(new SQLException("very wrong SQL"));
        String s = stasiResultSet.toString();
        LOG.info("s = \"{}\"", s);
    }
    
    @Test
    public void testToStringOverriden() {
        String expected = "hello world";
        when(resultSet.toString()).thenReturn(expected);
        assertEquals(expected, stasiResultSet.toString());
    }

}

