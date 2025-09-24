/*
 * Copyright (c) 2025 by Oli B.
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
 * (c)reated 24.09.25 by oboehm
 */
package clazzfish.core;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link SystemInfo}.
 *
 * @author oboehm
 * @since 24.09.25
 */
class SystemInfoTest {

    @Test
    void getBootClasspath() {
        assertNotNull(SystemInfo.getBootClasspath());
    }

    @Test
    void getClasspath() {
        String[] classpath = SystemInfo.getClasspath();
        assertThat(classpath.length, greaterThan(0));
        assertTrue(new File(classpath[0]).exists());
    }

}