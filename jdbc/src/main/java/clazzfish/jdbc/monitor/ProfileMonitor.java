package clazzfish.jdbc.monitor;

import java.util.Date;

/**
 * The Interface ProfileMonitor.
 */
public interface ProfileMonitor extends Comparable<ProfileMonitor> {

	/**
	 * Gets the monitors.
	 *
	 * @return the monitors
	 */
	ProfileMonitor[] getMonitors();

	/**
	 * Start.
	 */
	void start();

	/**
	 * Stop.
	 */
	void stop();

	/**
	 * Reset.
	 */
	void reset();

	/**
	 * Gets the label.
	 *
	 * @return the label
	 */
	String getLabel();

	/**
	 * Normally this method should be synchronized. But we waive it with the
	 * risk that the measured time may be slightly wrong.
	 *
	 * @param value
	 *            the value
	 */
	void add(double value);

	/**
	 * Gets the total.
	 *
	 * @return the total
	 */
	double getTotal();

	/**
	 * Gets the last value.
	 *
	 * @return the last value (in ms)
	 */
	double getLastValue();

	/**
	 * Gets the last value as time string. It should return the same result as
	 * {@link #getLastValue()} but in a human readable format.
	 *
	 * @return the last time (e.g. "42 seconds")
	 * @since 1.4.2
	 */
	String getLastTime();

	/**
	 * Gets the max.
	 *
	 * @return the max
	 */
	double getMax();

	/**
	 * Gets the min.
	 *
	 * @return the min
	 */
	double getMin();

	/**
	 * Gets the hits.
	 *
	 * @return the hits
	 */
	int getHits();

	/**
	 * Gets the avg.
	 *
	 * @return the avg
	 */
	double getAvg();

	/**
	 * To short string.
	 *
	 * @return the string
	 */
	String toShortString();

	/**
	 * To csv string.
	 *
	 * @return the string
	 */
	String toCsvString();

	/**
	 * To csv headline.
	 *
	 * @return the string
	 */
	String toCsvHeadline();

	/**
	 * Gets the active.
	 *
	 * @return the active
	 */
	double getActive();

	/**
	 * Gets the avg active.
	 *
	 * @return the avg active
	 */
	double getAvgActive();

	/**
	 * Gets the max active.
	 *
	 * @return the max active
	 */
	double getMaxActive();

	/**
	 * Gets the first access.
	 *
	 * @return the first access
	 */
	Date getFirstAccess();

	/**
	 * Gets the last access.
	 *
	 * @return the last access
	 */
	Date getLastAccess();

	/**
	 * Gets the units.
	 *
	 * @return the units
	 */
	String getUnits();

}
