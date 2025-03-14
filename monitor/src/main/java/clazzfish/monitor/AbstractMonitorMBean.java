/*
 * Copyright (c) 2016-2018 by Oliver Boehm
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
 * (c)reated 25.02.2016 by oliver (ob@oasd.de)
 */

package clazzfish.monitor;

import clazzfish.monitor.jmx.Description;
import clazzfish.monitor.util.Shutdownable;

import java.io.File;
import java.io.IOException;

/**
 * Some common interface methods for a monitor MBean are listed here.
 * Originally this interface was part of the PatternTesting project.
 *
 * @author oliver
 */
public interface AbstractMonitorMBean extends Shutdownable {

	/**
	 * Prints the different MBean attributes to the log output.
	 */
	@Description("logs all attributes to the log output")
	void logMe();

	/**
	 * This operation dumps the different MBean attributes to a temporary file
	 * with a common prefix (the name of the class, e.g. ClasspathMonitor) and
	 * the extension ".txt".
	 * <p>
	 * To be able to see the name of the temporary file in the 'jconsole' it
	 * should be returned as value.
	 * </p>
	 *
	 * @return the temporary file
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Description("dumps all attributes to a temporary file")
	File dumpMe() throws IOException;

	/**
	 * This operation dumps the different MBean attributes to the given
	 * directory.
	 *
	 * @param dirname the directory name
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Description("dumps all attributs to the given directory")
	void dumpMe(final String dirname) throws IOException;

	/**
	 * This operations gives you the directory where the different MBeans
	 * attributes are dumped to.
	 *
	 * @return the dump directory
	 * @since 2.3
	 */
	@Description("gets the directory where the attributes are dumped to")
	File getDumpDir();

}
