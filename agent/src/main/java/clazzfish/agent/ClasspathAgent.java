/*
 * Copyright (c) 2012-2018 by Oliver Boehm
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
 * (c)reated 24.02.2012 by oliver (ob@oasd.de)
 */

package clazzfish.agent;

import javax.management.*;
import java.io.*;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * This is a simple Java agent to be able to watch the classpath. We need it
 * because some classloaders (e.g. the classloader of the IBM JDK) does not
 * provide the information which classes are loaded.
 * <p>
 * Originally this class was part of patterntesting-agent.
 * </p>
 *
 * @author oboehm
 * @since 0.8
 */
public class ClasspathAgent implements ClasspathAgentMBean {

    /** The Constant MBEAN_NAME for the registered name in JMX. */
    public static final String MBEAN_NAME = "clazzfish.agent:type=ClasspathAgent";

    private static final long serialVersionUID = 20180517L;
    private static final Logger LOG = Logger.getLogger(ClasspathAgent.class.getName());
    private static final ClasspathAgent INSTANCE = new ClasspathAgent();
    private static Instrumentation instrumentation;
    private static String args;

    private ClasspathAgent() {
        setUpLogging();
        try {
            this.registerAsMBean();
            LOG.info("ClasspathAgent is ready and registered as MBean \"" + MBEAN_NAME + "\".");
        } catch (MBeanRegistrationException | OperationsException ex) {
            LOG.info("ClasspathAgent is ready but not registered as MBean \"" + MBEAN_NAME
                    + "\" because of " + ex);
        }
    }

    /**
     * Gets the single instance of ClasspathAgent.
     *
     * @return single instance of ClasspathAgent
     */
    public static ClasspathAgent getInstance() {
        return INSTANCE;
    }

    /**
     * This method will be called if the class is loaded dynmically by
     * ClasspathAgentLoader.
     *
     * @param agentArgs the agent args
     * @param inst the inst
     */
    public static void agentmain(final String agentArgs, final Instrumentation inst){
        premain(agentArgs, inst);
    }

    /**
     * I guess this method will be called from the VM.
     *
     * @param agentArgs the agent args
     * @param inst the inst
     */
    public static void premain(final String agentArgs, final Instrumentation inst){
        instrumentation = inst;
        args = agentArgs;
    }

    /**
     * Gets the instrumentation.
     *
     * @return the instrumentation
     */
    public static Instrumentation getInstrumentation() {
        if (instrumentation == null) {
            throw new IllegalStateException("I must be started as javaagent");
        }
        return instrumentation;
    }

    /**
     * Get the arguments from the call as agent.
     *
     * @return the args
     */
    public String getArgs() {
        return args;
    }

    /**
     * Gets the loaded classes.
     *
     * @return the loaded classes
     */
    public Class<?>[] getLoadedClasses() {
        return getInstrumentation().getAllLoadedClasses();
    }

    /**
     * Gets the loaded classes.
     *
     * @param classloader the classloader
     * @return the loaded classes
     * @see ClasspathAgentMBean#getLoadedClasses(ClassLoader)
     */
    public Class<?>[] getLoadedClasses(final ClassLoader classloader) {
        return getInstrumentation().getInitiatedClasses(classloader);
    }



    /**
     * Returns the classes which were loaded by the classloader. The loaded
     * packages are returned as string array so that it can be displayed by the
     * 'jconsole'.
     * <p>
     * Note: Because we had in the past some doublets in the resulting array we
     * use now a {@link SortedSet} for sorting.
     * </p>
     *
     * @return the classnames as string array
     */
    public String[] getLoadedClassnames() {
        Class<?>[] classes = this.getLoadedClasses();
        SortedSet<String> classnames = new TreeSet<>();
        for (Class<?> aClass : classes) {
            classnames.add(aClass == null ? "-" : aClass.toString());
        }
        int size = classnames.size();
        return classnames.toArray(new String[size]);
    }

