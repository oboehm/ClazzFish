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
 * (c)reated 28.04.16 by oliver (ob@oasd.de)
 */
package clazzfish.monitor;

import clazzfish.monitor.jmx.Description;

import java.net.URI;
import java.util.NoSuchElementException;

/**
 * All the methods which might help you to find classpath problems with
 * resources are collected in this JMX interface. Most of the methods returns a
 * String because this can be easily shown in the JConsole.
 *
 * @author <a href="ob@aosd.de">oliver</a>
 * @version $
 */
@Description("Resourcepath Monitor to be able to inspect the classpath for resources and to find doublets")
public interface ResourcepathMonitorMBean extends AbstractMonitorMBean {

	/**
	 * Looks if the given resource can be found in the classpath.
	 * <p>
	 * To avoid problems like "java.rmi.UnmarshalException: failed to unmarshal
	 * class java.lang.Object; nested exception is: java.io.IOException: unknown
	 * protocol: zip" no longer a URL but URI is now returned
	 * </p>
	 *
	 * @param name
	 *            of a resource e.g. "log4j.properties"
	 * @return URI of the given resource (or null if resource was not found)
	 */
	@Description("returns the URI of the given resource")
	URI whichResource(String name);

	/**
	 * Looks in the normal classpath after all resources and returns it as
	 * String array.
	 *
	 * @return all found resources in the classpath
	 */
	@Description("returns all resources of the classpath")
	String[] getResources();

	/**
	 * Get number of resources.
	 *
	 * @param name
	 *            of the resource
	 * @return how often the resource was found
	 */
	@Description("how often the given resource is found in the classpath")
	int getNoResources(String name);

	/**
	 * Is the given classname or resource a doublet, e.g. can it be found
	 * several times in the classpath? If the classname or resource is not in
	 * the classpath a java.util.NoSuchElementException will be thrown.
	 *
	 * @param name
	 *            a classname or resource
	 * @return true if more than one classname or resource was found in the
	 *         classpath
	 * @throws NoSuchElementException
	 *             the no such element exception
	 * @throws NoSuchElementException
	 *             if no classname or resource was found
	 */
	@Description("is the given classname or resource found more than once in the classpath?")
	boolean isDoublet(String name) throws NoSuchElementException;

	/**
	 * Returns the first doublet of the given classname or resource.
	 *
	 * @param name
	 *            a classname or resource
	 * @return the first doublet
	 */
	@Description("returns the first doublet of the given classname or resource")
	URI getFirstDoublet(String name);

	/**
	 * Returns the n'th doublet of the given classname or resource.
	 *
	 * @param name
	 *            a classname or resource
	 * @param n
	 *            number of wanted doublet
	 * @return the n'th doublet URL
	 */
	@Description("returns the n'th doublet of the given classname or resource")
	URI getDoublet(String name, int n);

	/**
	 * Looks for each loaded resource if it has a doublet or not.
	 *
	 * @return a sorted array with the found doublets
	 */
	@Description("returns a sorted array of all found doublets")
	String[] getDoublets();

	/**
	 * Looks for each found doublet resource in which classpath it was found.
	 * Resources like META-INF/MANIFEST.MF which you would find each jar file
	 * should be not considered as doublet - otherwise you'll find all
	 * classpathes in the array.
	 *
	 * @return the classpath where doublets were found
	 */
	@Description("returns the classpath where doublets were found")
	String[] getDoubletResourcepath();

	/**
	 * Incompatible resources are doublets with different content.
	 *
	 * @return doublet resources with different content
	 */
	@Description("returns the doublet resources with different content")
	String[] getIncompatibleResources();

	/**
	 * Gets the incompatible classpath.
	 *
	 * @return the classpathes where incompatible classes were found
	 */
	@Description("returns the classpath where incompatible resources were found")
	String[] getIncompatibleResourcepath();

}
