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
 * (c)reated 17.11.25 by oboehm
 */
package clazzfish.monitor.jmx;

import clazzfish.agent.ClasspathAgent;
import clazzfish.agent.ClasspathAgentMBean;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.JMException;
import javax.management.RuntimeMBeanException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link AgentFinder}.
 *
 * @author oboehm
 * @since 17.11.25
 */
class AgentFinderTest {

    private static final Logger log = LoggerFactory.getLogger(AgentFinderTest.class);
    private static final ClasspathAgentMBean agent = ClasspathAgent.getInstance();
    private final AgentFinder agentFinder = new AgentFinder();

    @Test
    void isAgentAvailable() {
        assertTrue(agentFinder.isAgentAvailable());
    }

    @Test
    void isDumping() {
        assertEquals(agent.isDumping(), agentFinder.isDumping());
    }

    @Test
    void getDumpURI() throws JMException {
        assertNotNull(agentFinder.getDumpURI());
    }

    @Test
    void getLoadedClasses() throws JMException {
        try {
            assertNotNull(agentFinder.getLoadedClasses());
        } catch (RuntimeMBeanException mayhappen) {
            log.info("Loaded classes are not available by {}:", agentFinder, mayhappen);
        }
    }

}