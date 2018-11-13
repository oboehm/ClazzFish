package clazzfish.monitor.util;
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
 * (c)reated 13.11.2018 by oboehm (ob@oasd.de)
 */

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link StackTraceScanner}.
 */
class StackTraceScannerTest {

    /**
     * Test method for {@link StackTraceScanner#getCallerClass()}.
     */
    @Test
    public void testGetCallerClass() {
        checkGetCallerClass(this.getClass());
    }

    private static void checkGetCallerClass(final Class<?> expected) {
        assertEquals(expected, StackTraceScanner.getCallerClass());
    }

    /**
     * Test method for {@link StackTraceScanner#getCallerStackTrace()}. As
     * result we expect the stacktrace of this method here.
     */
    @Test
    public void testGetCallerStacktrace() {
        StackTraceElement[] stacktrace = StackTraceScanner.getCallerStackTrace();
        Class<?> callerClass = StackTraceScanner.getCallerClass();
        assertEquals(callerClass.getName(), stacktrace[0].getClassName());
    }

}
