/*
 * Copyright (c) 2026 by Oli B.
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
 * (c)reated 15.06.26 by oboehm
 */
package clazzfish.jdbc.internal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Caller}.
 *
 * @author oboehm
 * @since 15.06.26
 */
class CallerTest {

    @Test
    void getStackTraceElement() {
        Caller caller = Caller.of();
        StackTraceElement element = caller.getStackTraceElement();
        assertEquals("getStackTraceElement", element.getMethodName());
    }

}