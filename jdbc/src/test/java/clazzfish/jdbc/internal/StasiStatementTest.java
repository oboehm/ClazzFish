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
 * (c)reated 23.03.2014 by oliver (ob@oasd.de)
 */

package clazzfish.jdbc.internal;

import clazzfish.jdbc.AbstractDbTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link StasiStatement} class.
 *
 * @author oliver
 * @version $Revision: 1.10 $
 * @since 1.4.1 (23.03.2014)
 */
public final class StasiStatementTest extends AbstractDbTest {

    private static final Logger LOG = LoggerFactory.getLogger(StasiStatementTest.class);
    private StasiStatement statement;

    /**
     * Returns an object for testing.
     *
     * @return the object
     */
    @Override
    protected Statement getObject() {
        try {
            return this.proxy.createStatement();
        } catch (SQLException sex) {
            throw new UnsupportedOperationException("cannot provide object for testing", sex);
        }
    }

    /**
     * Sets up the statement for testing.
     *
     * @throws SQLException the SQL exception
     */
    @BeforeEach
    public void setUpStatement() throws SQLException {
        statement = (StasiStatement) this.proxy.createStatement();
    }

    /**
     * Test method for {@link StasiStatement#execute(String)}.
     *
     * @throws SQLException the sQL exception
     */
    @Test
    public void testExecute() throws SQLException {
        statement.execute("create table DUMMY(ID integer not null, NAME varchar(32))");
    }

    /**
     * If the execution of a SQL statement fails, we expected that SQL as part
     * of the exception message.
     */
    @Test
    public void testExecuteFailing() {
        String sql = "create something with wrong syntax";
        try {
            statement.execute(sql);
            fail("should fail: " + sql);
        } catch (SQLException expected) {
            LOG.debug("SQLException expected.", expected);
            String msg = expected.getMessage();
            assertThat(msg, containsString(sql));
        }
    }

    /**
     * Test method for {@link StasiStatement#executeQuery(String)}.
     *
     * @throws SQLException the SQL exception
     */
    @Test
    public void testExcecuteQuery() throws SQLException {
        try (StasiStatement stasiStmt = statement) {
            assertEquals(getIdWith(stasiStmt), getIdWith(stasiStmt.getStatement()));
        }
    }

    private static int getIdWith(Statement stmt) throws SQLException {
        String sql = "SELECT * FROM persons WHERE id = 1001";
        try (ResultSet query = stmt.executeQuery(sql)) {
            assertTrue(query.next(), "result expected");
            return query.getInt("id");
        }
    }

    /**
     * Test method for {@link StasiStatement#addBatch(String)}. This method was
     * introduced to see the LOG for a void method. Watch the LOG!
     *
     * @throws SQLException the sQL exception
     */
    @Test
    public void testAddBatch() throws SQLException {
        statement.addBatch("create table BATCH(NAME varchar(12))");
    }

    /**
     * Test method for {@link StasiStatement#isWrapperFor(Class)}.
     *
     * @throws SQLException the SQL exception
     */
    @Test
    public void testIsWrapper() throws SQLException {
        Statement wrappedStatement = statement.getStatement();
        assertTrue(statement.isWrapperFor(wrappedStatement.getClass()), "true expected for " + wrappedStatement);
    }

    /**
     * Test method for {@link StasiStatement#unwrap(Class)}.
     *
     * @throws SQLException the SQL exception
     */
    @Test
    public void testUnwrap() throws SQLException {
        Statement wrappedStatement = statement.getStatement();
        assertEquals(wrappedStatement, statement.unwrap(Statement.class));
    }

    /**
     * Close statement.
     *
     * @throws SQLException the sQL exception
     */
    @AfterEach
    public void closeStatement() throws SQLException {
        this.statement.close();
    }

}

