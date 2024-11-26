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
 * (c)reated 26.11.24 by oliver
 */
package clazzfish.monitor.rec;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;

class PathRecordTest {

    @Test
    void compareTo() {
        PathRecord a = new PathRecord(URI.create("file:a.jar"), "hello");
        PathRecord b = new PathRecord(URI.create("file:b.jar"), "world");
        assertThat(a.compareTo(b),  lessThan(0));
    }

}