/*
 * $Id: StasiStatement.java,v 1.13 2017/11/09 20:34:51 oboehm Exp $
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
 * (c)reated 16.03.2014 by oliver (ob@oasd.de)
 */

package clazzfish.jdbc.internal;

import clazzfish.jdbc.ProxyConnection;
import clazzfish.jdbc.SqlStatistic;
import clazzfish.jdbc.monitor.ProfileMonitor;
import clazzfish.monitor.log.LogWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Arrays;

/**
 * A simple wrapper for {@link Statement} to be able to find resource problems
 * while reading and writing to the database. It allows us also to measure times
 * of SQL statements.
 * <p>
 * Why the name "Stasi..."? The Stasi was the official state security service of
 * Eastern Germany which controls the people (like NSA in the U.S. or KGB in
 * Russia, see also <a href="http://en.wikipedia.org/wiki/Stasi">Wikipedia</a>).
 * The StasiStatement controls the embedded {@link Statement} - therefore the
 * name.
 * </p>
 *
 * @author oliver
 * @version $Revision: 1.13 $
 * @since 1.4.1 (16.03.2014)
 */
public class StasiStatement implements Statement {

	private static final Logger LOG = LoggerFactory.getLogger(StasiStatement.class);
	private final LogWatch logWatch = new LogWatch();
	private final Statement statement;
	private final StackTraceElement[] caller;
	private int updateCount = 0;

	/**
	 * Instantiates a new proxy statement.
	 *
	 * @param statement
	 *            the statement
	 */
	public StasiStatement(final Statement statement) {
		this.statement = statement;
		this.caller = getCallerStacktrace(ProxyConnection.class);
	}

	/**
	 * Gets the caller stacktrace. To find the real caller we ignore the first 3
	 * elements from the stacktrace because this is e.g. the method
	 * {@link Thread#getStackTrace()} which is not relevant here.
	 *
	 * @param ignoredClasses
	 *            the ignored classes
	 * @return the caller stacktrace
	 */
	public static StackTraceElement[] getCallerStacktrace(final Class<?>... ignoredClasses) {
		StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
		for (int i = 3; i < stacktrace.length; i++) {
			String classname = stacktrace[i].getClassName();
			if (!(classname.startsWith("com.sun.proxy.") || matches(classname, ignoredClasses))) {
				StackTraceElement[] stacktraceCaller = new StackTraceElement[stacktrace.length - i];
				System.arraycopy(stacktrace, i, stacktraceCaller, 0, stacktrace.length - i);
				return stacktraceCaller;
			}
		}
		throw new IllegalStateException("no caller found for " + Arrays.toString(ignoredClasses));
	}

