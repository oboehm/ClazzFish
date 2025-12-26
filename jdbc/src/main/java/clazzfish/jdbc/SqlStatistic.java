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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.Strings.CS;

/**
 * This class monitors and measures SQL statements.
 *
 * @author oliver
 * @since 0.9
 */
public class SqlStatistic extends AbstractStatistic implements SqlStatisticMBean {

	private static final Logger log = LoggerFactory.getLogger(SqlStatistic.class);
	private static final SqlStatistic SQL_INSTANCE;
    private CsvXPorter xPorter;

	static {
		CsvXPorter cxp = normalize(XPorter.createCsvXPorter(Config.DEFAULT.getDumpURI()));
		SQL_INSTANCE = new SqlStatistic(cxp);
	}

    public static SqlStatistic getInstance() {
        return SQL_INSTANCE;
    }

	private static CsvXPorter normalize(CsvXPorter xPorter) {
		URI csvURI = xPorter.getURI();
		if (!csvURI.toString().endsWith(".csv")) {
			csvURI = URI.create(csvURI + "/SqlStatistic.csv");
			xPorter = xPorter.withURI(csvURI);
		}
		return xPorter;
	}

    private SqlStatistic(CsvXPorter xPorter) {
        super("SQL");
        this.xPorter = xPorter;
        log.trace("Statistics will be imported from / exported with \"{}\".", xPorter);
    }

	public CsvXPorter getXPorter() {
		return xPorter;
	}

	public void setXPorter(CsvXPorter xPorter) {
		this.xPorter = normalize(xPorter);
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
		importCSV(xPorter.getURI());
        log.info("Exporting SQL statistic with '{}'...", xPorter);
        xPorter.exportCSV(getCsvLines());
        return xPorter.getURI();
    }

	@Override
	public URI getExportURI() {
		return xPorter.getURI();
	}

	/**
	 * Imports the statistics from the given URI.
	 *
	 * @param csvURI URI where the statistic should be imported from
	 */
	public void importCSV(URI csvURI) {
		try {
			List<String> csvLines = xPorter.importCSV(csvURI);
			if (csvLines.isEmpty()) {
				log.debug("URI \"{}\" is empty and not imported.", csvURI);
				return;
			}
			for (int i = 1; i < csvLines.size(); i++) {
				String line = csvLines.get(i);
				String label = line.split(";")[0];
				label = CS.removeEnd(StringUtils.removeStart(label, '"'), "\"");
				ProfileMonitor mon = getMonitor(label);
				mon.readFromCsv(line);
			}
			log.debug("SQL statistic from {} imported.", csvURI);
		} catch (IOException ex) {
			log.info("URI \"{}\" cannot be imported ({}).", csvURI, ex.getMessage());
			log.debug("Details:", ex);
		}
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "-" + getExportURI();
	}

}
