/*
 * Copyright (c) 2012-2025 by Oliver Boehm
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

import clazzfish.core.ClassLoading;
import clazzfish.core.spi.CsvXPorter;

import java.io.File;
import java.io.Serializable;
import java.net.URI;

/**
 * This is the MBean for the {@link ClasspathAgent} to be able to register
 * this class for a JMX console like jconsole or visualvm.
 * Originally this interface was part of patterntesting-agent.
 *
 * @author oliver
 */
public interface ClasspathAgentMBean extends ClassLoading, Serializable {

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
     * <p>
     * To be able to see the name of the temporary file in the 'jconsole' it
     * should be returned as value.
     * </p>
     *
     * @return the temporary file
     * @since 1.5
     */
    File dumpLoadedClasses();

    /**
     * Gets all classes which are available thru the classpath
     *
     * @return all classes of the classpath
     * @since 3.0
     */
    String[] getAllClasses();

    /**
     * Gets unused classes. If you want to find dead classed look at the
     * unused classes.
     *
     * @return all unused classes of the classpath
     * @since 3.0
     */
    String[] getUnusedClasses();

    /**
     * Returns the URI where a little statistic of loaded and unloaded
     * classes are dumped to.
     *
     * @return dump URI or "dev:/null" if nothing is dumped
     * @since 3.0
     */
    URI getDumpURI();

    /**
     * Indicates if class statistic is damped at the end of the application.
     *
     * @return true if class statistic is dumped by agent
     */
    default boolean isDumping() {
        return !CsvXPorter.NULL_URI.equals(getDumpURI());
    }

}

