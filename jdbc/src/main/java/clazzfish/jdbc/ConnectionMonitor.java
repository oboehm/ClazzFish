/*
 * $Id: ConnectionMonitor.java,v 1.17 2016/12/18 20:19:38 oboehm Exp $
 *
 * Copyright (c) 2012 by Oliver Boehm
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
 * (c)reated 07.10.2012 by oliver (ob@oasd.de)
 */

package clazzfish.jdbc;

import clazzfish.monitor.jmx.MBeanHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.JMException;
import javax.management.openmbean.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This is the monitor class for the {@link ProxyConnection} which monitors the
 * different newInstance() and close() calls. So you can ask this monitor how
 * many connections and which connections are open.
 * <p>
 * The instance of this class is automatically registered as MBean as soon as a
 * {@link ProxyConnection} is used.
 * </p>
 * <p>
 * Originally this class was part of PatternTesting (since 2012) but was moved
 * in 2018 to the ClazzFish project.
 * </p>
 *
 * @author oliver (ob@aosd.de)
 * @since 0.9
 */
public class ConnectionMonitor extends clazzfish.monitor.AbstractMonitor implements ConnectionMonitorMBean {

	private static final Logger LOG = LoggerFactory.getLogger(ConnectionMonitor.class);
	private static final ConnectionMonitor INSTANCE;
	private static final List<ProxyConnection> openConnections = new CopyOnWriteArrayList<>();

	private static int sumOfConnections = 0;

	static {
		INSTANCE = new ConnectionMonitor();
		try {
			MBeanHelper.registerMBean(INSTANCE);
			LOG.debug("{} created and registered as MBean.", INSTANCE);
		} catch (JMException e) {
			LOG.info("{} can't be registered as MBean ({})", INSTANCE, e);
		}
	}

	/**
	 * No need to instantiate it - we provide only some services. The
	 * constructor is protected because it is still used by the (deprecated)
	 * ConnectionMonitor in patterntesting.runtime.db.
	 */
	protected ConnectionMonitor() {
		LOG.trace("New instance of {} created.", ConnectionMonitor.class);
	}

	/**
	 * Yes, it is a Singleton because it offers only some services. So we don't
	 * need the object twice.
	 *
	 * @return the only instance
	 */
	public static ConnectionMonitor getInstance() {
		return INSTANCE;
	}

	/**
	 * If you want to monitor the connection use this method.
	 *
	 * @param connection
	 *            the original connection
	 * @return a proxy for the connection
	 */
	public static Connection getMonitoredConnection(final Connection connection) {
		return ProxyConnection.newInstance(connection);
	}

	/**
	 * Adds the connection to the list of open connections.
	 *
	 * @param proxyConnection
	 *            the proxy connection
	 */
	public static void addConnection(final ProxyConnection proxyConnection) {
		openConnections.add(proxyConnection);
		sumOfConnections++;
	}

	/**
	 * Removes the connection from the list of open connections.
	 *
	 * @param proxyConnection
	 *            the proxy connection
	 */
	public static void removeConnection(final ProxyConnection proxyConnection) {
		openConnections.remove(proxyConnection);
	}

	/**
	 * Gets the caller of the given connection.
	 *
	 * @param connection
	 *            the connection
	 * @return the caller of the connection
	 */
	public static StackTraceElement getCallerOf(final Connection connection) {
		for (ProxyConnection proxy : openConnections) {
			if (proxy.getConnection().equals(connection)) {
				return proxy.getCaller()[0];
			}
		}
		throw new IllegalArgumentException("not monitored or closed: " + connection);
	}

	/**
	 * Gets the callers.
	 *
	 * @return the callers
	 * @see ConnectionMonitorMBean#getCallers()
	 */
	@Override
	public StackTraceElement[] getCallers() {
		StackTraceElement[] callers = new StackTraceElement[openConnections.size()];
		int i = 0;
		for (ProxyConnection proxy : openConnections) {
			callers[i] = proxy.getCaller()[0];
			i++;
		}
		return callers;
	}

	/**
	 * Gets the stacktrace of the caller which opens the last connection.
	 *
	 * @return the all caller
	 */
	@Override
	public StackTraceElement[] getLastCallerStacktrace() {
		if (openConnections.isEmpty()) {
			return new StackTraceElement[0];
		}
		ProxyConnection lastConnection = openConnections.get(openConnections.size() - 1);
		return lastConnection.getCaller();
	}

	/**
	 * Gets the caller stacktraces of all connections.
	 *
	 * @return stacktraces of all callers
	 * @throws OpenDataException
	 *             the open data exception
	 */
	@Override
	public TabularData getCallerStacktraces() throws OpenDataException {
		String[] itemNames = { "Caller", "Stacktrace" };
		String[] itemDescriptions = { "caller name", "stacktrace" };
		try {
			OpenType<?>[] itemTypes = { SimpleType.STRING, new ArrayType<String>(1, SimpleType.STRING) };
			CompositeType rowType = new CompositeType("propertyType", "property entry", itemNames, itemDescriptions,
					itemTypes);
			TabularDataSupport data = MBeanHelper.createTabularDataSupport(rowType, itemNames);
			for (ProxyConnection proxy : openConnections) {
				StackTraceElement[] stacktrace = proxy.getCaller();
				Map<String, Object> map = new HashMap<>();
				map.put("Caller", stacktrace[0].toString());
				map.put("Stacktrace", toStringArray(stacktrace));
				CompositeDataSupport compData = new CompositeDataSupport(rowType, map);
				data.put(compData);
			}
			return data;
		} catch (OpenDataException ex) {
			LOG.warn("Cannot get caller stacktraces of " + openConnections.size() + " open connections.", ex);
			throw ex;
		}
	}

