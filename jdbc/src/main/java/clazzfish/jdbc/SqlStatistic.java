/*
 * Copyright (c) 2014-2025 by Oliver Boehm
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

import clazzfish.core.Config;
import clazzfish.core.spi.CsvXPorter;
import clazzfish.jdbc.internal.PasswordFilter;
import clazzfish.jdbc.internal.StasiPreparedStatement;
import clazzfish.jdbc.internal.StasiStatement;
import clazzfish.jdbc.monitor.ProfileMonitor;
import clazzfish.monitor.spi.XPorter;
import clazzfish.monitor.util.Converter;
import clazzfish.monitor.util.StackTraceScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * This class monitors and measures SQL statements.
 *
 * @author oliver
 * @since 0.9
 */
public class SqlStatistic extends AbstractStatistic implements SqlStatisticMBean {

	private static final Logger log = LoggerFactory.getLogger(SqlStatistic.class);
    private static final Map<CsvXPorter, SqlStatistic> INSTANCES = new ConcurrentHashMap<>();
	private static SqlStatistic SQL_INSTANCE;
    private final URI csvURI;
    private final CsvXPorter xPorter;

    private static SqlStatistic getInstance() {
        if (SQL_INSTANCE == null) {
            SQL_INSTANCE = SqlStatistic.of(Config.DEFAULT.getDumpURI());
        }
        return SQL_INSTANCE;
    }

    public static SqlStatistic of(URI csvURI) {
        String c = "ClazzStatistic.csv";
        String s = csvURI.toString();
        if (s.endsWith(c)) {
            csvURI = URI.create(s.substring(0, s.length() - c.length()) + "SqlStatistic.csv");
            log.info("DumpURI is changed from '{}' to '{}'.", s, csvURI);
        } else if (!s.endsWith(".csv")) {
            csvURI = URI.create(s + "/SqlStatistic.csv");
            log.info("SQL statistic will be dumped to '{}'.", csvURI);
        }
        return of(XPorter.createCsvXPorter(csvURI));
    }

    public static SqlStatistic of(CsvXPorter xPorter) {
        return INSTANCES.computeIfAbsent(xPorter, uri -> of(xPorter.getURI(), xPorter));
    }

    private static SqlStatistic of(URI csvURI, CsvXPorter xPorter) {
        return new SqlStatistic(csvURI, xPorter);
    }

    private SqlStatistic(URI csvURI, CsvXPorter xPorter) {
        super("SQL");
        this.xPorter = xPorter;
        this.csvURI = csvURI;
        log.trace("Statistics will be imported from / exported to \"{}\".", csvURI);
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
		return getInstance().startProfileMonitorFor(sql.trim());
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
		if (log.isDebugEnabled()) {
			String msg = '"' + PasswordFilter.filter(command) + "\" returned with " + Converter.toShortString(returnValue) + " after "
					+ mon.getLastTime();
			if (log.isTraceEnabled()) {
				StackTraceElement[] stacktrace = StackTraceScanner.getCallerStackTrace(new Pattern[0],
						SqlStatistic.class, StasiStatement.class, StasiPreparedStatement.class);
				log.trace("{}\n\t{}", msg, Converter.toLongString(stacktrace).trim());
			} else {
				log.debug("{}.", msg);
			}
		}
	}

    /**
     * Exports the SQL statistic as CSV.
     *
     * @return the URI where the statistic is exported
     * @throws IOException in case of IO problems
     * @since 3.0
     */
    @Override
    public URI exportCSV() throws IOException {
        log.info("Exporting SQL statistic to '{}'...", csvURI);
        xPorter.exportCSV(getCsvLines());
        return xPorter.getURI();
    }

}
