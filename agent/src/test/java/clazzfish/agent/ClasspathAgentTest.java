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
 * (c)reated 16.10.18 by oliver (ob@oasd.de)
 */
package clazzfish.agent;

import org.junit.jupiter.api.Test;

import java.lang.instrument.Instrumentation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for {@link ClasspathAgent}.
 */
public final class ClasspathAgentTest {

    @Test
    public void testAgentmain() {
        Instrumentation instrumentation = mock(Instrumentation.class);
        ClasspathAgent.agentmain("test", instrumentation);
        assertEquals(instrumentation, ClasspathAgent.getInstrumentation());
    }

}