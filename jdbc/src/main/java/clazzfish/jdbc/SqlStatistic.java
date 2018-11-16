/*
 * $Id: SqlStatistic.java,v 1.12 2016/12/18 20:19:38 oboehm Exp $
 *
 * Copyright (c) 2014 by Oliver Boehm
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
 * (c)reated 16.04.2014 by oliver (ob@oasd.de)
 */

package clazzfish.jdbc;

import clazzfish.jdbc.internal.StasiPreparedStatement;
import clazzfish.jdbc.internal.StasiStatement;
import clazzfish.jdbc.monitor.ProfileMonitor;
import clazzfish.monitor.util.Converter;
import clazzfish.monitor.util.StackTraceScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.openmbean.TabularData;
import java.util.regex.Pattern;

/**
 * This class monitors and measures SQL statements.
 *
 * @author oliver
 * @since 0.9
 */
public class SqlStatistic extends AbstractStatistic implements SqlStatisticMBean {

	private static final Logger LOG = LoggerFactory.getLogger(SqlStatistic.class);
	private static final SqlStatistic SQL_INSTANCE;

	static {
		SQL_INSTANCE = new SqlStatistic();
	}

	/**
	 * Gets the single instance of SqlStatistic.
	 *
	 * @return single instance of SqlStatistic
	 */
	public static SqlStatistic getInstance() {
		return SQL_INSTANCE;
	}

	private SqlStatistic() {
		super("SQL");
	}
	
	/**
	 * Start the monitor for the given SQL statement.
	 *
	 * @param sql the SQL statement to be monitored
	 * @return the started profile monitor
	 */
	public static ProfileMonitor start(final String sql) {
		return SQL_INSTANCE.startProfileMonitorFor(sql.trim());
	}

	/**
	 * Stops the given 'mon' and logs the given command with the needed time if
	 * debug is enabled.
	 *
	 * @param mon
	 *            the mon
	 * @param command
	 *            the command
	 */
	public static void stop(final ProfileMonitor mon, final String command) {
		stop(mon, command, Void.TYPE);
	}

	/**
	 * Stops the given 'mon' and logs the given command with the needed time if
	 * debug is enabled.
	 *
	 * @param mon
	 *            the mon
	 * @param command
	 *            the command
	 * @param returnValue
	 *            the return value
	 */
	public static void stop(final ProfileMonitor mon, final String command, final Object returnValue) {
		mon.stop();
		if (LOG.isDebugEnabled()) {
			String msg = '"' + command + "\" returned with " + Converter.toShortString(returnValue) + " after "
					+ mon.getLastTime();
			if (LOG.isTraceEnabled()) {
				StackTraceElement[] stacktrace = StackTraceScanner.getCallerStackTrace(new Pattern[0],
						SqlStatistic.class, StasiStatement.class, StasiPreparedStatement.class);
				LOG.trace("{}\n\t{}", msg, Converter.toLongString(stacktrace).trim());
			} else {
				LOG.debug("{}.", msg);
			}
		}
	}

	/**
	 * You can register the instance as shutdown hook. If the VM is terminated
	 * the profile values are logged and dumped to a CSV file in the tmp
	 * directory.
	 * 
	 * @param hook the SQL instance which is registered as shutdown hook
	 */
	public static void addAsShutdownHook(SqlStatistic hook) {
		Runtime.getRuntime().addShutdownHook(hook);
		LOG.debug("{} is registered as shutdown hook.", hook);
	}
	
	/**
	 * Gets the max hits.
	 *
	 * @return the max hits
	 */
	@Override
	public int getMaxHits() {
		return 0;
	}

	/**
	 * Gets the max hits label.
	 *
	 * @return the max hits label
	 */
	@Override
	public String getMaxHitsLabel() {
		return null;
	}

	/**
	 * Gets the max hits statistic.
	 *
	 * @return the max hits statistic
	 */
	@Override
	public String getMaxHitsStatistic() {
		return null;
	}

	/**
	 * Gets the max total.
	 *
	 * @return the max total
	 */
	@Override
	public double getMaxTotal() {
		return 0;
	}

	/**
	 * Gets the max total label.
	 *
	 * @return the max total label
	 */
	@Override
	public String getMaxTotalLabel() {
		return null;
	}

	/**
	 * Gets the max total statistic.
	 *
	 * @return the max total statistic
	 */
	@Override
	public String getMaxTotalStatistic() {
		return null;
	}

	/**
	 * Gets the max avg.
	 *
	 * @return the max avg
	 */
	@Override
	public double getMaxAvg() {
		return 0;
	}

	/**
	 * Gets the max avg label.
	 *
	 * @return the max avg label
	 */
	@Override
	public String getMaxAvgLabel() {
		return null;
	}

	/**
	 * Gets the max avg statistic.
	 *
	 * @return the max avg statistic
	 */
	@Override
	public String getMaxAvgStatistic() {
		return null;
	}

	/**
	 * Gets the max max.
	 *
	 * @return the max max
	 */
	@Override
	public double getMaxMax() {
		return 0;
	}

	/**
	 * Gets the max max label.
	 *
	 * @return the max max label
	 */
	@Override
	public String getMaxMaxLabel() {
		return null;
	}

	/**
	 * Gets the max max statistic.
	 *
	 * @return the max max statistic
	 */
	@Override
	public String getMaxMaxStatistic() {
		return null;
	}

	/**
	 * Sets the maximal size of statistic entries.
	 *
	 * @param size the new max size
	 * @since 1.6
	 */
	@Override
	public void setMaxSize(int size) {

	}

	/**
	 * Gets the max size.
	 *
	 * @return the max size
	 * @since 1.6
	 */
	@Override
	public int getMaxSize() {
		return 0;
	}

	/**
	 * Gets the statistics.
	 *
	 * @return the statistics
	 */
	@Override
	public TabularData getStatistics() {
		return null;
	}
}
