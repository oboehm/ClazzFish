/**
 * $Id: JamonMonitor.java,v 1.9 2016/12/18 20:19:36 oboehm Exp $
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
 * (c)reated 25.12.2008 by oliver (ob@oasd.de)
 */
package clazzfish.jdbc.monitor;

import com.jamonapi.ListenerType;
import com.jamonapi.MonKey;
import com.jamonapi.Monitor;
import com.jamonapi.Range;

import java.util.Date;

/**
 * This is a thin wrapper for com.jamonapi.Monitor
 *
 * @author <a href="boehm@javatux.de">oliver</a>
 * @version $Revision: 1.9 $
 * @see com.jamonapi.Monitor
 * @since 25.12.2008
 */
public final class JamonMonitor extends AbstractProfileMonitor {

	private final Monitor monitor;

	/**
	 * Instantiates a new monitor.
	 *
	 * @param monitor
	 *            the monitor
	 */
	public JamonMonitor(final Monitor monitor) {
		this.monitor = monitor;
	}

	/**
	 * Adds the.
	 *
	 * @param arg0
	 *            argument
	 * @see com.jamonapi.Monitor#add(double)
	 */
	@Override
	public void add(final double arg0) {
		monitor.add(arg0);
	}

	/**
	 * Disable.
	 *
	 * @see com.jamonapi.Monitor#disable()
	 */
	public void disable() {
		monitor.disable();
	}

	/**
	 * Enable.
	 *
	 * @see com.jamonapi.Monitor#enable()
	 */
	public void enable() {
		monitor.enable();
	}

	/**
	 * Tests if given object is equal.
	 *
	 * @param obj
	 *            the other object to compare
	 * @return true if objects are equal
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof JamonMonitor)) {
			return false;
		}
		JamonMonitor other = (JamonMonitor) obj;
		return this.monitor.equals(other.monitor);
	}

	/**
	 * Gets the active.
	 *
	 * @return active
	 *
	 * @see com.jamonapi.Monitor#getActive()
	 */
	@Override
	public double getActive() {
		return monitor.getActive();
	}

	/**
	 * Gets the average.
	 *
	 * @return average
	 * @see com.jamonapi.Monitor#getAvg()
	 */
	@Override
	public double getAvg() {
		return monitor.getAvg();
	}

	/**
	 * Gets the active average.
	 *
	 * @return average active
	 * @see com.jamonapi.Monitor#getAvgActive()
	 */
	@Override
	public double getAvgActive() {
		return monitor.getAvgActive();
	}

	/**
	 * Gets the global active average.
	 *
	 * @return average global active
	 * @see com.jamonapi.Monitor#getAvgGlobalActive()
	 */
	public double getAvgGlobalActive() {
		return monitor.getAvgGlobalActive();
	}

	/**
	 * Gets the primary active average.
	 *
	 * @return average primary active
	 *
	 * @see com.jamonapi.Monitor#getAvgPrimaryActive()
	 */
	public double getAvgPrimaryActive() {
		return monitor.getAvgPrimaryActive();
	}

	/**
	 * Gets the first access.
	 *
	 * @return first access
	 *
	 * @see com.jamonapi.Monitor#getFirstAccess()
	 */
	@Override
	public Date getFirstAccess() {
		return monitor.getFirstAccess();
	}

	/**
	 * Gets the hits.
	 *
	 * @return number of hits
	 *
	 * @see com.jamonapi.Monitor#getHits()
	 */
	@Override
	public int getHits() {
		return (int) monitor.getHits();
	}

	/**
	 * Gets the label.
	 *
	 * @return label
	 *
	 * @see com.jamonapi.Monitor#getLabel()
	 */
	@Override
	public String getLabel() {
		return monitor.getLabel();
	}

	/**
	 * Gets the last access.
	 *
	 * @return last access
	 *
	 * @see com.jamonapi.Monitor#getLastAccess()
	 */
	@Override
	public Date getLastAccess() {
		return monitor.getLastAccess();
	}

	/**
	 * Gets the last value.
	 *
	 * @return last value
	 *
	 * @see com.jamonapi.Monitor#getLastValue()
	 */
	@Override
	public double getLastValue() {
		return monitor.getLastValue();
	}

	/**
	 * Gets the listener type.
	 *
	 * @param listenerType
	 *            the listener type
	 *
	 * @return listener type
	 *
	 * @see com.jamonapi.Monitor#getListenerType(String)
	 */
	public ListenerType getListenerType(final String listenerType) {
		return monitor.getListenerType(listenerType);
	}

