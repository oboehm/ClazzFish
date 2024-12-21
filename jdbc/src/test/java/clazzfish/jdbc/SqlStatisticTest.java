/*
 * $Id: SqlStatisticTest.java,v 1.13 2016/12/18 20:19:39 oboehm Exp $
 *
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
 * (c)reated 16.04.2014 by oliver (ob@oasd.de)
 */

package clazzfish.jdbc;

import clazzfish.jdbc.monitor.ProfileMonitor;
import clazzfish.monitor.jmx.MBeanHelper;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.openmbean.TabularData;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link SqlStatistic} class.
 *
 * @author oliver
 * @since 1.4.2 (16.04.2014)
 */
public class SqlStatisticTest {

    private static final Logger log = LoggerFactory.getLogger(SqlStatisticTest.class);
    private static final String[] sqls = {
        "SELECT a FROM b",
        "SELECT a FROM c",
        "SELECT a FROM d",
        "SELECT a FROM e",
        "SELECT a FROM f"
    };
    private final SqlStatistic instance = SqlStatistic.getInstance();

    /**
     * Prepare the statistics. Each monitor is started and stopped to have
     * a minimal statistic.
     */
    @BeforeEach
    public void prepareStatistic() {
        ProfileMonitor[] monitors = new ProfileMonitor[sqls.length];
        int[] hits = new int[sqls.length];
        for (int i = 0; i < sqls.length; i++) {
            monitors[i] = instance.startProfileMonitorFor(sqls[i]);
            hits[i] = monitors[i].getHits();
        }
        int n = instance.getMonitors().length;
        for (int i = 0; i < sqls.length; i++) {
            monitors[i].stop();
            log.info("monitor[{}] = {}", i, monitors[i]);
            if (n < instance.getMaxSize()) {
                assertEquals(hits[i] + 1, monitors[i].getHits());
            }
            assertTrue(monitors[i].getLastValue() >= 0.0);
        }
    }

    /**
     * Test method for {@link SqlStatistic#getInstance()}.
     */
    @Test
    public void testGetInstance() {
        assertNotNull(SqlStatistic.getInstance());
    }

    /**
     * The SQL statements are not needed after a reset. Unfortunately with 
     * JAMon 2.81 an exception entry is added to the list of monitors - this
     * is accepted in this test.
     */
    @Test
    public final void testReset() {
        instance.reset();
        TabularData statistics = instance.getStatistics();
        assertThat(statistics.size(), lessThan(2));
    }

    /**
     * The name of the class should be part of the toString implementation.
     */
    @Test
    public void testToString() {
        String s = instance.toString();
        assertThat(s, containsString("SqlStatistic"));
        log.info("s = \"{}\"", s);
    }

    /**
     * Test method for {@link SqlStatistic#start()}.
     */
    @Test
    public void testStart() {
        ProfileMonitor mon = SqlStatistic.start("DROP table dummy");
        mon.stop();
        log.info("SQL statistic: {}", mon);
    }

    /**
     * The label should be trimmed. This is tested here.
     */
    @Test
    public void testGetLabel() {
        ProfileMonitor mon = SqlStatistic.start("   DROP table dummy   ");
        mon.stop();
        assertEquals("DROP table dummy", mon.getLabel());
    }

    /**
     * Test method for {@link SqlStatistic#getMonitor(String)}.
     */
	@Test
    public void testGetProfileMonitor() {
        String sql = sqls[0];
        ProfileMonitor started = SqlStatistic.start(sql);
        started.stop();
        ProfileMonitor mon = instance.getMonitor(sql);
        assertNotNull(mon);
        assertEquals(started.getLabel(), mon.getLabel());
        assertThat(mon.getHits(), greaterThan(0));
        log.info("mon = {}", mon);
    }

    /**
     * Test register as shutdown hook.
     */
    @Test
    public void testRegisterAsShutdownHook() {
        SqlStatistic.addAsShutdownHook();
    }

    /**
     * Because the statistic was set up by this test there should be some
     * statistics available.
     */
    @Test
    public void testInstance() {
        ProfileMonitor mon = instance.getMonitor(sqls[0]);
        assertThat(mon.getHits(), greaterThan(0));
    }

    /**
     * Test method for {@link SqlStatistic#dumpMe(File)}. For the SQL
     * statistic it is important that the label is quoted because a semicolon
     * (";") could be part of a SQL statement. This is tested here.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testDumpStatisticToFile() throws IOException {
        File dumpFile = new File("target", "sql-stat.csv");
        instance.dumpMe(dumpFile);
        List<String> lines = FileUtils.readLines(dumpFile, StandardCharsets.UTF_8);
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            assertThat(line, startsWith("\""));
        }
    }

    @Test
    public void testDumpMe() throws IOException {
        File file = instance.dumpMe();
        assertTrue(file.isFile());
    }

    /**
     * Test method for {@link SqlStatistic#registerAsMBean(String)}.
     */
	@Test
    public void testRegisterAsMBean() {
        String mbeanName = "test.mon.SqlStat";
        SqlStatistic.registerAsMBean(mbeanName);
        assertTrue(MBeanHelper.isRegistered(mbeanName), "not registered: " + mbeanName);
    }

    /**
     * Unit test for issue #14.
     *
     * @throws SQLException in case of error
     * @throws IOException in case of error
     */
    @Test
    public void testLog() throws SQLException, IOException {
        ProxyDriver.register();
        try (Connection connection = DriverManager.getConnection("jdbc:proxy:hsqldb:mem:testdb")) {
            assertNotNull(connection);
            executeUpdate("CREATE TABLE users (name VARCHAR(50), password VARCHAR(16))", connection);
            executeUpdate("INSERT INTO users (name, password) VALUES ('James', 'secret')", connection);
            executeUpdate("UPDATE users SET password = 'topsecret' WHERE name = 'James'", connection);
        }
        File logfile = new File("target", "sql.log");   // see log4j2.xml
        String sqls = FileUtils.readFileToString(logfile, StandardCharsets.UTF_8);
        assertThat("see " + logfile, sqls, not(containsString("secret")));
    }

    private static void executeUpdate(final String sql, final Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

}

