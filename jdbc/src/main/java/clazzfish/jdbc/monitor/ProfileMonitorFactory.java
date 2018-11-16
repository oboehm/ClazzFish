/*
 * $Id: ProfileMonitorFactory.java,v 1.8 2016/12/10 20:55:19 oboehm Exp $
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
 * (c)reated 19.12.2015 by oliver (ob@oasd.de)
 */
package clazzfish.jdbc.monitor;

import java.util.ArrayList;
import java.util.List;

/**
 * This factory abstracts the differences between the monitor based on JAMon and
 * the simple monitor provided by PatternTesting itself.
 *
 * @since 1.6
 */
public abstract class ProfileMonitorFactory {

	/** The root monitor. */
	protected final SimpleProfileMonitor rootMonitor;

	/**
	 * Instantiates a new monitor factory.
	 *
	 * @param rootMonitor
	 *            the root monitor
	 */
	protected ProfileMonitorFactory(final SimpleProfileMonitor rootMonitor) {
		this.rootMonitor = rootMonitor;
	}

	/**
	 * Gets the root monitor.
	 *
	 * @return the root monitor
	 */
	public SimpleProfileMonitor getRootMonitor() {
		return this.rootMonitor;
	}

	/**
	 * Sets the max num monitors.
	 *
	 * @param maxMonitors
	 *            the new max num monitors
	 */
	public abstract void setMaxNumMonitors(final int maxMonitors);

	/**
	 * Gets the max num monitors.
	 *
	 * @return the max num monitors
	 */
	public abstract int getMaxNumMonitors();

	/**
	 * Adds the monitors.
	 *
	 * @param labels
	 *            the labels
	 */
	public abstract void addMonitors(final List<String> labels);

	/**
	 * Adds the monitor.
	 *
	 * @param label
	 *            the label
	 */
	public void addMonitor(final String label) {
		List<String> lbls = new ArrayList<>();
		lbls.add(label);
		this.addMonitors(lbls);
	}

	/**
	 * Gets the monitor.
	 *
	 * @param label
	 *            the label or signature
	 * @return the monitor
	 */
	public abstract ProfileMonitor getMonitor(final String label);

	/**
	 * Gets the monitors (unsorted).
	 *
	 * @return the monitors
	 */
	public abstract ProfileMonitor[] getMonitors();

	/**
	 * Reset.
	 */
	public abstract void reset();

	/**
	 * To string.
	 *
	 * @return the string
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " <" + this.rootMonitor + ">";
	}

}
