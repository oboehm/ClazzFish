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
 * (c)reated 25.11.24 by oboehm
 */
package clazzfish.monitor.rec;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Unit tests for {@link ClazzRecorder}.
 *
 * @author oboehm
 * @since 25.11.24
 */
class ClazzRecorderTest {

    private final ClazzRecorder recorder = new ClazzRecorder();

    @Test
    void collectClasses() {
        Set<PathRecord> classes = recorder.collectClasses();
        assertFalse(classes.isEmpty());
    }

}