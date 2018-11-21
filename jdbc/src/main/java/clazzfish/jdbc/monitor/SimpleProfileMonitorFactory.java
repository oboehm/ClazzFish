/*
 * Copyright (c) 2015 by Oli B.
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
 * (c)reated 19.12.2015 by Oli B. (ob@aosd.de)
 */

package clazzfish.jdbc.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * This is the counterpart to JamonMonitorFactory. It encapsulate the different
 * behaviour of the {@link SimpleProfileMonitor} for the AbstractStatistic class.
 *
 * @author oboehm (ob@aosd.de)
 * @since 1.6 (19.12.2015)
 */
public final class SimpleProfileMonitorFactory extends ProfileMonitorFactory {

	private static final Logger LOG = LoggerFactory.getLogger(SimpleProfileMonitorFactory.class);
	private int maxSize = Integer.MAX_VALUE;

	/**
	 * Instantiates a new jamon monitor factory.
	 *
	 * @param rootMonitor
	 *            the root monitor
	 */
	public SimpleProfileMonitorFactory(final SimpleProfileMonitor rootMonitor) {
		super(rootMonitor);
	}

	/**
	 * Gets the monitors.
	 *
	 * @return the monitors
	 */
	@Override
	public ProfileMonitor[] getMonitors() {
		return this.rootMonitor.getMonitors();
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
		SimpleProfileMonitor parent = this.getSimpleProfileMonitor(label);
		return new SimpleProfileMonitor(label, parent);
	}

	private SimpleProfileMonitor getSimpleProfileMonitor(final String sig) {
		SimpleProfileMonitor monitor = this.rootMonitor.getMonitor(sig);
		if (monitor == null) {
			monitor = new SimpleProfileMonitor(sig, this.rootMonitor);
			int tooMuch = this.rootMonitor.getNumberOfMonitors() - maxSize;
			for (int i = 0; i < tooMuch; i++) {
				ProfileMonitor[] monitors = this.rootMonitor.getMonitors();
				SimpleProfileMonitor oldest = getOldestMonitorOf(monitors);
				this.removeMonitor(oldest);
			}
		}
		return monitor;
	}

	private void removeMonitor(final SimpleProfileMonitor monitor) {
		this.rootMonitor.removeMonitor(monitor);
	}

	private static SimpleProfileMonitor getOldestMonitorOf(final ProfileMonitor[] monitors) {
		ProfileMonitor oldest = monitors[0];
		int maxHits = oldest.getHits();
		double maxAvg = oldest.getAvg();
		double maxTotal = oldest.getTotal();
		double maxMax = oldest.getMax();
		for (int i = 1; i < monitors.length; i++) {
			ProfileMonitor mon = monitors[i];
			if (mon == null) {
				LOG.info("Will return {} as oldest because monitor {} is null (lost?).", oldest, i);
				break;
			}
			if (oldest.getLastAccess().after(mon.getFirstAccess()) && mon.getHits() <= maxHits && mon.getAvg() <= maxAvg
					&& mon.getTotal() <= maxTotal && mon.getMax() <= maxMax) {
				oldest = mon;
			} else {
				if (mon.getHits() > maxHits) {
					maxHits = mon.getHits();
				}
				if (mon.getAvg() > maxAvg) {
					maxAvg = mon.getAvg();
				}
				if (mon.getTotal() > maxTotal) {
					maxTotal = mon.getTotal();
				}
				if (mon.getMax() > maxMax) {
					maxMax = mon.getMax();
				}
			}
		}
		return (SimpleProfileMonitor) oldest;
	}

	/**
	 * Gets the max num monitors.
	 *
	 * @return the max num monitors
	 * @see ProfileMonitorFactory#getMaxNumMonitors()
	 */
	@Override
	public int getMaxNumMonitors() {
		return this.maxSize;
	}

	/**
	 * Here you can set the maximal number of monitors.
	 *
	 * @param size
	 *            the new max size
	 * @since 1.6
	 */
	@Override
	public void setMaxNumMonitors(final int size) {
		this.maxSize = size;
		limitMonitorsToMaxSize();
	}

	/**
	 * Reset.
	 *
	 * @see ProfileMonitorFactory#reset()
	 */
	@Override
	public void reset() {
		this.rootMonitor.reset();
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
		this.rootMonitor.addChildren(labels);
		limitMonitorsToMaxSize();
	}

	private void limitMonitorsToMaxSize() {
		ProfileMonitor[] monitors = this.rootMonitor.getMonitors();
		int tooMuch = monitors.length - this.maxSize;
		for (int i = 0; i < tooMuch; i++) {
			SimpleProfileMonitor oldest = getOldestMonitorOf(monitors);
			this.removeMonitor(oldest);
			monitors = this.rootMonitor.getMonitors();
		}
	}

}
