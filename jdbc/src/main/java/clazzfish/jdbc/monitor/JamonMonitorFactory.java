/*
 * $Id: JamonMonitorFactory.java,v 1.20 2016/12/18 20:19:36 oboehm Exp $
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
 * (c)reated 27.12.2008 by oliver (ob@oasd.de)
 */
package clazzfish.jdbc.monitor;

import com.jamonapi.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is a thin wrapper around com.jamonapi.MonitorFactory to keep the
 * AbstractStatistic class clean from com.jamonapi dependencies.
 *
 * @author <a href="boehm@javatux.de">oliver</a>
 * @version $Revision: 1.20 $
 * @since 27.12.2008
 */
public final class JamonMonitorFactory extends ProfileMonitorFactory {

	private static final Logger LOG = LoggerFactory.getLogger(JamonMonitorFactory.class);
	private static final Map<String, MonitorFactoryInterface> monitorFactories = new ConcurrentHashMap<>();

	/**
	 * Instantiates a new jamon monitor factory.
	 *
	 * @param rootMonitor
	 *            the root monitor
	 */
	public JamonMonitorFactory(final SimpleProfileMonitor rootMonitor) {
		super(rootMonitor);
	}

	/**
	 * Sets the max num monitors.
	 *
	 * @param maxMonitors
	 *            the new max num monitors
	 */
	@Override
	public void setMaxNumMonitors(final int maxMonitors) {
		setMaxNumMonitors(maxMonitors, this.rootMonitor);
		int newSize = this.getMonitors().length;
		if (newSize > maxMonitors) {
			LOG.warn("Cannot reduce max. number of monitors to {} because {} has already {} monitors stored.",
					maxMonitors, rootMonitor, newSize);
		}
	}

	/**
	 * Sets the max num monitors.
	 *
	 * @param maxMonitors
	 *            the max monitors
	 * @param rootMonitor
	 *            the root monitor
	 * @since 1.6
	 */
	public static void setMaxNumMonitors(final int maxMonitors, final ProfileMonitor rootMonitor) {
		MonitorFactoryInterface factory = getMonitorFactory(rootMonitor.getLabel());
		factory.setMaxNumMonitors(maxMonitors);
	}

	private static MonitorFactoryInterface getMonitorFactory(final String label) {
		MonitorFactoryInterface factory = monitorFactories.get(label);
		if (factory == null) {
			factory = new FactoryEnabled();
			monitorFactories.put(label, factory);
		}
		return factory;
	}

	/**
	 * Gets the max num monitors.
	 *
	 * @return the max num monitors
	 * @see ProfileMonitorFactory#getMaxNumMonitors()
	 */
	@Override
	public int getMaxNumMonitors() {
		return getMaxNumMonitors(this.rootMonitor);
	}

	/**
	 * Gets the max num monintors.
	 *
	 * @param rootMonitor
	 *            the root monitor
	 * @return the max num monintors
	 */
	public static int getMaxNumMonitors(final ProfileMonitor rootMonitor) {
		MonitorFactoryInterface factory = getMonitorFactory(rootMonitor.getLabel());
		return factory.getMaxNumMonitors();
	}

	/**
	 * Reset.
	 *
	 * @see ProfileMonitorFactory#reset()
	 */
	@Override
	public void reset() {
		reset(this.rootMonitor);
	}

	/**
	 * Reset.
	 *
	 * @param rootMonitor
	 *            the root monitor
	 * @since 1.4.2
	 */
	public static void reset(final ProfileMonitor rootMonitor) {
		reset(rootMonitor.getLabel());
	}

	/**
	 * Reset.
	 *
	 * @param rootLabel
	 *            the root label
	 * @since 1.4.2
	 */
	public static synchronized void reset(final String rootLabel) {
		MonitorFactoryInterface factory = getMonitorFactory(rootLabel);
		factory.reset();
	}

	/**
	 * Start.
	 *
	 * @param label
	 *            the label
	 * @param root
	 *            the root
	 * @return the profile monitor
	 */
	public static ProfileMonitor start(final String label, final ProfileMonitor root) {
		return start(label, root.getLabel());
	}

	/**
	 * Start.
	 *
	 * @param label
	 *            the label
	 * @param rootLabel
	 *            the root label
	 * @return the profile monitor
	 */
	public static ProfileMonitor start(final String label, final String rootLabel) {
		MonitorFactoryInterface factory = getMonitorFactory(rootLabel);
		Monitor mon = factory.start(label);
		if (mon instanceof NullMonitor) {
			LOG.debug("Limit of {} monitors reached.", factory.getMaxNumMonitors());
			reduceMonitors(factory);
			mon = factory.start(label);
		}
		return new JamonMonitor(mon);
	}

	private static void reduceMonitors(MonitorFactoryInterface factory) {
		Map<MonKey, Monitor> monitorMap = factory.getMap();
		Map.Entry<MonKey, Monitor> first = monitorMap.entrySet().iterator().next();
		for (Map.Entry<MonKey, Monitor> entry : monitorMap.entrySet()) {
			Monitor mon = entry.getValue();
			if (mon.getFirstAccess().before(first.getValue().getFirstAccess())) {
				first = entry;
			}
		}
		LOG.debug("Removing {} to reduce number of monitors.", first);
		factory.remove(first.getKey());
	}

	/**
	 * Adds the monitors.
	 *
	 * @param labels
	 *            the labels
	 * @see ProfileMonitorFactory#addMonitors(List)
	 */
	@Override
	public void addMonitors(final List<String> labels) {
		for (String label : labels) {
			start(label, this.rootMonitor.getLabel());
		}
	}

	/**
	 * Gets the monitor.
	 *
	 * @param label
	 *            the label
	 * @return the monitor
	 * @see ProfileMonitorFactory#getMonitor(String)
	 */
	@Override
	public ProfileMonitor getMonitor(final String label) {
		return getMonitor(label, this.rootMonitor);
	}

	/**
	 * Gets the monitor.
	 *
	 * @param label
	 *            the label
	 * @param root
	 *            the root monitor
	 * @return the monitor
	 * @since 1.4.2
	 */
	public static ProfileMonitor getMonitor(final String label, final ProfileMonitor root) {
		return start(label, root);
	}

	/**
	 * Gets the monitors.
	 *
	 * @return the monitors
	 */
	@Override
	public ProfileMonitor[] getMonitors() {
		return getMonitors(this.rootMonitor);
	}

	/**
	 * Gets the monitors.
	 *
	 * @param root
	 *            the root
	 * @return the monitors
	 * @since 1.4.2
	 */
	public static ProfileMonitor[] getMonitors(final ProfileMonitor root) {
		return getMonitors(root.getLabel());
	}

	/**
	 * Gets the monitors.
	 *
	 * @param rootLabel
	 *            the root label
	 * @return the monitors
	 * @since 1.4.2
	 */
	public static ProfileMonitor[] getMonitors(final String rootLabel) {
		MonitorFactoryInterface factory = monitorFactories.get(rootLabel);
		if (factory == null) {
			return new JamonMonitor[0];
		} else {
			return getMonitorsFrom(factory);
		}
	}

	private static ProfileMonitor[] getMonitorsFrom(final MonitorFactoryInterface factory) {
		MonitorComposite rootMonitor = factory.getRootMonitor();
		Monitor[] monitors = rootMonitor.getMonitors();
		if (monitors == null) {
			return new JamonMonitor[0];
		}
		ProfileMonitor[] profMonitors = new JamonMonitor[monitors.length];
		for (int i = 0; i < monitors.length; i++) {
			profMonitors[i] = new JamonMonitor(monitors[i]);
		}
		return profMonitors;
	}

}
