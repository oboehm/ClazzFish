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
 * (c)reated 02.03.2012 by oliver (ob@oasd.de)
 */

package clazzfish.agent;

import java.io.*;

/**
 * This is the MBean for the {@link ClasspathAgent} to be able to register
 * this class for a JMX console like jconsole or visualvm.
 * Originally this class was part of patterntesting-agent.
 *
 * @author oliver
 */
public interface ClasspathAgentMBean extends Serializable{

    /**
     * Checks if agent is active. This is true if this class here was started
     * as Java agent.
     *
     * @return true, if started as Java agent
     */
    boolean isActive();

    /**
     * Get the arguments from the call as agent.
     *
     * @return the args
     */
    String getArgs();

    /**
     * Returns the classes which were loaded by the classloader.
     *
     * NOTE: Although the 'jconsole' displays the classes as 'unavailable'
     * do not remove it. It is needed by the ClasspathDigger class in
     * PatternTesting Runtime to get the loaded classes.
     *
     * @return the classes as string array
     */
    Class<?>[] getLoadedClasses();

    /**
     * Returns the classes which were loaded by the classloader.
     * The loaded packages are returned as string array so that it can
     * be displayed by the 'jconsole'.
     *
     * @return the classnames as string array
     */
    String[] getLoadedClassnames();

    /**
     * Gets the loaded classes.
     *
     * @param classloader the classloader
     * @return the loaded classes
     */
    Class<?>[] getLoadedClasses(final ClassLoader classloader);

    /**
     * Prints the loaded classes to the log output.
     *
     * @since 1.5
     */
    void logLoadedClasses();

    /**
     * This operation dumps the loaded classes to a temporary file with the
     * prefix "dumpLoadedClasses" and the extension ".txt".
     *
     * To be able to see the name of the temporary file in the 'jconsole' it
     * should be returned as value.
     *
     * @return the temporary file
     * @throws IOException Signals that an I/O exception has occurred.
     * @since 1.5
     */
    File dumpLoadedClasses() throws IOException;

    /**
     * This operation dumps the loaded classes to the given file.
     *
     * @param filename the file where the classes are dumped to.
     * @throws IOException Signals that an I/O exception has occurred.
     * @since 1.5
     */
    void dumpLoadedClasses(final String filename) throws IOException;

}

