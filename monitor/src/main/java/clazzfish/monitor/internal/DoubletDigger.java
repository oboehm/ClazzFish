/*
 * $Id: DoubletDigger.java,v 1.11 2017/09/01 20:33:17 oboehm Exp $
 *
 * Copyright (c) 2016 by Oliver Boehm
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
 * (c)reated 09.03.2016 by oliver (ob@oasd.de)
 */

package clazzfish.monitor.internal;

import clazzfish.monitor.util.Converter;
import clazzfish.monitor.util.ObjectComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The DoubletDigger looks for classes which appears more than once in the
 * classpath.
 * <p>
 * Originally the class was part of the PatternTesting project where it was
 * located in the ClasspathMonitor till v0.6.1. But it gets too big so it was
 * extracted to its own class.
 * </p>
 *
 * @author oliver
 */
public final class DoubletDigger {

	private static final Logger LOG = LoggerFactory.getLogger(DoubletDigger.class);
	private final Map<Class<?>, Boolean> doubletClasses = new ConcurrentHashMap<>();

	private final ClassLoader cloader;
	private final ClasspathDigger classpathDigger;
	private final List<Class<?>> doubletList = new CopyOnWriteArrayList<>();

	/**
	 * Instantiates a new doublet digger.
	 *
	 * @param classpathDigger
	 *            the classpath digger
	 */
	public DoubletDigger(final ClasspathDigger classpathDigger) {
		this.classpathDigger = classpathDigger;
		this.cloader = classpathDigger.getClassLoader();
	}

	/**
	 * Resets the doubletList and maybe later other things. At the moment it is
	 * only used by ClasspathMonitorTest by the testGetDoubletListPerformance()
	 * method.
	 */
	protected final void reset() {
		doubletList.clear();
	}

	/**
	 * Is the given class a doublet, i.e. can it be found more than once in the
	 * classpath?
	 * <p>
	 * Note: Because this method is needed by other methods like
	 * getIncompatibleClassList() in the ClasspathMonitor the result of it is
	 * cached. This speeds up this method about the factor 2000 and more:
	 * </p>
	 *
	 * <pre>
	 * Implementation | Mode  | Score    | Unit
	 * ---------------+-------+----------+-------
	 * old (till 1.4) | thrpt |    35514 | ops/s
	 * new (cached)   | thrpt | 92968300 | ops/s
	 * </pre>
	 * <p>
	 * This results were calculated with the help of JMH and was executed on a
	 * Dell Latitude e6520 with i5 processor (i5-2540M CPU @ 2.60GHz).
	 * </p>
	 *
	 * @param clazz
	 *            the class to be checked
	 * @return true if class is found more than once in the classpath
	 */
	public boolean isDoublet(final Class<?> clazz) {
		Boolean doublet = this.doubletClasses.get(clazz);
		if (doublet == null) {
			String classname = clazz.getName();
			String resource = Converter.classToResource(classname);
			try {
				doublet = isDoublet(resource);
			} catch (NoSuchElementException ex) {
				LOG.trace("{} is not found:", clazz, ex);
				LOG.debug("{} is a proxy or similar class because classloader does not find it:", clazz);
				doublet = false;
			}
			this.doubletClasses.put(clazz, doublet);
		}
		return doublet;
	}

	/**
	 * Checks if is doublet. If a resource appears with the same URI in the
	 * classpath this is not considered as classpath, only if it is a real
	 * doubliet.
	 *
	 * @param name
	 *            the name
	 * @return true, if checks if is doublet
	 */
	public boolean isDoublet(final String name) {
		Enumeration<URL> resources = this.classpathDigger.getResources(name);
		if (!resources.hasMoreElements()) {
			throw new NoSuchElementException("resource '" + name + "' not found");
		}
		String rsc = resources.nextElement().toString();
		while (resources.hasMoreElements()) {
		    String doublet = resources.nextElement().toString();
		    if (!rsc.equals(doublet)) {
	            logDoublets(name);
	            return true;
		    }
		}
		return false;
	}

	/**
	 * Gets the doublet.
	 *
	 * @param name the name
	 * @param nr the nr (starting at 0)
	 * @return the doublet
	 */
	public URI getDoublet(final String name, final int nr) {
		Enumeration<URL> resources = this.classpathDigger.getResources(name);
		int i = 0;
		while (resources.hasMoreElements()) {
			URL url = resources.nextElement();
			if (i == nr) {
				try {
					return url.toURI();
				} catch (URISyntaxException ex) {
					LOG.debug("Cannot convert {} to URI:", url, ex);
					return URI.create(url.toString());
				}
			}
			i++;
		}
		return null;
	}

	/**
	 * Gets the doublet.
	 *
	 * @param clazz
	 *            the clazz
	 * @param nr
	 *            the nr
	 * @return the doublet
	 */
	public URI getDoublet(final Class<?> clazz, final int nr) {
		String resource = Converter.classToResource(clazz.getName());
		return this.getDoublet(resource, nr);
	}

