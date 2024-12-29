/*
 * $Id: AbstractStatistic.java,v 1.40 2017/06/01 17:24:29 oboehm Exp $
 *
 * Copyright (c) 2008 by Oliver Boehm
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
 * (c)reated 22.12.2008 by oliver (ob@oasd.de)
 */
package clazzfish.jdbc;

import clazzfish.jdbc.monitor.*;
import clazzfish.monitor.AbstractMonitor;
import clazzfish.monitor.ClasspathMonitor;
import clazzfish.monitor.internal.Config;
import clazzfish.monitor.io.ExtendedFile;
import clazzfish.monitor.jmx.MBeanFinder;
import clazzfish.monitor.util.ClasspathHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.openmbean.*;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * This is constructed as a thin layer around com.jamonapi.MonitorFactory for
 * the needs of generating statistics. The reason for this layer is that sometimes you
 * want to minimize the use of other libraries. So this implementation provides
 * also an implementation if the JaMon library is missing.
 *
 * @author <a href="boehm@javatux.de">oliver</a>
 * @see com.jamonapi.MonitorFactory
 * @since 0.9
 */
public abstract class AbstractStatistic extends AbstractMonitor implements AbstractStatisticMBean {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractStatistic.class);

	private final ProfileMonitorFactory factory;

	/** Is JaMon library available?. */
	private static final boolean JAMON_AVAILABLE;

	/*
	 * Instances of this class *must* be initialized after isJamonAvailable 
	 * attribute is set. Otherwise you'll get a NullPointerException after MBean
	 * registration.
	 */
	static {
		JAMON_AVAILABLE = isJamonAvailable();
	}

	/**
	 * Instantiates a new profile statistic.
	 *
	 * @param rootLabel
	 *            the root label
	 */
	protected AbstractStatistic(final String rootLabel) {
		super();
		SimpleProfileMonitor rootMonitor = new SimpleProfileMonitor(rootLabel);
		factory = JAMON_AVAILABLE ? new JamonMonitorFactory(rootMonitor) : new SimpleProfileMonitorFactory(rootMonitor);
		factory.setMaxNumMonitors(100);
		registerMeAsMBean();
	}

	/**
	 * We can't reset all ProfileMonitors - we must keep the empty monitors with
	 * 0 hits to see which methods or constructors are never called.
	 */
	@Override
	public synchronized void reset() {
		List<String> labels = new ArrayList<>();
		ProfileMonitor[] monitors = getMonitors();
		for (ProfileMonitor monitor : monitors) {
			if (monitor.getHits() == 0) {
				labels.add(monitor.getLabel());
			}
		}
		this.factory.addMonitors(labels);
		LOG.debug("{} is resetted.", this);
	}
	
	/**
	 * Resets the root monitor.
	 */
	protected void resetRootMonitor() {
		this.factory.reset();
	}
	
	/**
	 * Here you can set the maximal size of the statistic entries.
	 *
	 * @param size the new max size
	 */
	@Override
	public void setMaxSize(final int size) {
		factory.setMaxNumMonitors(size);
	}

	/**
	 * Gets the max size.
	 *
	 * @return the max size
	 */
	@Override
	public int getMaxSize() {
		return factory.getMaxNumMonitors();
	}

	
	
	///// business logic (measurement, statistics and more) ///////////////

	/**
	 * Start profile monitor for the given signature.
	 *
	 * @param sig the signature
	 * @return the profile monitor
	 */
	public ProfileMonitor startProfileMonitorFor(final String sig) {
		ProfileMonitor mon = factory.getMonitor(sig);
		LOG.trace("Starting '{}' for {}.", mon, sig);
		mon.start();
		return mon;
	}

	/**
	 * Gets the monitors (unsorted).
	 *
	 * @return the monitors
	 */
	public ProfileMonitor[] getMonitors() {
		return factory.getMonitors();
	}

	/**
	 * Returns the monitor for the given label. It it does not exists an
	 * {@link IllegalArgumentException} will be thrown.
	 * 
	 * @param label label of the monitor
	 * @return monitor with the given label
	 */
	public ProfileMonitor getMonitor(String label) {
		for (ProfileMonitor profMon : this.getMonitors()) {
			if (label.equals(profMon.getLabel())) {
				return profMon;
			}
		}
		throw new IllegalArgumentException("not a valid monitor label: '" + label + "'");
	}

	/**
	 * Gets the sorted monitors.
	 *
	 * @return monitors sorted after total time (descending order)
	 */
	protected final ProfileMonitor[] getSortedMonitors() {
		ProfileMonitor[] monitors = getMonitors();
		Arrays.sort(monitors);
		return monitors;
	}

	private ProfileMonitor getMaxHitsMonitor() {
		ProfileMonitor[] monitors = getMonitors();
		ProfileMonitor max = new SimpleProfileMonitor();
		for (ProfileMonitor monitor : monitors) {
			if (monitor.getHits() >= max.getHits()) {
				max = monitor;
			}
		}
		return max;
	}

	/**
	 * Gets the max hits.
	 *
	 * @return the max hits
	 */
	@Override
	public int getMaxHits() {
		return getMaxHitsMonitor().getHits();
	}

	/**
	 * Gets the max hits label.
	 *
	 * @return the max hits label
	 */
	@Override
	public String getMaxHitsLabel() {
		return getMaxHitsMonitor().getLabel();
	}

	/**
	 * Gets the max hits statistic.
	 *
	 * @return the max hits statistic
	 */
	@Override
	public String getMaxHitsStatistic() {
		return getMaxHitsMonitor().toShortString();
	}

	private ProfileMonitor getMaxTotalMonitor() {
		ProfileMonitor[] monitors = getMonitors();
		ProfileMonitor max = new SimpleProfileMonitor();
		for (ProfileMonitor monitor : monitors) {
			if (monitor.getTotal() >= max.getTotal()) {
				max = monitor;
			}
		}
		return max;
	}

	/**
	 * Gets the max total.
	 *
	 * @return the max total
	 */
	@Override
	public double getMaxTotal() {
		return getMaxTotalMonitor().getTotal();
	}

	/**
	 * Gets the max total label.
	 *
	 * @return the max total label
	 */
	@Override
	public String getMaxTotalLabel() {
		return getMaxTotalMonitor().getLabel();
	}

	/**
	 * Gets the max total statistic.
	 *
	 * @return the max total statistic
	 */
	@Override
	public String getMaxTotalStatistic() {
		return getMaxTotalMonitor().toShortString();
	}

	private ProfileMonitor getMaxAvgMonitor() {
		ProfileMonitor[] monitors = getMonitors();
		ProfileMonitor max = monitors[0];
		double maxValue = 0.0;
		for (ProfileMonitor monitor : monitors) {
			double value = monitor.getAvg();
			if (!Double.isNaN(value) && (value > maxValue)) {
				maxValue = value;
				max = monitor;
			}
		}
		return max;
	}

	/**
	 * Gets the root monitor.
	 *
	 * @return the root monitor
	 */
	protected ProfileMonitor getRootMonitor() {
		return this.factory.getRootMonitor();
	}

	/**
	 * Gets the max avg.
	 *
	 * @return the max avg
	 */
	@Override
	public double getMaxAvg() {
		return getMaxAvgMonitor().getAvg();
	}

	/**
	 * Gets the max avg label.
	 *
	 * @return the max avg label
	 */
	@Override
	public String getMaxAvgLabel() {
		return getMaxAvgMonitor().getLabel();
	}

	/**
	 * Gets the max avg statistic.
	 *
	 * @return the max avg statistic
	 */
	@Override
	public String getMaxAvgStatistic() {
		return getMaxAvgMonitor().toShortString();
	}

	private ProfileMonitor getMaxMaxMonitor() {
		ProfileMonitor[] monitors = getMonitors();
		ProfileMonitor max = new SimpleProfileMonitor();
		for (ProfileMonitor monitor : monitors) {
			if (monitor.getMax() >= max.getMax()) {
				max = monitor;
			}
		}
		return max;
	}

	/**
	 * Gets the max max.
	 *
	 * @return the max max
	 */
	@Override
	public double getMaxMax() {
		return getMaxMaxMonitor().getMax();
	}

	/**
	 * Gets the max max label.
	 *
	 * @return the max max label
	 */
	@Override
	public String getMaxMaxLabel() {
		return getMaxMaxMonitor().getLabel();
	}

	/**
	 * Gets the max max statistic.
	 *
	 * @return the max max statistic
	 */
	@Override
	public String getMaxMaxStatistic() {
		return getMaxMaxMonitor().toShortString();
	}

	/**
	 * Gets the statistics.
	 *
	 * @return the statistics
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public TabularData getStatistics() {
		try {
			String[] itemNames = { "Label", "Units", "Hits", "Avg", "Total", "Min", "Max" };
			String[] itemDescriptions = { "method name", "time unit (e.g. ms)", "number of hits", "average time",
					"total time", "minimal time", "maximal time" };
			OpenType[] itemTypes = { SimpleType.STRING, SimpleType.STRING, SimpleType.INTEGER, SimpleType.DOUBLE,
					SimpleType.DOUBLE, SimpleType.DOUBLE, SimpleType.DOUBLE };
			CompositeType rowType = new CompositeType("propertyType", "property entry", itemNames, itemDescriptions,
					itemTypes);
			TabularDataSupport data = MBeanFinder.createTabularDataSupport(rowType, itemNames);
			ProfileMonitor[] monitors = getSortedMonitors();
			for (ProfileMonitor monitor : monitors) {
				Map<String, Object> map = new HashMap<>();
				map.put("Label", monitor.getLabel());
				map.put("Units", monitor.getUnits());
				map.put("Hits", monitor.getHits());
				map.put("Avg", monitor.getAvg());
				map.put("Total", monitor.getTotal());
				map.put("Min", monitor.getMin());
				map.put("Max", monitor.getMax());
				CompositeDataSupport compData = new CompositeDataSupport(rowType, map);
				data.put(compData);
			}
			return data;
		} catch (OpenDataException e) {
			LOG.error("can't create TabularData for log settings", e);
			return null;
		}
	}

	/**
	 * Log statistic.
	 */
	@Override
	public void logMe() {
		LOG.info("----- Profile Statistic -----");
		ProfileMonitor[] monitors = getSortedMonitors();
		for (ProfileMonitor profMon : monitors) {
			LOG.info("{}", profMon);
		}
	}

	/**
	 * This operation dumps statistic into a (temporary) file with the
	 * classname as prefix. The name of the created file is returned
	 * so that you can see it in the 'jconsole' (if you have triggered
	 * it from there).
	 *
	 * @return the file
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public File dumpMe() throws IOException {
		File dumpDir = Config.DEFAULT.getDumpDir();
		ExtendedFile.createDir(dumpDir);
		File dumpFile = new File(dumpDir, getClass().getSimpleName() + ".csv");
		this.dumpMe(dumpFile);
		return dumpFile;
	}

	/**
	 * Dump statistic to the given file.
	 *
	 * @param dumpFile
	 *            the dump file
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
	public void dumpMe(final File dumpFile) throws IOException {
		ProfileMonitor[] monitors = getSortedMonitors();
		if (monitors.length == 0) {
			LOG.debug("No profiling data available.");
			return;
		}
		try (BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(dumpFile), StandardCharsets.UTF_8))) {
    		writer.write(monitors[0].toCsvHeadline());
    		writer.newLine();
    		for (ProfileMonitor profMon : monitors) {
    			writer.write(profMon.toCsvString());
    			writer.newLine();
    		}
		}
		LOG.info("Profiling data dumped to '{}'.", dumpFile);
	}

	/**
	 * It is only tested for Jamon 2.4 and 2.7 so we look for it
	 *
	 * @return true if Jamon 2.4 or 2.7 (or greater) was found
	 */
	private static boolean isJamonAvailable() {
		String resource = "/com/jamonapi/MonitorFactory.class";
		URL classURL = SqlStatistic.class.getResource(resource);
		if (classURL == null) {
			LOG.debug("JAMon and {} not available, using simple profiling.", resource);
			return false;
		}
		try (JarFile jarfile = ClasspathMonitor
				.whichResourceJar(ClasspathHelper.getParent(classURL.toURI(), resource))) {
			Manifest manifest = jarfile.getManifest();
			Attributes attributes = manifest.getMainAttributes();
			String version = attributes.getValue("version");
			if (version == null) {
				LOG.info("JAMon in {} available for profiling.", jarfile.getName());
				return true;
			} else if ("JAMon 2.4".equalsIgnoreCase(version) || (version.compareTo("JAMon 2.7") >= 0)) {
				LOG.info("{} available for profiling.", version);
				return true;
			} else {
				LOG.info("{} not supported (only JAMon 2.4 and 2.7 or higher), using simple profiling.", version);
			}
		} catch (IOException | URISyntaxException ex) {
			LOG.info("Will use simple profiling because cannot read manifest for {}:", classURL, ex);
		}
		return false;
	}

}
