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
 * (c)reated 24.04.16 by oliver (ob@oasd.de)
 */

package clazzfish.monitor;

import clazzfish.monitor.internal.DoubletDigger;
import clazzfish.monitor.internal.ResourcepathDigger;
import clazzfish.monitor.jmx.Description;
import clazzfish.monitor.util.ArchivEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;

/**
 * Analogous to ClasspathMonitor this class allows you to ask for resources in
 * the classpath. Originally this class was part of the PatternTesting project.
 *
 * @author <a href="ob@aosd.de">oliver</a>
 */
public class ResourcepathMonitor extends AbstractMonitor implements ResourcepathMonitorMBean {

	private static final Logger LOG = LoggerFactory.getLogger(ResourcepathMonitor.class);
	private static final Executor EXECUTOR = Executors.newCachedThreadPool();
	private static final ResourcepathMonitor INSTANCE;
	private final ResourcepathDigger resourcepathDigger;
	private final DoubletDigger doubletDigger;
	private final FutureTask<String[]> resources;

	static {
		INSTANCE = new ResourcepathMonitor();
	}

	/**
	 * We offer only some services. So there is no need to instantiate it from
	 * outside.
	 */
	protected ResourcepathMonitor() {
		this.resourcepathDigger = new ResourcepathDigger();
		this.doubletDigger = new DoubletDigger(this.resourcepathDigger);
		this.resources = getFutureResources();
	}

	private FutureTask<String[]> getFutureResources() {
		Callable<String[]> callable = new Callable<String[]>() {
			@Override
			public String[] call() {
				return getResourcesArray();
			}
		};
		FutureTask<String[]> resources = new FutureTask<>(callable);
		EXECUTOR.execute(resources);
		return resources;
	}

	/**
	 * Yes, it is a Singleton because it offers only some services. So we don't
	 * need the object twice.
	 *
	 * @return the only instance
	 */
	public static ResourcepathMonitor getInstance() {
		return INSTANCE;
	}

    /**
     * With this method you can register the {@link ResourcepathMonitor} with the
     * default name.
     * <p>
     * You can only register the {@link ResourcepathMonitor} once only. If you want
     * to register it with another name you have to first unregister it.
     * </p>
     */
    public static void registerAsMBean() {
        getInstance().registerMeAsMBean();
    }

    /**
     * Unregister ResourcepathMonitor as MBean.
     */
    public static void unregisterAsMBean() {
        getInstance().unregisterMeAsMBean();
    }

    /**
     * If you want to ask JMX if bean is already registered you can use this
     * method.
     *
     * @return true if class is registered as MBean
     */
    public static boolean isRegisteredAsMBean() {
        return getInstance().isMBean();
    }
    
	/**
	 * Scans the classpath for all resources. For performance reason we return
	 * no longer a copy but the origin array. Don't do changes for the returned
	 * array!
	 *
	 * @return all resources as String array
	 */
	@Override
	public String[] getResources() {
		try {
			return this.resources.get();
		} catch (ExecutionException ex) {
			LOG.warn("Cannot execute get of {}:", this.resources, ex);
		} catch (InterruptedException ex) {
			LOG.warn("Was interrupted before got result from {}:", this.resources, ex);
			Thread.currentThread().interrupt();
		}
		return this.getResourcesArray();
	}

	private String[] getResourcesArray() {
		Set<String> resourceSet = this.resourcepathDigger.getResources();
		return resourceSet.toArray(new String[resourceSet.size()]);
	}

	/**
	 * Looks if the given resource can be found in the classpath.
	 *
	 * @param name
	 *            of a resource e.g. "log4j.properties"
	 * @return URI of the given resource (or null if resource was not found)
	 * @see ResourcepathMonitorMBean#whichResource(String)
	 */
	@Override
	@Description("returns the URI of the given resource")
	public URI whichResource(final String name) {
		return resourcepathDigger.whichResource(name);
	}

	/**
	 * Gets the resources for a given name.
	 *
	 * @param name
	 *            the name
	 * @return the resources as URL enumeration
	 */
	public Enumeration<URL> getResourcesFor(final String name) {
		return this.resourcepathDigger.getResources(name);
	}

	/**
	 * Gets the number of resources.
	 *
	 * @param name the name of the resource
	 * @return number of resources
	 * @see ResourcepathMonitorMBean#getNoResources(String)
	 */
	@Override
	public int getNoResources(final String name) {
		Enumeration<URL> urls = getResourcesFor(name);
		if (urls == null) {
			return 0;
		}
		int n = 0;
		while (urls.hasMoreElements()) {
			n++;
			urls.nextElement();
		}
		LOG.trace("{} element(s) of {} found in classpath.", n, name);
		return n;
	}

	/**
     * Is the given classname or resource a doublet, e.g. can it be found
     * several times in the classpath? If the classname or resource is not in
     * the classpath a java.util.NoSuchElementException will be thrown.
     *
     * @param name a classname or resource
     * @return true if more than one classname or resource was found in the
     *         classpath
     * @throws NoSuchElementException the no such element exception
     * @throws NoSuchElementException if no classname or resource was
     *         found
     * @see ResourcepathMonitorMBean#isDoublet(String)
     */
	@Override
	@Description("is the given classname or resource found more than once in the classpath?")
	public boolean isDoublet(String name) {
        return this.doubletDigger.isDoublet(name);
	}

	/**
	 * Returns the first doublet of the given classname or resource.
	 *
	 * @param name
	 *            a classname or resource
	 * @return the first doublet
	 * @see ResourcepathMonitorMBean#getFirstDoublet(String)
	 */
	@Override
	@Description("returns the first doublet of the given classname or resource")
	public URI getFirstDoublet(String name) {
		return this.getDoublet(name, 1);
	}

