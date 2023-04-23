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

import clazzfish.jdbc.internal.PasswordFilter;
import clazzfish.jdbc.internal.StasiPreparedStatement;
import clazzfish.jdbc.internal.StasiStatement;
import clazzfish.jdbc.monitor.ProfileMonitor;
import clazzfish.monitor.jmx.MBeanHelper;
import clazzfish.monitor.util.Converter;
import clazzfish.monitor.util.StackTraceScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	protected SqlStatistic() {
		super("SQL");
	}

	/**
	 * To start a new statistic call this method. In contradiction to
	 * {@link AbstractStatistic#reset()} old {@link ProfileMonitor}s will
	 * removed.
	 */
	@Override
	public void reset() {
		synchronized (SqlStatistic.class) {
			this.resetRootMonitor();
		}
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
	 * @param mon         the monitor
	 * @param command     the SQL command
	 * @param returnValue the monitored return value
	 */
	public static void stop(final ProfileMonitor mon, final String command, final Object returnValue) {
		mon.stop();
		if (LOG.isDebugEnabled()) {
			String msg = '"' + PasswordFilter.filter(command) + "\" returned with " + Converter.toShortString(returnValue) + " after "
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
	 */
	public static void addAsShutdownHook() {
		Runtime.getRuntime().addShutdownHook(SQL_INSTANCE);
		LOG.debug("{} is registered as shutdown hook.", SQL_INSTANCE);
	}

	/**
	 * With this method you can register the monitor with your own name. This is
	 * e.g. useful if you have an application server with several applications.
	 * <p>
	 * You can only register the monitor only once. If you want to register it
	 * with another name you have to first unregister it.
	 * </p>
	 *
	 * @param name the MBean name (e.g. "my.class.Monitor")
	 */
	public static void registerAsMBean(final String name) {
		getInstance().registerMeAsMBean(MBeanHelper.getAsObjectName(name));
	}

}