	private static String[] toStringArray(final StackTraceElement[] stacktrace) {
		String[] array = new String[stacktrace.length];
		for (int i = 0; i < stacktrace.length; i++) {
			array[i] = stacktrace[i].toString();
		}
		return array;
	}

	/**
	 * Gets the caller which opens the last connection.
	 *
	 * @return the all caller
	 */
	@Override
	public StackTraceElement getLastCaller() {
		StackTraceElement[] callers = this.getCallers();
		if (callers.length == 0) {
			LOG.debug("No open connections - last caller is null.");
			return null;
		}
		return callers[callers.length - 1];
	}

	/**
	 * Gets the number of open connections.
	 *
	 * @return the open count
	 * @see ConnectionMonitorMBean#getOpenConnections()
	 */
	@Override
	public int getOpenConnections() {
		return ConnectionMonitor.openConnections.size();
	}

	/**
	 * Gets the number of closed connections.
	 *
	 * @return the closed connections
	 * @since 1.4.1
	 */
	@Override
	public int getClosedConnections() {
		return this.getSumOfConnections() - this.getOpenConnections();
	}

	/**
	 * Gets the total sum of open and closed connections.
	 *
	 * @return the sum of connections
	 * @since 1.4.1
	 */
	@Override
	public int getSumOfConnections() {
		return sumOfConnections;
	}

	/**
	 * Assert that all connections are closed.
	 */
	public static void assertConnectionsClosed() {
		int count = INSTANCE.getOpenConnections();
		if (count > 0) {
			AssertionError error = new AssertionError(count + " connection(s) not closed");
			error.setStackTrace(openConnections.iterator().next().getCaller());
			throw error;
		}
	}

	/**
	 * This toString implementation supports logging and debugging by showing
	 * the number of open connections.
	 *
	 * @return the string
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		int n = this.getOpenConnections();
		if (n < 1) {
			return this.getClass().getSimpleName();
		}
		return this.getClass().getSimpleName() + " watching " + n + " connection(s)";
	}

	/**
	 * This operation dumps the different MBean attributes to the given
	 * directory.
	 *
	 * @param dumpDir
	 *            the directory where the attributes are dumped to.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
	public void dumpMe(final File dumpDir) throws IOException {
		super.dumpMe(dumpDir);
		dumpArray(this.getCallers(), dumpDir, "callers");
		dumpArray(this.getLastCallerStacktrace(), dumpDir, "lastCallerStracktrace");
		dumpArray(openConnections.toArray(), dumpDir, "openConnections");
		dumpArray(getCallerStacktraceDumps().toArray(), dumpDir, "callerStacktraces");
		copyResource("ConnectionMonitor.txt", new File(dumpDir, "README.txt"));
	}

	/**
	 * Logs the different array to the log output.
	 */
	@Override
	public void logMe() {
		try {
			StringWriter writer = new StringWriter();
			dumpArray(this.getCallers(), new BufferedWriter(writer), "callers");
			dumpArray(this.getLastCallerStacktrace(), new BufferedWriter(writer), "lastCallerStracktrace");
			dumpArray(openConnections.toArray(), new BufferedWriter(writer), "openConnections");
			dumpArray(getCallerStacktraceDumps().toArray(), new BufferedWriter(writer), "callerStacktraces");
			LOG.info(writer.toString());
		} catch (IOException cannothappen) {
			LOG.warn("Cannot dump resources:", cannothappen);
		}
	}

	/**
	 * Log the caller stacktraces.
	 *
	 * @see ConnectionMonitorMBean#logCallerStacktraces()
	 * @since 1.6.1
	 */
	@Override
	public void logCallerStacktraces() {
		for (String dump : getCallerStacktraceDumps()) {
			LOG.info(dump);
		}
	}

	private static List<String> getCallerStacktraceDumps() {
		List<String> connectionStacktraces = new ArrayList<>();
		for (ProxyConnection proxy : openConnections) {
			StringBuilder buf = new StringBuilder();
			StackTraceElement[] stacktrace = proxy.getCaller();
			for (StackTraceElement element : stacktrace) {
				buf.append("\n\tat ");
				buf.append(element);
			}
			connectionStacktraces.add(proxy + " was called " + buf);
		}
		return connectionStacktraces;
	}

	/**
	 * This method is called when the ConnectionMonitor is registered as
	 * shutdown hook.
	 *
	 * @see Thread#run()
	 */
	@Override
	public void run() {
		super.run();
		LOG.info("---->>>>---->>>>----    {} of {} connection(s) are still open    ---->>>>---->>>>----",
				openConnections.size(), sumOfConnections);
		this.logCallerStacktraces();
		LOG.info("----<<<<----<<<<----    {} of {} connection(s) are still open    ----<<<<----<<<<----",
				openConnections.size(), sumOfConnections);
	}

}