	/**
	 * Gets the doublet.
	 *
	 * @param name
	 *            the name
	 * @param nr
	 *            the nr
	 * @return the doublet
	 * @see ResourcepathMonitorMBean#getDoublet(String, int)
	 */
	@Override
	public URI getDoublet(final String name, final int nr) {
		return this.doubletDigger.getDoublet(name, nr);
	}

	/**
	 * Gets the doublets.
     * <p>
     * Note: The Manifest file (META-INF/MANIFEST.MF) ist not counted as
     * doublet file because it is not really a resource. 
     * </p>
	 *
	 * @return the doublets
	 * @see ResourcepathMonitorMBean#getDoublets()
	 */
	@Override
	public String[] getDoublets() {
		List<String> doublets = new ArrayList<>();
		for (String resource : this.getResources()) {
			try {
				if (!"/META-INF/MANIFEST.MF".equals(resource) && this.isDoublet(resource)) {
					doublets.add(resource);
				}
			} catch (NoSuchElementException ex) {
				LOG.debug("'{}' is ignored as resource ({})", resource, ex.getMessage());
				LOG.trace("Details:", ex);
			}
		}
		return doublets.toArray(new String[doublets.size()]);
	}

	/**
	 * Looks for each found doublet resource in which classpath it was found.
	 * Resources like META-INF/MANIFEST.MF are not considered as doublet -
	 * otherwise you'll find all classpathes in the array.
	 *
	 * @return the classpath where doublets were found
	 */
	@Override
	@Description("returns the classpath where doublets were found")
	public String[] getDoubletResourcepath() {
		URI[] resourcepathURIs = this.getDoubletResourcepathURIs();
		return toStringArray(resourcepathURIs);
	}

	/**
	 * Gets the URIs of the doublet resourcepath. Doublets in the
	 * META-INF folder are filtered out because these resources
	 * contains often some meta information of the JAR.
	 *
	 * @return the URIs of the doublets
	 */
	protected URI[] getDoubletResourcepathURIs() {
		LOG.trace("Calculating doublet-resourcepath.");
		List<String> doubletsWithoutMetaInf = new ArrayList<>();
		for (String doublet : this.getDoublets()) {
			if (!"/META-INF/MANIFEST.MF".equals(doublet)) {
				doubletsWithoutMetaInf.add(doublet);
			}
		}
		Set<URI> resourcepathSet = this.resourcepathDigger.getResourcepathSet(doubletsWithoutMetaInf);
		return resourcepathSet.toArray(new URI[resourcepathSet.size()]);
	}

	/**
	 * Incompatible resources are doublets with different content.
	 *
	 * @return doublet resources with different content
	 */
	@Override
	public String[] getIncompatibleResources() {
		List<String> incompatibleResources = new ArrayList<>();
		for (String rsc : this.getDoublets()) {
			Enumeration<URL> doubletURLs = this.getResourcesFor(rsc);
			try {
				URL url = doubletURLs.nextElement();
				ArchivEntry archivEntry = new ArchivEntry(url);
				while (doubletURLs.hasMoreElements()) {
					url = doubletURLs.nextElement();
					ArchivEntry doubletEntry = new ArchivEntry(url);
					if (archivEntry.equals(doubletEntry)) {
						incompatibleResources.add(rsc);
						break;
					}
				}
			} catch (NoSuchElementException nse) {
				LOG.warn("{} is not added to incompatible resource list:", rsc, nse);
			}
		}
		return incompatibleResources.toArray(new String[incompatibleResources.size()]);
	}

	/**
	 * Gets the classpath which contains incompatible resources. Resources in
	 * the META-INF folder are filtered out because resources in this folder
	 * contains often only some meta info of the JAR.
	 *
	 * @return the classpathes where incompatible resources were found
	 */
	@Override
	public String[] getIncompatibleResourcepath() {
        URI[] resourcepathURIs = this.getIncompatibleResourcepathURIs();
        return toStringArray(resourcepathURIs);
	}
	
    /**
     * Gets the classpath which contains incompatible resources. Resources in
     * the META-INF folder are filtered out because resources in this folder
     * contains often only some meta info of the JAR.
     *
     * @return the resourcepath as array of URIs
     */
	protected URI[] getIncompatibleResourcepathURIs() {
        List<String> resourcesWithoutMetainf = new ArrayList<String>();
        for (String resource : this.getIncompatibleResources()) {
            if (!resource.startsWith("/META-INF")) {
                resourcesWithoutMetainf.add(resource);
            }
        }
        Set<URI> resourcepathSet = this.resourcepathDigger.getResourcepathSet(resourcesWithoutMetainf);
        return resourcepathSet.toArray(new URI[resourcepathSet.size()]);
	}

	/**
	 * Logs the different array to the log output.
	 */
	@Override
	public void logMe() {
		try {
			StringWriter writer = new StringWriter();
			dumpArray(this.getResources(), new BufferedWriter(writer), "resources");
			LOG.info(writer.toString());
		} catch (IOException cannothappen) {
			LOG.warn("Cannot dump resources:", cannothappen);
		}
	}

	/**
	 * This operation dumps the different MBean attributes to the given
	 * directory.
	 *
	 * @param dumpDir
	 *            the directory where the attributes are dumped to.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
	public void dumpMe(final File dumpDir) throws IOException {
		super.dumpMe(dumpDir);
        this.dump(dumpDir, "DoubletResourcepath", "DoubletResourcepathURIs", "Doublets", "IncompatibleResources",
                "IncompatibleResourcepath", "IncompatibleResourcepathURIs", "Resources");
        copyResource("RscMonREADME.txt", new File(dumpDir, "README.txt"));
	}

}
