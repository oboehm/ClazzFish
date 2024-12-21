/*
 * Copyright (c) 2018 by Oliver Boehm
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * (c)reated 16.11.2018 by oboehm (ob@oasd.de)
 */
package clazzfish.jdbc;

import org.junit.jupiter.api.Test;

import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.TabularData;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link ConnectionMonitor}.
 */
class ConnectionMonitorTest extends AbstractDbTest {
    
    private static final ConnectionMonitor monitor = ConnectionMonitor.getInstance();

    /**
     * Returns an object for testing.
     *
     * @return the test object
     */
    @Override
    protected ConnectionMonitor getObject() {
        return monitor;
    }

    /**
     * Test method for {@link ConnectionMonitor#getOpenConnections()}.
     */
    @Test
    public void testGetOpenConnections() {
        assertEquals(1, monitor.getOpenConnections());
    }

    /**
     * Test method for {@link ConnectionMonitor#getLastCallerStacktrace()}.
     */
    @Test
    public void testGetLastCallerStacktrace() {
        StackTraceElement[] stacktrace = monitor.getLastCallerStacktrace();
        assertThat(stacktrace, not(emptyArray()));
    }

    /**
     * Test method for {@link ConnectionMonitor#getCallerStacktraces()}.
     *
     * @throws OpenDataException the open data exception
     */
    @Test
    public void testGetCallerStacktraces() throws OpenDataException {
        TabularData stacktraces = monitor.getCallerStacktraces();
        assertEquals(1, stacktraces.size());
    }

    /**
     * Test method for {@link ConnectionMonitor#getClosedConnections()} and for
     * {@link ConnectionMonitor#getSumOfConnections()}.
     */
    @Test
    public void testGetClosedConnections() {
        int expected = monitor.getSumOfConnections() - monitor.getOpenConnections();
        int closedConnections = monitor.getClosedConnections();
        assertEquals(expected, closedConnections);
        assertThat(closedConnections, greaterThanOrEqualTo(0));
    }

    /**
     * Test method for {@link ConnectionMonitor#assertConnectionsClosed()}.
     */
    @Test
    public void testAssertConnectionsClosed() {
        assertThrows(AssertionError.class, ConnectionMonitor::assertConnectionsClosed);
    }

    /**
     * Test method for {@link ConnectionMonitor#getCallerOf(Connection)}.
     */
    @Test
    public void testGetCallerOf() {
        StackTraceElement caller = ConnectionMonitor.getCallerOf(connection);
        assertEquals(caller.getMethodName(), "setUpConnection", "wrong caller: " + caller);
    }

    /**
     * Test method for {@link ConnectionMonitor#getCallerOf(Connection)}. We
     * do not want to see the {@link ConnectionMonitor} as caller!
     *
     * @throws SQLException the SQL exception
     */
    @Test
    public void testGetCallerOfMonitoredConnection() throws SQLException {
        Connection con = DriverManager.getConnection("jdbc:hsqldb:mem:testdb");
        try (Connection proxyCon = ConnectionMonitor.getMonitoredConnection(con)) {
            StackTraceElement callerOf = ConnectionMonitor.getCallerOf(con);
            assertEquals(this.getClass().getName(), callerOf.getClassName());
        }
    }

    /**
     * Test method for {@link ConnectionMonitor#getCallers()}.
     */
    @Test
    public void testGetCallers() {
        StackTraceElement[] callers = monitor.getCallers();
        assertEquals(1, callers.length);
    }

    /**
     * Test method for {@link ConnectionMonitor#logCallerStacktraces()}. Watch
     * the log to see if it works.
     */
    @Test
    public void testLogStacktraces() {
        monitor.logCallerStacktraces();
    }

    /**
     * Watch the log to see if the {@link ConnectionMonitor#run()} produces
     * the expected output.
     */
    @Test
    public void testRun() {
        monitor.run();
    }

    @Test
    public void testDumpMe() throws IOException {
        monitor.dumpMe();
    }

}