	/**
	 * Gets the max.
	 *
	 * @return maximum
	 *
	 * @see com.jamonapi.Monitor#getMax()
	 */
	@Override
	public double getMax() {
		return monitor.getMax();
	}

	/**
	 * Gets the max active.
	 *
	 * @return maximal active
	 *
	 * @see com.jamonapi.Monitor#getMaxActive()
	 */
	@Override
	public double getMaxActive() {
		return monitor.getMaxActive();
	}

	/**
	 * Gets the min.
	 *
	 * @return minimum
	 *
	 * @see com.jamonapi.Monitor#getMin()
	 */
	@Override
	public double getMin() {
		return monitor.getMin();
	}

	/**
	 * Gets the monitors.
	 *
	 * @return monitors
	 *
	 * @see com.jamonapi.MonitorComposite#getMonitors()
	 */
	@Override
	public ProfileMonitor[] getMonitors() {
		return new SimpleProfileMonitor[0];
	}

	/**
	 * Gets the mon key.
	 *
	 * @return monitor key
	 *
	 * @see com.jamonapi.Monitor#getMonKey()
	 */
	public MonKey getMonKey() {
		return monitor.getMonKey();
	}

	/**
	 * Gets the range.
	 *
	 * @return range
	 *
	 * @see com.jamonapi.Monitor#getRange()
	 */
	public Range getRange() {
		return monitor.getRange();
	}

	/**
	 * Gets the std dev.
	 *
	 * @return standard dev
	 *
	 * @see com.jamonapi.Monitor#getStdDev()
	 */
	public double getStdDev() {
		return monitor.getStdDev();
	}

	/**
	 * Gets the total.
	 *
	 * @return total
	 *
	 * @see com.jamonapi.Monitor#getTotal()
	 */
	@Override
	public double getTotal() {
		return monitor.getTotal();
	}

	/**
	 * Gets the units.
	 *
	 * @return units
	 *
	 * @see com.jamonapi.Monitor#getUnits()
	 */
	@Override
	public String getUnits() {
		return monitor.getUnits();
	}

	/**
	 * Hash code.
	 *
	 * @return hash code
	 *
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return monitor.hashCode();
	}

	/**
	 * Checks for listeners.
	 *
	 * @return true if object has listeners
	 *
	 * @see com.jamonapi.Monitor#hasListeners()
	 */
	public boolean hasListeners() {
		return monitor.hasListeners();
	}

	/**
	 * Checks if is activity tracking.
	 *
	 * @return activity tracking
	 *
	 * @see com.jamonapi.Monitor#isActivityTracking()
	 */
	public boolean isActivityTracking() {
		return monitor.isActivityTracking();
	}

	/**
	 * Checks if is enabled.
	 *
	 * @return true if enabled
	 *
	 * @see com.jamonapi.Monitor#isEnabled()
	 */
	public boolean isEnabled() {
		return monitor.isEnabled();
	}

	/**
	 * Checks if is primary.
	 *
	 * @return true if primary
	 *
	 * @see com.jamonapi.Monitor#isPrimary()
	 */
	public boolean isPrimary() {
		return monitor.isPrimary();
	}

	/**
	 * Reset.
	 *
	 * @see com.jamonapi.Monitor#reset()
	 */
	@Override
	public void reset() {
		monitor.reset();
	}

	/**
	 * Sets the access stats.
	 *
	 * @param now
	 *            the now
	 *
	 * @see com.jamonapi.Monitor#setAccessStats(long)
	 */
	public void setAccessStats(final long now) {
		monitor.setAccessStats(now);
	}

	/**
	 * Sets the active.
	 *
	 * @param arg0
	 *            the arg0
	 *
	 * @see com.jamonapi.Monitor#setActive(double)
	 */
	public void setActive(final double arg0) {
		monitor.setActive(arg0);
	}

	/**
	 * Sets the activity tracking.
	 *
	 * @param arg0
	 *            the arg0
	 *
	 * @see com.jamonapi.Monitor#setActivityTracking(boolean)
	 */
	public void setActivityTracking(final boolean arg0) {
		monitor.setActivityTracking(arg0);
	}

	/**
	 * Sets the first access.
	 *
	 * @param arg0
	 *            the arg0
	 *
	 * @see com.jamonapi.Monitor#setFirstAccess(Date)
	 */
	public void setFirstAccess(final Date arg0) {
		monitor.setFirstAccess(arg0);
	}

	/**
	 * Sets the hits.
	 *
	 * @param arg0
	 *            the arg0
	 *
	 * @see com.jamonapi.Monitor#setHits(double)
	 */
	public void setHits(final double arg0) {
		monitor.setHits(arg0);
	}

