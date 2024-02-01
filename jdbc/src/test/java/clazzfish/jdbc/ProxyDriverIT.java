/*
 * Copyright (c) 2024 by Oli B.
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
 * (c)reated 31.01.24 by oboehm
 */
package clazzfish.jdbc;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The ProxyDriverIT is an integration test for the Postgres driver.
 *
 * @author oboehm
 * @since 2.2 (31.01.24)
 */
public class ProxyDriverIT {

    private static final Logger log = LoggerFactory.getLogger(ProxyDriverIT.class);
    private static final ProxyDriver driver = new ProxyDriver();
    private static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15-alpine").waitingFor(Wait.defaultWaitStrategy());

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @Test
    public void testHsqldbProxy() throws SQLException {
        testSQL("jdbc:proxy:hsqldb:mem:testdb", "testuser", "secret");
    }

    @Test
    public void testPostgresProxy() throws SQLException {
        String jdbcURL = postgres.getJdbcUrl();
        jdbcURL = "jdbc:proxy:" + StringUtils.substringAfter(jdbcURL, "jdbc:");
        assertEquals(postgres.getJdbcUrl(), ProxyDriver.getRealURL(jdbcURL));
        testSQL(jdbcURL, postgres.getUsername(), postgres.getPassword());
    }

    public void testSQL(String jdbcURL, String user, String passwd) throws SQLException {
        driver.acceptsURL(jdbcURL);
        try (Connection conn = DriverManager.getConnection(jdbcURL, user, passwd);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("create table PERSONS (ID int, NAME varchar(255), CITY varchar(255))");
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

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

}
