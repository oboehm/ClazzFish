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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express orimplied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * (c)reated 16.03.18 by oliver (ob@oasd.de)
 */
package clazzfish.agent;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.management.*;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit tests for {@link ClasspathAgent} class. Some tests requires that you
 * start this class as java agent. To do this go the command line and start the
 * following command <tt>mvn -DskipTests package</tt>.
 * <p>
 * Next you shoud add the following vmargs in the launch configuration:
 * </p>
 * <pre>
 * -javaagent:target/clazzfish-agent-x.x-SNAPSHOT.jar
 * </pre>
 *
 * @author oboehm
 */
class ClasspathAgentTest {

    private static final Logger LOG = Logger.getLogger(ClasspathAgentTest.class.getName());
    private static final ClasspathAgent agent = ClasspathAgent.getInstance();
    private static final MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
    private static ObjectName mbean;

    /**
     * Sets the up MBean.
     *
     * @throws MalformedObjectNameException the malformed object name exception
     * @throws IOException                  the io exception
     */
    @BeforeAll
    public static void setUpObjectName() throws MalformedObjectNameException, IOException {
        mbean = new ObjectName(ClasspathAgent.MBEAN_NAME);
        if (ClasspathAgent.getInstrumentation() == null) {
            loadJavaAgentFrom(Paths.get("target"));
        }
    }
    
    private static void loadJavaAgentFrom(Path dir) throws IOException {
        assertTrue(Files.isDirectory(dir));
        Optional<Path> jarFile =
                Files.list(dir).filter(f -> f.toString().endsWith(".jar")).filter(f -> Files.isRegularFile(f)).findFirst();
        jarFile.ifPresent(ClasspathAgentLoader::loadAgent);
    }

    /**
     * Test method for {@link ClasspathAgent#getInstrumentation()}.
     */
    @Test
    public void testGetInstrumentation() {
        Instrumentation instrumentation = ClasspathAgent.getInstrumentation();
        if (instrumentation == null) {
            LOG.info("you must start " + ClasspathAgent.class + " as 'javaagent'");
        } else {
            assertTrue(instrumentation.getAllLoadedClasses().length > 0, "no classes loaded?");
            LOG.info("loaded classes: " + instrumentation.getAllLoadedClasses().length);
        }
    }

    /**
     * Test method for {@link ClasspathAgent#getLoadedClasses()}.
     */
    @Test
    public void testGetLoadedClasses() {
        if (agent.isActive()) {
            Class<?>[] loadedClasses = agent.getLoadedClasses();
            assertTrue(loadedClasses.length > 0, "no classes loaded?");
        }
    }

    /**
     * Test method for {@link ClasspathAgent#getLoadedClassnames()}. The
     * received classnames should be sorted and there should be now duplicates
     * inside.
     */
    @Test
    public void testGetLoadedClassnames() {
        if (agent.isActive()) {
            String[] classnames = agent.getLoadedClassnames();
            assertTrue(classnames.length > 1, "not enough classes loaded?");
            for (int i = 1; i < classnames.length; i++) {
                assertNotEquals(classnames[i - 1], classnames[i], "doublet at " + i + ". element:");
                assertTrue(classnames[i - 1].compareTo(classnames[i]) < 0, "unsorted at " + i + ". element:");
            }
        }
    }

    /**
     * Test method for {@link ClasspathAgent#getLoadedClasses(ClassLoader)}.
     */
    @Test
    public void testGetLoadedClassesFromClasslaoder() {
        if (agent.isActive()) {
            Class<?>[] loadedClasses = agent.getLoadedClasses(this.getClass().getClassLoader());
            assertTrue(loadedClasses.length > 0, "no classes loaded?");
        }
    }

    /**
     * Here we test if the agent is registered as MBean.
     *
     * @throws InstanceNotFoundException the instance not found exception
     */
    @Test
    public void testMBeanRegistration() throws InstanceNotFoundException {
        ObjectInstance oi = mbeanServer.getObjectInstance(mbean);
        assertNotNull(oi, ClasspathAgent.MBEAN_NAME + " is not registered for JMX");
    }

    /**
     * Here we test if we can access an attribute of the registered MBean.
     *
     * @throws JMException the jM exception
     */
    @Test
    public void testMBeanAttribute() throws JMException {
        if (agent.isActive()) {
            Object attribute = mbeanServer.getAttribute(mbean, "LoadedClassnames");
            assertNotNull(attribute);
        }
    }

    /**
     * Here we test if we can access an operation of the registered MBean.
     *
     * @throws JMException the jM exception
     */
    @Test
    public void testMBeanOperation() throws JMException {
        if (agent.isActive()) {
            Object result = mbeanServer.invoke(mbean, "getLoadedClasses", new Object[] { this
                    .getClass().getClassLoader() }, new String[] { ClassLoader.class.getName() });
            assertNotNull(result);
        }
    }

    /**
     * Test method for {@link ClasspathAgent#logLoadedClasses()}. Watch the
     * LOG and check if it is ok.
     */
    @Test
    public void testLogLoadedClasses() {
        if (agent.isActive()) {
            agent.logLoadedClasses();
        }
    }

    /**
     * Test method for {@link ClasspathAgent#dumpLoadedClasses()}. Watch the
     * LOG to see the filename.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testDumpLoadedClasses() throws IOException {
        if (agent.isActive()) {
            agent.dumpLoadedClasses();
        }
    }

}
