/*
 * $Id: ProxyConnection.java,v 1.18 2016/12/18 20:19:38 oboehm Exp $
 *
 * Copyright (c) 2012-2014 by Oliver Boehm
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
 * (c)reated 29.09.2012 by oliver (ob@oasd.de)
 */

package clazzfish.jdbc;

import clazzfish.jdbc.internal.StasiPreparedStatement;
import clazzfish.jdbc.internal.StasiStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This is a dynamic proxy for a JDBC connection which monitors together with
 * the {@link ConnectionMonitor} the different newInstance() and close() call.
 * <p>
 * Note: Since 1.4.2 this class was moved from package
 * "patterntesting.runtime.db" to here.
 * </p>
 *
 * @author oliver (ob@aosd.de)
 * @since 1.3 (29.09.2012)
 */
public class ProxyConnection implements InvocationHandler {

	private static final Logger LOG = LoggerFactory.getLogger(ProxyConnection.class);
	private final Connection connection;
	private final StackTraceElement[] caller;
	private final Collection<StasiStatement> uncommittedStatements = new CopyOnWriteArrayList<>();
	private boolean committed = false;
	private boolean autoCommit = true;

	/**
	 * Creates a new proxy instance for the given connection.
	 *
	 * @param connection
	 *            the connection
	 * @return the connection
	 */
	public static Connection newInstance(final Connection connection) {
		ProxyConnection proxyConnection = new ProxyConnection(connection);
		ConnectionMonitor.addConnection(proxyConnection);
		Class<?>[] interfaces = new Class[] { Connection.class };
		return (Connection) Proxy.newProxyInstance(connection.getClass().getClassLoader(), interfaces, proxyConnection);
	}

	/**
	 * Instantiates a new proxy connection. This constructor is called from
	 * {@link #newInstance(Connection)} which is of no interest for us. We want
	 * to store the real caller so we ignore the {@link ProxyConnection} class
	 * but also the {@link ProxyDriver} class (ProxyDriver also calls this
	 * constructor indirectly) and other non-interesting classes.
	 *
	 * @param connection
	 *            the connection
	 */
	protected ProxyConnection(final Connection connection) {
		this.connection = connection;
		this.caller = StasiStatement.getCallerStacktrace(ProxyConnection.class, ProxyDriver.class,
				ConnectionMonitor.class, DriverManager.class);
		try {
			this.autoCommit = connection.getAutoCommit();
		} catch (SQLException sex) {
			LOG.debug("Cannot decide if auto-commit is on:", sex);
		}
	}

	/**
	 * Invokes the orginal {@link Connection} method and puts a wrapper around
	 * {@link Statement} and {@link PreparedStatement} to support monitoring.
	 * <p>
	 * TODO: CallableStatement is not yet wrapped.
	 * </p>
	 *
	 * @param proxy
	 *            the proxy
	 * @param method
	 *            the method
	 * @param args
	 *            the args
	 * @return the object
	 * @throws Throwable
	 *             the throwable
	 * @see InvocationHandler#invoke(Object, Method, Object[])
	 */
	@Override
	public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
		LOG.trace("Call of {} is delegated to {}.", proxy, connection);
		String methodName = method.getName();
		try {
			if ("createStatement".equals(methodName)) {
				Statement stmt = (Statement) method.invoke(connection, args);
				return save(new StasiStatement(stmt));
			} else if ("prepareStatement".equals(methodName)) {
				PreparedStatement stmt = (PreparedStatement) method.invoke(connection, args);
				return save(new StasiPreparedStatement(stmt, args));
			} else if ("toString".equals(methodName)) {
				return this.toString();
			}
			return invoke(method, args);
		} catch (InvocationTargetException ex) {
			Throwable cause = ex.getTargetException();
			LOG.trace("Cannot invoke {}:", method, ex);
			throw cause;
		}
	}

	private Object invoke(final Method method, final Object[] args) throws Throwable {
		String methodName = method.getName();
		if ("close".equals(methodName)) {
			this.close();
		} else if ("setAutoCommit".equals(methodName)) {
			this.autoCommit = (Boolean) args[0];
		} else if (isCommitMethod(methodName)) {
			commit();
		}
		return method.invoke(connection, args);
	}

	private void commit() {
		this.committed = true;
		this.uncommittedStatements.clear();
	}

	private boolean isCommitMethod(String methodName) {
		return "commit".equals(methodName) || "rollback".equals(methodName);
	}

	private void close() {
		ConnectionMonitor.removeConnection(this);
		for (StasiStatement stmt : this.uncommittedStatements) {
			int updateCount = stmt.getUpdateCount();
			if ((updateCount > 0) && !this.isCommitted()) {
				LOG.warn("{} entries were updated with '{}' but not committed.", updateCount, stmt);
			}
		}
		LOG.trace("'{}' is closed, {} statement(s) will be freed.", this, this.uncommittedStatements.size());
		this.uncommittedStatements.clear();
	}

	private Statement save(StasiStatement stmt) {
		if (!this.autoCommit) {
			this.uncommittedStatements.add(stmt);
			LOG.trace("{} is added to {}.", stmt, this);
		}
		return stmt;
	}

	private boolean isCommitted() {
		return this.autoCommit || this.committed;
	}

	/**
	 * Gets the wrapped connection.
	 *
	 * @return the connection
	 */
	public Connection getConnection() {
		return this.connection;
	}

	/**
	 * Gets the stacktrace of the caller.
	 *
	 * @return the caller stacktrace
	 */
	public StackTraceElement[] getCaller() {
		return this.caller;
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