	/**
	 * Sets the last access.
	 *
	 * @param arg0
	 *            the arg0
	 *
	 * @see com.jamonapi.Monitor#setLastAccess(Date)
	 */
	public void setLastAccess(final Date arg0) {
		monitor.setLastAccess(arg0);
	}

	/**
	 * Sets the last value.
	 *
	 * @param arg0
	 *            the arg0
	 *
	 * @see com.jamonapi.Monitor#setLastValue(double)
	 */
	public void setLastValue(final double arg0) {
		monitor.setLastValue(arg0);
	}

	/**
	 * Sets the max.
	 *
	 * @param arg0
	 *            the arg0
	 *
	 * @see com.jamonapi.Monitor#setMax(double)
	 */
	public void setMax(final double arg0) {
		monitor.setMax(arg0);
	}

	/**
	 * Sets the max active.
	 *
	 * @param arg0
	 *            the arg0
	 *
	 * @see com.jamonapi.Monitor#setMaxActive(double)
	 */
	public void setMaxActive(final double arg0) {
		monitor.setMaxActive(arg0);
	}

	/**
	 * Sets the min.
	 *
	 * @param arg0
	 *            the arg0
	 *
	 * @see com.jamonapi.Monitor#setMin(double)
	 */
	public void setMin(final double arg0) {
		monitor.setMin(arg0);
	}

	/**
	 * Sets the primary.
	 *
	 * @param arg0
	 *            the arg0
	 *
	 * @see com.jamonapi.Monitor#setPrimary(boolean)
	 */
	public void setPrimary(final boolean arg0) {
		monitor.setPrimary(arg0);
	}

	/**
	 * Sets the total.
	 *
	 * @param arg0
	 *            the arg0
	 *
	 * @see com.jamonapi.Monitor#setTotal(double)
	 */
	public void setTotal(final double arg0) {
		monitor.setTotal(arg0);
	}

	/**
	 * Sets the total active.
	 *
	 * @param arg0
	 *            the arg0
	 *
	 * @see com.jamonapi.Monitor#setTotalActive(double)
	 */
	public void setTotalActive(final double arg0) {
		monitor.setTotalActive(arg0);
	}

	/**
	 * Start.
	 *
	 * @see com.jamonapi.Monitor#start()
	 */
	@Override
	public void start() {
		monitor.start();
	}

	/**
	 * Stop.
	 *
	 * @see com.jamonapi.Monitor#stop()
	 */
	@Override
	public void stop() {
		monitor.stop();
	}

	/**
	 * To string.
	 *
	 * @return e.g. "JAMon Label=void
	 *         patterntesting.runtime.monitor.ProfileTest.testProfiler(),
	 *         Units=ms.:" + " (LastValue=8.0, Hits=1.0, Avg=8.0, Total=8.0,
	 *         Min=8.0, Max=8.0, Active=1.0, Avg Active=3.0," + " Max
	 *         Active=2.0, First Access=Sat Dec 27 17:31:09 CET 2008, Last
	 *         Access=Sat Dec 27 17:31:09 CET 2008)"
	 *
	 * @see com.jamonapi.Monitor#toString()
	 */
	@Override
	public String toString() {
		return monitor.toString();
	}

	/**
	 * To short string.
	 *
	 * @return the string
	 */
	@Override
	public String toShortString() {
		return "Hits=" + (long) monitor.getHits() + ", Avg=" + monitor.getAvg() + ", Total=" + monitor.getTotal()
				+ ", Max=" + monitor.getMax() + ", Unit=" + monitor.getUnits();
	}

	/**
	 * To csv headline.
	 *
	 * @return the string
	 */
	@Override
	public String toCsvHeadline() {
		return "Label; Unit; LastValue; Hits; Avg; Total; Min; Max; Active; Avg Active; Max Active; First Access; Last Access;";
	}

	/**
	 * To csv string.
	 *
	 * @return the string
	 */
	@Override
	public String toCsvString() {
		return '"' + monitor.getLabel() + "\"; " + monitor.getUnits() + "; " + monitor.getLastValue() + "; "
				+ (long) monitor.getHits() + "; " + monitor.getAvg() + "; " + monitor.getTotal() + "; "
				+ monitor.getMin() + "; " + monitor.getMax() + "; " + monitor.getActive() + "; "
				+ monitor.getAvgActive() + "; " + monitor.getMaxActive() + "; " + monitor.getFirstAccess() + "; "
				+ monitor.getLastAccess() + ";";
	}

}