	private void logDoublets(final String name) {
		if (LOG.isTraceEnabled()) {
			List<URL> doublets = new ArrayList<>();
			Enumeration<URL> resources = this.classpathDigger.getResources(name);
			while (resources.hasMoreElements()) {
				URL url = resources.nextElement();
				doublets.add(url);
			}
			LOG.trace("{} doublets found: {}", name, doublets);
		}
	}

	/**
	 * Gets the doublets.
	 *
	 * @return the doublets
	 */
	public String[] getDoubletClasses() {
		LOG.debug("Calculating doublet classes...");
		List<Class<?>> classes = this.getDoubletClassList();
		String[] doublets = new String[classes.size()];
		for (int i = 0; i < doublets.length; i++) {
			doublets[i] = classes.get(i).toString();
		}
		LOG.debug("Calculating doublet classes successful finished with {} doublet(s) found.", doublets.length);
		return doublets;
	}

	/**
	 * Gets the doublet resources.
	 *
	 * @return the doublets
	 */
	public String[] getDoubletResources() {
		LOG.debug("Calculating doublet resources...");
		List<String> resources = this.getDoubletResourceList();
		LOG.debug("Calculating doublet classes successful finished with {} doublet(s) found.", resources.size());
		return resources.toArray(new String[resources.size()]);
	}

	/**
	 * Gets the doublet list.
	 *
	 * @return the doublet list
	 */
	public synchronized List<Class<?>> getDoubletClassList() {
		if (multiThreadingEnabled) {
			return getDoubletListParallel();
		} else {
			return getDoubletListSerial();
		}
	}

	/**
	 * Gets the doublet list.
	 *
	 * @return the doublet list
	 */
	public synchronized List<String> getDoubletResourceList() {
		List<String> loadedResources = this.classpathDigger.getLoadedResources();
		List<String> doubletResources = new ArrayList<>();
		for (String rsc : loadedResources) {
			if (this.isDoublet(rsc)) {
				doubletResources.add(rsc);
			}
		}
		return doubletResources;
	}

	/**
	 * Looks for each loaded class if it is a doublet or not. For the
	 * performance reason it looks in the doubletList from the last time if it
	 * is already found. This is done because normally the number of doublets
	 * does not decrease.
	 * <p>
	 * The method is protetected for testing reasons.
	 * </p>
	 *
	 * @return a sorted list of found doublets
	 */
	protected List<Class<?>> getDoubletListSerial() {
		List<Class<?>> loadedClassList = this.classpathDigger.getLoadedClasses();
		synchronized (doubletList) {
			for (Class<?> clazz : loadedClassList) {
				if (doubletList.contains(clazz)) {
					continue;
				}
				try {
					if (this.isDoublet(clazz)) {
						doubletList.add(clazz);
					}
				} catch (NoSuchElementException nsee) {
					LOG.trace("{} not found -> ignored:", clazz, nsee);
				}
			}
			sortDoubletList();
		}
		return Collections.unmodifiableList(doubletList);
	}

	/**
	 * On Java 6 you may get
	 *
	 * <pre>
	 * java.lang.UnsupportedOperationException
	 *     at java.util.concurrent.CopyOnWriteArrayList$COWIterator.set(CopyOnWriteArrayList.java:1013)
	 *     at java.util.Collections.sort(Collections.java:161)
	 * </pre>
	 *
	 * as exception. So we provide a fallback here for this situation.
	 */
	private void sortDoubletList() {
		try {
			Collections.sort(doubletList, new ObjectComparator());
		} catch (UnsupportedOperationException ex) {
			LOG.debug("Will sort doubletList with fallback because Collections.sort(..) failed:", ex);
			sortList(doubletList);
		}
	}

	private static void sortList(List<Class<?>> list) {
		List<Class<?>> sorted = new ArrayList<>(list.size());
		Collections.sort(sorted, new ObjectComparator());
		list.clear();
		list.addAll(sorted);
	}

	/**
	 * To string.
	 *
	 * @return the string
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " for " + this.cloader;
	}

	///// M U L T I T H R E A D I N G S E C T I O N ///////////////////

	/** The multi threading enabled. */
	private boolean multiThreadingEnabled = getMultiThreadingEnabled();

	/**
	 * Multi threading is enabled if more than one processor will be found or if
	 * property "multiThreadingEnabled=true" is set.
	 *
	 * @return true on multi core processors
	 */
	private static boolean getMultiThreadingEnabled() {
		boolean enabled = Boolean.getBoolean(System.getProperty("multiThreadingEnabled", "false"));
		if (enabled) {
			LOG.debug("Multi threading is enabled.");
			return true;
		}
		int n = Runtime.getRuntime().availableProcessors();
		enabled = (n > 1);
		LOG.debug("{} processors found, multi threading is {}enabled.", n, enabled ? "" : "not ");
		return enabled;
	}

