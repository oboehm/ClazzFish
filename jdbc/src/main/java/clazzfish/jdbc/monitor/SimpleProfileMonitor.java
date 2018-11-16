/*
 * $Id: SimpleProfileMonitor.java,v 1.27 2016/12/18 20:19:36 oboehm Exp $
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
 * (c)reated 19.12.2008 by oliver (ob@oasd.de)
 */
package clazzfish.jdbc.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Class SimpleProfileMonitor.
 *
 * @author <a href="boehm@javatux.de">oliver</a>
 * @version $Revision: 1.27 $
 * @since 19.12.2008
 */
public final class SimpleProfileMonitor extends AbstractProfileMonitor {

	private static final Logger LOG = LoggerFactory.getLogger(SimpleProfileMonitor.class);
	private SimpleProfileMonitor parent;
	private final Map<String, SimpleProfileMonitor> childs = new ConcurrentHashMap<>();
	private final String label;
	/** start time in nanoseconds */
	private long startTime;
	/** the measured time in milliseconds */
	private double total;
	private double lastValue;
	private double min;
	private double max;
	/** number of calls (or "hits") */
	private int hits;
	private final Date firstAccess = new Date();
	private Date lastAccess = new Date();

	/**
	 * Instantiates a new simple profile monitor.
	 */
	public SimpleProfileMonitor() {
		this("root");
	}

	/**
	 * Instantiates a new simple profile monitor.
	 *
	 * @param rootLabel
	 *            the root label
	 */
	public SimpleProfileMonitor(final String rootLabel) {
		this.reset();
		this.label = rootLabel;
	}

	/**
	 * Reset.
	 *
	 * @see ProfileMonitor#reset()
	 */
	@Override
	public void reset() {
		this.total = 0.0;
		this.lastValue = 0.0;
		this.min = Double.MAX_VALUE;
		this.max = 0.0;
		this.hits = 0;
		this.parent = null;
		this.childs.clear();
	}

	/**
	 * Instantiates a new simple profile monitor.
	 *
	 * @param label
	 *            the label
	 * @param parent
	 *            the parent
	 */
	public SimpleProfileMonitor(final String label, final SimpleProfileMonitor parent) {
		this.reset();
		this.label = label;
		this.parent = parent;
		this.parent.addChild(this);
	}

	/**
	 * Adds the child.
	 *
	 * @param child
	 *            the child
	 */
	protected void addChild(final SimpleProfileMonitor child) {
		this.childs.put(child.label, child);
	}

	/**
	 * Adds the children.
	 *
	 * @param labels
	 *            the labels
	 */
	public void addChildren(final List<String> labels) {
		for (String lbl : labels) {
			addChild(lbl);
		}
	}

	/**
	 * Adds the child.
	 *
	 * @param lbl
	 *            the label
	 */
	public void addChild(final String lbl) {
		SimpleProfileMonitor child = new SimpleProfileMonitor(lbl, this);
		addChild(child);
	}

	/**
	 * Gets the monitors.
	 *
	 * @return the monitors
	 * @see ProfileMonitor#getMonitors()
	 */
	@Override
	public ProfileMonitor[] getMonitors() {
		return this.childs.values().toArray(new SimpleProfileMonitor[childs.size()]);
	}

	/**
	 * Gets the number of monitors.
	 *
	 * @return the number of monitors
	 * @since 1.6
	 */
	public int getNumberOfMonitors() {
		return this.childs.size();
	}

	/**
	 * Removes the given child monitor. To be sure to get the correct entry we
	 * use the label of the given monitor.
	 *
	 * @param monitor
	 *            the monitor
	 * @since 1.6
	 */
	public void removeMonitor(final SimpleProfileMonitor monitor) {
		ProfileMonitor removed = this.childs.remove(monitor.label);
		LOG.debug("{} was removed from childs.", removed);
	}

	/**
	 * Gets the monitor.
	 *
	 * @param lbl the label as String
	 * @return the monitor
	 */
	public SimpleProfileMonitor getMonitor(final String lbl) {
		if (lbl == null) {
			return null;
		}
		return this.childs.get(lbl);
	}

	/**
	 * Start.
	 *
	 * @see ProfileMonitor#start()
	 */
	@Override
	public void start() {
		this.startTime = System.nanoTime();
		this.lastAccess = new Date();
	}

	/**
	 * Stop.
	 *
	 * @see ProfileMonitor#stop()
	 */
	@Override
	public void stop() {
		long time = System.nanoTime() - startTime;
		this.add(time / 1000000.0);
	}

