/*
 * Copyright (c) 2018-2021 by Oliver Boehm
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
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * JUnit tests for {@link ClasspathAgent} class. Some tests requires that you
 * start this class as java agent. This is done using the helper class
 * {@link ClasspathAgentLoader}. This is also the reason which this class has
 * the suffix 'IT' (for integration test).
 * <p>
 * If you want to start the jar file manually go the command line and start
 * the following command: <tt>mvn -DskipTests package</tt>.
 * </p><p>
 * Next you should add the following vmargs in the launch configuration:
 * <pre>
 * -javaagent:target/clazzfish-agent-x.x-SNAPSHOT.jar
 * </pre>
 * </p>
 * @author oboehm
 */
public class ClasspathAgentIT {

    private static final Logger LOG = Logger.getLogger(ClasspathAgentIT.class.getName());
    private static final ClasspathAgent agent = ClasspathAgent.getInstance();
    private static final MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
    private static ObjectName mbean;

    /**
     * Sets up the MBean.
     *
     * @throws MalformedObjectNameException the malformed object name exception
     * @throws IOException                  the io exception
     */
    @BeforeAll
    public static void setUpObjectName() throws MalformedObjectNameException, IOException {
        mbean = new ObjectName(ClasspathAgent.MBEAN_NAME);
        try {
            if (ClasspathAgent.getInstrumentation() == null) {
                loadJavaAgentFrom(Paths.get("target"));
            }
        } catch (IllegalStateException ex) {
            LOG.log(Level.WARNING, "ClasspathAgent is not available:", ex);
        }
    }
    
    private static void loadJavaAgentFrom(Path dir) throws IOException {
        assertTrue(Files.isDirectory(dir));
        if (Files.list(dir).noneMatch(f -> f.toString().endsWith(".jar"))) {
            ClasspathAgentLoader.createJar();
        }
        Optional<Path> jarFile =
                Files.list(dir).filter(f -> f.toString().endsWith(".jar")).filter(Files::isRegularFile).findFirst();
        jarFile.ifPresent(ClasspathAgentLoader::loadAgent);
    }

    /**
     * Test method for {@link ClasspathAgent#getInstrumentation()}.
     */
    @Test
    public void testGetInstrumentation() {
        if (agent.isActive()) {
            Instrumentation instrumentation = ClasspathAgent.getInstrumentation();
            assertTrue(instrumentation.getAllLoadedClasses().length > 0, "no classes loaded?");
            LOG.info("loaded classes: " + instrumentation.getAllLoadedClasses().length);
        } else {
            LOG.info("you must start " + ClasspathAgent.class + " as 'javaagent'");
        }
    }

    /**
     * Test method for {@link ClasspathAgent#getLoadedClasses()}.
     */
    @Test
    public void testGetLoadedClasses() {
        assumeTrue(agent.isActive());
        Class<?>[] loadedClasses = agent.getLoadedClasses();
        assertTrue(loadedClasses.length > 0, "no classes loaded?");
    }

    /**
     * Test method for {@link ClasspathAgent#getLoadedClassnames()}. The
     * received classnames should be sorted and there should be now duplicates
     * inside.
     */
    @Test
    public void testGetLoadedClassnames() {
        assumeTrue(agent.isActive());
        String[] classnames = agent.getLoadedClassnames();
        assertTrue(classnames.length > 1, "not enough classes loaded?");
        for (int i = 1; i < classnames.length; i++) {
            assertNotEquals(classnames[i - 1], classnames[i], "doublet at " + i + ". element:");
            assertTrue(classnames[i - 1].compareTo(classnames[i]) < 0, "unsorted at " + i + ". element:");
        }
    }

    /**
     * Test method for {@link ClasspathAgent#getLoadedClasses(ClassLoader)}.
     */
    @Test
    public void testGetLoadedClassesFromClasslaoder() {
        assumeTrue(agent.isActive());
        Class<?>[] loadedClasses = agent.getLoadedClasses(this.getClass().getClassLoader());
        assertTrue(loadedClasses.length > 0, "no classes loaded?");
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
        assumeTrue(agent.isActive());
        Object attribute = mbeanServer.getAttribute(mbean, "LoadedClassnames");
        assertNotNull(attribute);
    }

    /**
     * Here we test if we can access an operation of the registered MBean.
     *
     * @throws JMException the jM exception
     */
    @Test
    public void testMBeanOperation() throws JMException {
        assumeTrue(agent.isActive());
        Object result = mbeanServer.invoke(mbean, "getLoadedClasses", new Object[] { this
                .getClass().getClassLoader() }, new String[] { ClassLoader.class.getName() });
        assertNotNull(result);
    }

    /**
     * Test method for {@link ClasspathAgent#logLoadedClasses()}. Watch the
     * LOG and check if it is ok.
     */
    @Test
    public void testLogLoadedClasses() {
        assumeTrue(agent.isActive());
        agent.logLoadedClasses();
    }

    /**
     * Test method for {@link ClasspathAgent#dumpLoadedClasses()}. Watch the
     * LOG to see the filename.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testDumpLoadedClasses() throws IOException {
        assumeTrue(agent.isActive());
        agent.dumpLoadedClasses();
    }

}
