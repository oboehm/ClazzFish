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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ClasspathAgent}.
 */
public final class ClasspathAgentTest {

    private final ClasspathAgent agent = ClasspathAgent.getInstance();
    private final Instrumentation instrumentation = mock(Instrumentation.class);

    @BeforeEach
    public void setUpMock() {
        Class[] classes = Arrays.asList(ClasspathAgentTest.class, Instrumentation.class).toArray(new Class[2]);
        when(instrumentation.getAllLoadedClasses()).thenReturn(classes);
        ClasspathAgent.agentmain("setUpMock", instrumentation);
    }

    @Test
    public void testAgentmain() {
        ClasspathAgent.agentmain("test", instrumentation);
        assertEquals(instrumentation, ClasspathAgent.getInstrumentation());
        assertEquals("test", agent.getArgs());
    }

    @Test
    public void testGetLoadedClassnames() {
        String[] loadedClassnames = agent.getLoadedClassnames();
        assertNotNull(loadedClassnames);
        assertThat(loadedClassnames, not(emptyArray()));
    }

    @Test
    public void testDumpLoadedClasses() throws IOException {
        File dump = new File("target", "dump.txt");
        agent.dumpLoadedClasses(dump.toString());
        assertTrue(dump.isFile());
    }

    /**
     * If the ClasspathAgent is not started as agent the
     * instrumentation attribute is null. In this case we
     * expected an Exeption as answer of the method
     * {@link ClasspathAgent#premain(String, Instrumentation)}.
     */
    @Test
    public void testGetInstrumentation() {
        ClasspathAgent.premain("", null);
        assertThrows(IllegalStateException.class, ClasspathAgent::getInstrumentation);
    }

    @Test
    public void testIsActive() {
        assertTrue(agent.isActive());
    }

    @Test
    public void testLogLoadedClasses() {
        agent.logLoadedClasses();
    }

}