	/**
	 * Adds the.
	 *
	 * @param value
	 *            the measured time
	 * @see ProfileMonitor#add(double)
	 */
	@Override
	public void add(final double value) {
		this.lastValue = value;
		this.total += value;
		this.hits++;
		if (this.parent != null) {
			this.parent.add(value);
		}
		if (value < this.min) {
			this.min = value;
		}
		if (value > this.max) {
			this.max = value;
		}
	}

	/**
	 * Gets the total.
	 *
	 * @return the total
	 * @see ProfileMonitor#getTotal()
	 */
	@Override
	public double getTotal() {
		return this.total;
	}

	/**
	 * Gets the last value.
	 *
	 * @return the last value
	 * @see ProfileMonitor#getLastValue()
	 */
	@Override
	public double getLastValue() {
		return this.lastValue;
	}

	/**
	 * Gets the max.
	 *
	 * @return the max
	 * @see ProfileMonitor#getMax()
	 */
	@Override
	public double getMax() {
		return this.max;
	}

	/**
	 * Gets the min.
	 *
	 * @return the min
	 * @see ProfileMonitor#getMin()
	 */
	@Override
	public double getMin() {
		return this.min;
	}

	/**
	 * Gets the hits.
	 *
	 * @return the hits
	 * @see ProfileMonitor#getHits()
	 */
	@Override
	public int getHits() {
		return this.hits;
	}

	/**
	 * Gets the avg.
	 *
	 * @return the avg
	 * @see ProfileMonitor#getAvg()
	 */
	@Override
	public double getAvg() {
		return this.total / this.hits;
	}

	/**
	 * Gets the label.
	 *
	 * @return the label
	 * @see ProfileMonitor#getLabel()
	 */
	@Override
	public String getLabel() {
		return this.label;
	}

	/**
	 * To string.
	 *
	 * @return the string
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		return this.getLabel() + " - " + toShortString();
	}

	/**
	 * To short string.
	 *
	 * @return the string
	 * @see ProfileMonitor#toShortString()
	 */
	@Override
	public String toShortString() {
		return "total: " + this.total + " ms / avg: " + this.getAvg() + " ms / hits: " + this.hits;
	}

	/**
	 * To csv headline.
	 *
	 * @return the string
	 * @see ProfileMonitor#toCsvHeadline()
	 */
	@Override
	public String toCsvHeadline() {
		return "Label; Unit; Total; Avg; Hits; Max; Min";
	}

	/**
	 * To csv string.
	 *
	 * @return the string
	 * @see ProfileMonitor#toCsvString()
	 */
	@Override
	public String toCsvString() {
		return '"' + this.getLabel() + "\"; ms; " + this.total + "; " + this.getAvg() + "; " + this.hits + "; "
				+ this.getMax() + "; " + this.getMin();
	}

	/**
	 * Gets the units.
	 *
	 * @return the units
	 * @see ProfileMonitor#getUnits()
	 */
	@Override
	public String getUnits() {
		return "ms";
	}

	/**
	 * Gets the active.
	 *
	 * @return the active
	 * @see ProfileMonitor#getActive()
	 */
	@Override
	public double getActive() {
		return 0.0;
	}

	/**
	 * Gets the avg active.
	 *
	 * @return the avg active
	 * @see ProfileMonitor#getAvgActive()
	 */
	@Override
	public double getAvgActive() {
		return 0.0;
	}

	/**
	 * Gets the first access.
	 *
	 * @return the first access
	 * @see ProfileMonitor#getFirstAccess()
	 */
	@Override
	public Date getFirstAccess() {
		return (Date) this.firstAccess.clone();
	}

	/**
	 * Gets the last access.
	 *
	 * @return the last access
	 * @see ProfileMonitor#getLastAccess()
	 */
	@Override
	public Date getLastAccess() {
		return (Date) this.lastAccess.clone();
	}

	/**
	 * Gets the max active.
	 *
	 * @return the max active
	 * @see ProfileMonitor#getMaxActive()
	 */
	@Override
	public double getMaxActive() {
		return 0.0;
	}

	/**
	 * Only two monitors of the same instance can be equals. Otherwise the
	 * result is the same as the one of the superclass.
	 *
	 * @param obj
	 *            the obj
	 * @return true, if successful
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof SimpleProfileMonitor)) {
			return false;
		}
		return super.equals(obj);
	}

	/**
	 * Hash code.
	 *
	 * @return the int
	 * @see AbstractProfileMonitor#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.label.hashCode();
	}

}