	private static boolean matches(final String classname, final Class<?>... classes) {
		for (int i = 0; i < classes.length; i++) {
			if (classname.equals(classes[i].getName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * If an entry was updated with one of the executeUpdate methods this method
	 * returns the number of updated entries. Otherwise 0,
	 *
	 * @return the number of updated entries (or 0)
	 * @since 1.6.3
	 */
	public int getUpdated() {
		return this.updateCount;
	}

	/**
	 * Gets the statement.
	 *
	 * @return the statement
	 */
	protected final Statement getStatement() {
		return this.statement;
	}

	/**
	 * Adds the batch.
	 *
	 * @param sql the sql
	 * @throws SQLException the SQL exception
	 * @see Statement#addBatch(String)
	 */
	@Override
	public final void addBatch(final String sql) throws SQLException {
		ProfileMonitor mon = SqlStatistic.start(sql);
		try {
			statement.addBatch(sql);
			SqlStatistic.stop(mon, sql);
		} catch (SQLException ex) {
			throw enrichedSQLException(mon, sql, ex);
		}
	}

	/**
	 * Cancel.
	 *
	 * @throws SQLException
	 *             the sQL exception
	 * @see Statement#cancel()
	 */
	@Override
	public final void cancel() throws SQLException {
		statement.cancel();
		LOG.trace("{} cancelled.", statement);
	}

	/**
	 * Clear batch.
	 *
	 * @throws SQLException
	 *             the sQL exception
	 * @see Statement#clearBatch()
	 */
	@Override
	public final void clearBatch() throws SQLException {
		statement.clearBatch();
		LOG.trace("Batch cleared.");
	}

	/**
	 * Clear warnings.
	 *
	 * @throws SQLException
	 *             the sQL exception
	 * @see Statement#clearWarnings()
	 */
	@Override
	public final void clearWarnings() throws SQLException {
		statement.clearWarnings();
		LOG.trace("Warnings cleared.");
	}

	/**
	 * Close.
	 *
	 * @throws SQLException
	 *             the sQL exception
	 * @see Statement#close()
	 */
	@Override
	public void close() throws SQLException {
		this.statement.close();
		LOG.debug("Statement {} was closed after {}.", this.statement, this.logWatch);
	}

	/**
	 * Execute.
	 *
	 * @param sql
	 *            the sql
	 * @param autoGeneratedKeys
	 *            the auto generated keys
	 * @return true, if successful
	 * @throws SQLException
	 *             the sQL exception
	 * @see Statement#execute(String, int)
	 */
	@Override
	public final boolean execute(final String sql, final int autoGeneratedKeys) throws SQLException {
		ProfileMonitor mon = SqlStatistic.start(sql);
		try {
			boolean ok = statement.execute(sql, autoGeneratedKeys);
			SqlStatistic.stop(mon, sql, ok);
			return ok;
		} catch (SQLException ex) {
			throw enrichedSQLException(mon, sql, ex);
		}
	}

	/**
	 * Execute.
	 *
	 * @param sql
	 *            the sql
	 * @param columnIndexes
	 *            the column indexes
	 * @return true, if successful
	 * @throws SQLException
	 *             the sQL exception
	 * @see Statement#execute(String, int[])
	 */
	@Override
	public final boolean execute(final String sql, final int[] columnIndexes) throws SQLException {
		ProfileMonitor mon = SqlStatistic.start(sql);
		try {
			boolean ok = statement.execute(sql, columnIndexes);
			SqlStatistic.stop(mon, sql, ok);
			return ok;
		} catch (SQLException ex) {
			throw enrichedSQLException(mon, sql, ex);
		}
	}

	/**
	 * Execute.
	 *
	 * @param sql
	 *            the sql
	 * @param columnNames
	 *            the column names
	 * @return true, if successful
	 * @throws SQLException
	 *             the sQL exception
	 * @see Statement#execute(String, String[])
	 */
	@Override
	public final boolean execute(final String sql, final String[] columnNames) throws SQLException {
		ProfileMonitor mon = SqlStatistic.start(sql);
		try {
			boolean ok = statement.execute(sql, columnNames);
			SqlStatistic.stop(mon, sql, ok);
			return ok;
		} catch (SQLException ex) {
			throw enrichedSQLException(mon, sql, ex);
		}
	}

	/**
	 * Execute.
	 *
	 * @param sql
	 *            the sql
	 * @return true, if successful
	 * @throws SQLException
	 *             the sQL exception
	 * @see Statement#execute(String)
	 */
	@Override
	public final boolean execute(final String sql) throws SQLException {
		ProfileMonitor mon = SqlStatistic.start(sql);
		try {
			boolean ok = statement.execute(sql);
			SqlStatistic.stop(mon, sql, ok);
			return ok;
		} catch (SQLException ex) {
			throw enrichedSQLException(mon, sql, ex);
		}
	}

	/**
	 * Execute batch.
	 *
	 * @return the int[]
	 * @throws SQLException
	 *             the sQL exception
	 * @see Statement#executeBatch()
	 */
	@Override
	public final int[] executeBatch() throws SQLException {
		LogWatch watch = new LogWatch();
		int[] ret = statement.executeBatch();
		if (LOG.isDebugEnabled()) {
			LOG.debug("Batch execution returns with {} after {}.", Arrays.toString(ret), watch);
		}
		return ret;
	}

	/**
	 * Execute query.
	 *
	 * @param sql
	 *            the sql
	 * @return the result set
	 * @throws SQLException
	 *             the sQL exception
	 * @see Statement#executeQuery(String)
	 */
	@Override
	public final ResultSet executeQuery(final String sql) throws SQLException {
		ProfileMonitor mon = SqlStatistic.start(sql);
		try {
			ResultSet rs = new StasiResultSet(statement.executeQuery(sql));
			SqlStatistic.stop(mon, sql, rs);
			return rs;
		} catch (SQLException ex) {
			throw enrichedSQLException(mon, sql, ex);
		}
	}

	/**
	 * Execute update.
	 *
	 * @param sql
	 *            the sql
	 * @param autoGeneratedKeys
	 *            the auto generated keys
	 * @return the int
	 * @throws SQLException
	 *             the sQL exception
	 * @see Statement#executeUpdate(String, int)
	 */
	@Override
	public final int executeUpdate(final String sql, final int autoGeneratedKeys) throws SQLException {
		ProfileMonitor mon = SqlStatistic.start(sql);
		try {
			int ret = statement.executeUpdate(sql, autoGeneratedKeys);
			return saveUpdateStatistic(mon, sql, ret);
		} catch (SQLException ex) {
			throw enrichedSQLException(mon, sql, ex);
		}
	}

	/**
	 * Execute update.
	 *
	 * @param sql
	 *            the sql
	 * @param columnIndexes
	 *            the column indexes
	 * @return the int
	 * @throws SQLException
	 *             the sQL exception
	 * @see Statement#executeUpdate(String, int[])
	 */
	@Override
	public final int executeUpdate(final String sql, final int[] columnIndexes) throws SQLException {
		ProfileMonitor mon = SqlStatistic.start(sql);
		try {
			int ret = statement.executeUpdate(sql, columnIndexes);
			return saveUpdateStatistic(mon, sql, ret);
		} catch (SQLException ex) {
			throw enrichedSQLException(mon, sql, ex);
		}
	}

	/**
	 * Execute update.
	 *
	 * @param sql
	 *            the sql
	 * @param columnNames
	 *            the column names
	 * @return the int
	 * @throws SQLException
	 *             the sQL exception
	 * @see Statement#executeUpdate(String,
	 *      String[])
	 */
	@Override
	public final int executeUpdate(final String sql, final String[] columnNames) throws SQLException {
		ProfileMonitor mon = SqlStatistic.start(sql);
		try {
			int ret = statement.executeUpdate(sql, columnNames);
			return saveUpdateStatistic(mon, sql, ret);
		} catch (SQLException ex) {
			throw enrichedSQLException(mon, sql, ex);
		}
	}

	/**
	 * Execute update.
	 *
	 * @param sql
	 *            the sql
	 * @return the int
	 * @throws SQLException
	 *             the sQL exception
	 * @see Statement#executeUpdate(String)
	 */
	@Override
	public final int executeUpdate(final String sql) throws SQLException {
		ProfileMonitor mon = SqlStatistic.start(sql);
		try {
			int ret = statement.executeUpdate(sql);
			return saveUpdateStatistic(mon, sql, ret);
		} catch (SQLException ex) {
			throw enrichedSQLException(mon, sql, ex);
		}
	}

	/**
	 * Save update statistic.
	 *
	 * @param mon
	 *            the monitor for the statistic
	 * @param sql
	 *            the sql
	 * @param rc
	 *            the return code (= number of updated entries)
	 * @return the same as rc
	 */
	protected int saveUpdateStatistic(ProfileMonitor mon, final String sql, int rc) {
		SqlStatistic.stop(mon, sql, rc);
		updateCount += rc;
		return rc;
	}

	/**
	 * For better error analysis the original {@link SQLException} will be
	 * enriched with some additional infos.
	 *
	 * @param mon
	 *            the mon
	 * @param sql
	 *            the sql
	 * @param original
	 *            the original
	 * @return the sQL exception
	 */
	protected SQLException enrichedSQLException(final ProfileMonitor mon, final String sql,
			final SQLException original) {
		mon.stop();
		return new SQLException("SQL \"" + sql + "\" failed after " + mon.getLastTime(), original);
	}

	/**
	 * Gets the connection.
	 *
	 * @return the connection
	 * @throws SQLException
	 *             the sQL exception
	 * @see Statement#getConnection()
	 */
	@Override
	public final Connection getConnection() throws SQLException {
		return statement.getConnection();
	}

	/**
	 * Gets the fetch direction.
	 *
	 * @return the fetch direction
	 * @throws SQLException
	 *             the sQL exception
	 * @see Statement#getFetchDirection()
	 */
	@Override
	public final int getFetchDirection() throws SQLException {
		return statement.getFetchDirection();
	}

	/**
	 * Gets the fetch size.
	 *
	 * @return the fetch size
	 * @throws SQLException
	 *             the sQL exception
	 * @see Statement#getFetchSize()
	 */
	@Override
	public final int getFetchSize() throws SQLException {
		return statement.getFetchSize();
	}

	/**
	 * Gets the generated keys.
	 *
	 * @return the generated keys
	 * @throws SQLException
	 *             the sQL exception
	 * @see Statement#getGeneratedKeys()
	 */
	@Override
	public final ResultSet getGeneratedKeys() throws SQLException {
		return new StasiResultSet(statement.getGeneratedKeys());
	}

	/**
	 * Gets the max field size.
	 *
	 * @return the max field size
	 * @throws SQLException
	 *             the sQL exception
	 * @see Statement#getMaxFieldSize()
	 */
	@Override
	public final int getMaxFieldSize() throws SQLException {
		return statement.getMaxFieldSize();
	}

	/**
	 * Gets the max rows.
	 *
	 * @return the max rows
	 * @throws SQLException
	 *             the sQL exception
	 * @see Statement#getMaxRows()
	 */
	@Override
	public final int getMaxRows() throws SQLException {
		return statement.getMaxRows();
	}

	/**
	 * Gets the more results.
	 *
	 * @return the more results
	 * @throws SQLException
	 *             the sQL exception
	 * @see Statement#getMoreResults()
	 */
	@Override
	public final boolean getMoreResults() throws SQLException {
		return statement.getMoreResults();
	}

	/**
	 * Gets the more results.
	 *
	 * @param current
	 *            the current
	 * @return the more results
	 * @throws SQLException
	 *             the sQL exception
	 * @see Statement#getMoreResults(int)
	 */
	@Override
	public final boolean getMoreResults(final int current) throws SQLException {
		return statement.getMoreResults(current);
	}

	/**
	 * Gets the query timeout.
	 *
	 * @return the query timeout
	 * @throws SQLException
	 *             the sQL exception
	 * @see Statement#getQueryTimeout()
	 */
	@Override
	public final int getQueryTimeout() throws SQLException {
		return statement.getQueryTimeout();
	}

	/**
	 * Gets the result set.
	 *
	 * @return the result set
	 * @throws SQLException
	 *             the sQL exception
	 * @see Statement#getResultSet()
	 */
	@Override
	public final ResultSet getResultSet() throws SQLException {
		return new StasiResultSet(statement.getResultSet());
	}

	/**
	 * Gets the result set concurrency.
	 *
	 * @return the result set concurrency
	 * @throws SQLException
	 *             the sQL exception
	 * @see Statement#getResultSetConcurrency()
	 */
	@Override
	public final int getResultSetConcurrency() throws SQLException {
		return statement.getResultSetConcurrency();
	}

	/**
	 * Gets the result set holdability.
	 *
	 * @return the result set holdability
	 * @throws SQLException
	 *             the sQL exception
	 * @see Statement#getResultSetHoldability()
	 */
	@Override
	public final int getResultSetHoldability() throws SQLException {
		return statement.getResultSetHoldability();
	}

	/**
	 * Gets the result set type.
	 *
	 * @return the result set type
	 * @throws SQLException
	 *             the sQL exception
	 * @see Statement#getResultSetType()
	 */
	@Override
	public final int getResultSetType() throws SQLException {
		return statement.getResultSetType();
	}

	/**
	 * Gets the update count. This method works always - wether the statemnt was
	 * closed or not.
	 *
	 * @return the update count
	 * @see Statement#getUpdateCount()
	 */
	@Override
	public final int getUpdateCount() {
		try {
			return this.statement.getUpdateCount();
		} catch (SQLException sex) {
			LOG.debug("Returning {} as update count because original call failed ({}).", this.updateCount,
					sex.getMessage());
			LOG.trace("Details:", sex);
			return this.updateCount;
		}
	}

	/**
	 * Gets the warnings.
	 *
	 * @return the warnings
	 * @throws SQLException
	 *             the sQL exception
	 * @see Statement#getWarnings()
	 */
	@Override
	public final SQLWarning getWarnings() throws SQLException {
		return statement.getWarnings();
	}

	/**
	 * Checks if is closed.
	 *
	 * @return true, if is closed
	 * @throws SQLException
	 *             the sQL exception
	 * @see Statement#isClosed()
	 */
	@Override
	public final boolean isClosed() throws SQLException {
		return statement.isClosed();
	}

	/**
	 * Checks if is poolable.
	 *
	 * @return true, if is poolable
	 * @throws SQLException
	 *             the sQL exception
	 * @see Statement#isPoolable()
	 */
	@Override
	public final boolean isPoolable() throws SQLException {
		return statement.isPoolable();
	}

	/**
	 * Checks if is wrapper for.
	 *
	 * @param arg0
	 *            the arg0
	 * @return true, if is wrapper for
	 * @throws SQLException
	 *             the sQL exception
	 * @see Wrapper#isWrapperFor(Class)
	 */
	@Override
	public final boolean isWrapperFor(final Class<?> arg0) throws SQLException {
		return statement.isWrapperFor(arg0);
	}

	/**
	 * Sets the cursor name.
	 *
	 * @param name
	 *            the new cursor name
	 * @throws SQLException
	 *             the sQL exception
	 * @see Statement#setCursorName(String)
	 */
	@Override
	public final void setCursorName(final String name) throws SQLException {
		statement.setCursorName(name);
	}

	/**
	 * Sets the escape processing.
	 *
	 * @param enable
	 *            the new escape processing
	 * @throws SQLException
	 *             the sQL exception
	 * @see Statement#setEscapeProcessing(boolean)
	 */
	@Override
	public final void setEscapeProcessing(final boolean enable) throws SQLException {
		statement.setEscapeProcessing(enable);
	}

	/**
	 * Sets the fetch direction.
	 *
	 * @param direction
	 *            the new fetch direction
	 * @throws SQLException
	 *             the sQL exception
	 * @see Statement#setFetchDirection(int)
	 */
	@Override
	public final void setFetchDirection(final int direction) throws SQLException {
		statement.setFetchDirection(direction);
	}

	/**
	 * Sets the fetch size.
	 *
	 * @param rows
	 *            the new fetch size
	 * @throws SQLException
	 *             the sQL exception
	 * @see Statement#setFetchSize(int)
	 */
	@Override
	public final void setFetchSize(final int rows) throws SQLException {
		statement.setFetchSize(rows);
	}

	/**
	 * Sets the max field size.
	 *
	 * @param max
	 *            the new max field size
	 * @throws SQLException
	 *             the sQL exception
	 * @see Statement#setMaxFieldSize(int)
	 */
	@Override
	public final void setMaxFieldSize(final int max) throws SQLException {
		statement.setMaxFieldSize(max);
	}

	/**
	 * Sets the max rows.
	 *
	 * @param max
	 *            the new max rows
	 * @throws SQLException
	 *             the sQL exception
	 * @see Statement#setMaxRows(int)
	 */
	@Override
	public final void setMaxRows(final int max) throws SQLException {
		statement.setMaxRows(max);
	}

	/**
	 * Sets the poolable.
	 *
	 * @param poolable
	 *            the new poolable
	 * @throws SQLException
	 *             the sQL exception
	 * @see Statement#setPoolable(boolean)
	 */
	@Override
	public final void setPoolable(final boolean poolable) throws SQLException {
		statement.setPoolable(poolable);
	}

	/**
	 * Sets the query timeout.
	 *
	 * @param seconds
	 *            the new query timeout
	 * @throws SQLException
	 *             the sQL exception
	 * @see Statement#setQueryTimeout(int)
	 */
	@Override
	public final void setQueryTimeout(final int seconds) throws SQLException {
		statement.setQueryTimeout(seconds);
	}

	/**
	 * Unwrap.
	 *
	 * @param <T>
	 *            the generic type
	 * @param statementClass
	 *            the unwrapped statement class
	 * @return the t
	 * @throws SQLException
	 *             the sQL exception
	 * @see Wrapper#unwrap(Class)
	 */
	@Override
	public final <T> T unwrap(final Class<T> statementClass) throws SQLException {
		return statement.unwrap(statementClass);
	}

	/**
	 * Close on completion. This method is not required for Java 6 and below but
	 * for Java 7. To be compatible with Java 6 this method has no
	 * implementation.
	 *
	 * @throws SQLException
	 *             the SQL exception
	 * @since Java 7
	 */
	@Override
	public final void closeOnCompletion() throws SQLException {
		throw new UnsupportedOperationException("not yet implemented");
	}

	/**
	 * Checks if is close on completion. This method is not required for Java 6
	 * and below but for Java 7. To be compatible with Java 6 this method has no
	 * implementation.
	 *
	 * @return true, if is close on completion
	 * @throws SQLException
	 *             the SQL exception
	 * @since Java 7
	 */
	@Override
	public final boolean isCloseOnCompletion() throws SQLException {
		throw new UnsupportedOperationException("not yet implemented");
	}

	/**
	 * To string.
	 *
	 * @return the string
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " for " + this.caller[0];
	}

}
