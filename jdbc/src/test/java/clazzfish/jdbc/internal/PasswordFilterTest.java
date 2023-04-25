/*
 * Copyright (c) 2023 by Oli B.
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
 * (c)reated 21.04.23 by oboehm
 */
package clazzfish.jdbc.internal;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Unit-Test fuer {@link PasswordFilter} ...
 *
 * @author oboehm
 * @since 21.04.23
 */
class PasswordFilterTest {

    private static final Logger log = LoggerFactory.getLogger(PasswordFilterTest.class);

    @Test
    void filterInsert() {
        String sql = "INSERT INTO users (password, name) VALUES ('secret', 'James')";
        checkFilter(sql);
    }

    @Test
    void filterUpdate() {
        String sql = "UPDATE users SET password = 'secret' WHERE id = 007";
        checkFilter(sql);
    }

    @Test
    void filterSelect() {
        String sql = "SELECT id FROM online l, kunden k WHERE l.id = k.id AND l.password = 'secret'";
        checkFilter(sql);
    }


    private static void checkFilter(String sql) {
        String filtered = PasswordFilter.filter(sql);
        assertNotEquals(sql, filtered);
        assertThat(filtered, not(containsString("secret")));
        log.info("filtered = \"{}\"", filtered);
    }
}