	/**
	 * Is multi threading enabled? Multi threading is automatically enabled if
	 * more than one processor is found. Otherwise you can use set via the
	 * system property "multiThreadingEnabled=true" to activate it.
	 *
	 * @return true if multi threading is enabled for this class.
	 */
	public boolean isMultiThreadingEnabled() {
		return multiThreadingEnabled;
	}

	/**
	 * Here you can enable or disable the (experimental) multi threading mode to
	 * see if it is really faster on a mult-core machine.
	 *
	 * @param enabled true or false
	 */
	public void setMultiThreadingEnabled(final boolean enabled) {
		multiThreadingEnabled = enabled;
	}

	/**
	 * This was an experiment in v0.9.7: can we tune getDoubleList() by using
	 * thread techniques like consumer-/producer-pattern? Or is the
	 * synchronization overhead to big?
	 * <p>
	 * Multi threading is automatically activated if property
	 * "multiThreadingEnabled" is set to true or if more than one available
	 * processor is found.
	 * </p>
	 * <p>
	 * The method is protetected for testing reasons.
	 * </p>
	 *
	 * @return a list of doublets
	 */
	@SuppressWarnings("unchecked")
	protected List<Class<?>> getDoubletListParallel() {
		List<Class<?>> loadedClassList = this.classpathDigger.getLoadedClasses();
		if (loadedClassList.isEmpty()) {
			return loadedClassList;
		}
		LOG.trace("Creating queue with {} elements for synchronisation.", loadedClassList.size());
		Queue<Class<?>> queue = new ConcurrentLinkedQueue<>(loadedClassList);
		Class<?> lastClass = loadedClassList.get(loadedClassList.size() - 1);
		if (this.isDoublet(lastClass)) {
			doubletList.add(lastClass);
		}
		LOG.trace("Creating multiple threads.");
		int n = 2;
		List<Class<?>>[] l = new List[n];
		DoubletDiggerRunner[] d = new DoubletDiggerRunner[n];
		Thread[] t = new Thread[n];
		for (int i = 0; i < n; i++) {
			l[i] = new ArrayList<>();
			d[i] = new DoubletDiggerRunner(queue, lastClass, l[i]);
			t[i] = new Thread(d[i]);
			t[i].start();
		}
		LOG.debug("Starting {} threads...", n);
		for (int i = 0; i < n; i++) {
			try {
				LOG.trace("{} started...", t[i]);
				t[i].join();
				LOG.trace("{} finished.", t[i]);
			} catch (InterruptedException ie) {
				LOG.info("Waiting for {} threads are interrupted:", n, ie);
				Thread.currentThread().interrupt();
			}
		}
		LOG.debug("Starting {} threads finished - will add results to doublet list.", n);
		for (int i = 0; i < n; i++) {
			doubletList.addAll(l[i]);
		}
		sortDoubletList();
		LOG.trace("Result of {} threads was added to doublet list and sorted.", n);
		return Collections.unmodifiableList(doubletList);
	}

	/**
	 * This class is needed to realize multi threading.
	 */
	class DoubletDiggerRunner implements Runnable {

		private final Queue<Class<?>> queue;
		private final Class<?> lastClass;
		private final List<Class<?>> newDoublets;

		/**
		 * Instantiates a new doublet digger.
		 *
		 * @param queue
		 *            the queue
		 * @param lastClass
		 *            the last class
		 * @param newDoublets
		 *            the new doublets
		 */
		public DoubletDiggerRunner(final Queue<Class<?>> queue, final Class<?> lastClass,
				final List<Class<?>> newDoublets) {
			this.queue = queue;
			this.lastClass = lastClass;
			this.newDoublets = newDoublets;
		}

		/**
		 * Run.
		 *
		 * @see Runnable#run()
		 */
		@Override
		public void run() {
			LOG.debug("Running {}...", this);
			while (!queue.isEmpty()) {
				try {
					Class<?> clazz = queue.remove();
					if (clazz.equals(lastClass)) {
						LOG.trace("Last {} reached.", clazz);
						queue.add(clazz);
						break;
					}
					if (!doubletList.contains(clazz) && isDoublet(clazz)) {
						newDoublets.add(clazz);
					}
				} catch (NoSuchElementException nsee) {
					LOG.debug("No element from {} found:", queue, nsee);
				}
			}
			LOG.debug("Running {} finished with {} doublets found.", this, this.newDoublets.size());
		}

		/**
		 * To string.
		 *
		 * @return the string
		 * @see Object#toString()
		 */
		@Override
		public String toString() {
			return "DoubletDigger-" + Thread.currentThread().getId();
		}

	}

}