    /**
     * Checks if is active. This is true if this class here was started as
     * Java agent.
     *
     * @return true, if started as Java agent
     */
    public boolean isActive() {
        return instrumentation != null;
    }

    /**
     * We use JDK logging because we don't want a dependency to other JAR
     * files.
     */
    private static void setUpLogging() {
        if (System.getProperty("java.util.logging.config.file") == null) {
            try {
                setUpLogging("logging.properties");
            } catch (IOException ioe) {
                LOG.warning("Using default logging because can't read 'logging.properties': " + ioe);
            }
        }
    }

    private static void setUpLogging(final String resourceName) throws IOException {
        InputStream istream = ClasspathAgent.class.getResourceAsStream("logging.properties");
        if (istream == null) {
            LOG.warning("Using default logging because resource '" + resourceName + "' not found.");
        } else {
            try {
                LogManager.getLogManager().readConfiguration(istream);
            } finally {
                istream.close();
            }
        }
    }

    private void registerAsMBean() throws MBeanRegistrationException, NotCompliantMBeanException,
            MalformedObjectNameException {
        try {
            ObjectName mbeanName = new ObjectName(MBEAN_NAME);
            ManagementFactory.getPlatformMBeanServer().registerMBean(this, mbeanName);
        } catch (InstanceAlreadyExistsException e) {
            LOG.info("Registration of \"" + MBEAN_NAME + "\" ignored because of " + e);
        }
    }

    /**
     * Prints the loaded classes to the log output.
     *
     * @see ClasspathAgentMBean#logLoadedClasses()
     */
    public void logLoadedClasses() {
        try {
            StringWriter writer = new StringWriter();
            this.dumpLoadedClasses(new BufferedWriter(writer));
            LOG.info(writer.toString().trim());
            writer.close();
        } catch (IOException ioe) {
            LOG.log(Level.SEVERE, "Cannot log loaded classes:", ioe);
        }
    }

    /**
     * This operation dumps the loaded classes to a temporary file with the
     * prefix "dumpLoadedClasses" and the extension ".txt".
     *
     * To be able to see the name of the temporary file in the 'jconsole' it
     * is returned as value.
     *
     * @return the temporary file
     * @throws IOException Signals that an I/O exception has occurred.
     * @see ClasspathAgentMBean#dumpLoadedClasses()
     */
    public File dumpLoadedClasses() throws IOException {
        File dumpFile = File.createTempFile("dumpLoadedClasses", ".txt");
        dumpLoadedClasses(dumpFile);
        return dumpFile;
    }

    /**
     * This operation dumps the loaded classes to the given file.
     *
     * @param filename the file where the classes are dumped to.
     * @throws IOException Signals that an I/O exception has occurred.
     * @see ClasspathAgentMBean#dumpLoadedClasses(String)
     */
    public void dumpLoadedClasses(final String filename) throws IOException {
        this.dumpLoadedClasses(new File(filename));
    }

    /**
     * This operation dumps the loaded classes to the given file.
     *
     * @param dumpFile the file where the classes are dumped to.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void dumpLoadedClasses(final File dumpFile) throws IOException {
        LOG.info("Loaded classes will be dumped to '" + dumpFile + "'.");
        try (FileWriter writer = new FileWriter(dumpFile)) {
            dumpLoadedClasses(new BufferedWriter(writer));
        }
    }

    private void dumpLoadedClasses(final BufferedWriter writer) throws IOException {
        String[] classes = this.getLoadedClassnames();
        int numberClasses = 0;
        int numberInterfaces = 0;
        writer.write("=== Loaded: " + classes.length + " Classes ===");
        writer.newLine();
        for (String aClass : classes) {
            writer.write(aClass);
            writer.newLine();
            if (aClass.startsWith("class")) {
                numberClasses++;
            } else if (aClass.startsWith("interface")) {
                numberInterfaces++;
            }
        }
        writer.write("=== Summary: " + numberClasses + " Classes / " + numberInterfaces + " Interfaces ===");
        writer.newLine();
        writer.flush();
    }

}

