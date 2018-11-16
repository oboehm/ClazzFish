package clazzfish.jdbc;

import clazzfish.monitor.AbstractMonitorMBean;
import clazzfish.monitor.jmx.Description;

import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.TabularData;

/**
 * This is the interface of {@link ConnectionMonitor} for the use of this class
 * as MBean. You can monitor DB connections with it and ask the class for open
 * connections.
 * <p>
 * Note: Since 1.4.2 this class was moved from package
 * "patterntesting.runtime.db" to here.
 * </p>
 *
 * @author oliver (ob@aosd.de)
 * @version $Revision: 1.10 $
 * @since 1.3 (02-Jan-2013)
 */
public interface ConnectionMonitorMBean extends AbstractMonitorMBean {

	/**
	 * Gets the caller of all connections.
	 *
	 * @return all callers
	 */
	@Description("get the caller of the open connections")
	StackTraceElement[] getCallers();

	/**
	 * Gets the caller stacktraces of all connections.
	 *
	 * @return stacktraces of all callers
	 * @throws OpenDataException
	 *             the open data exception
	 */
	@Description("get the caller of the open connections")
	TabularData getCallerStacktraces() throws OpenDataException;

	/**
	 * Gets the caller which opens the last connection.
	 *
	 * @return the all caller
	 */
	@Description("get the caller of the last open connection")
	StackTraceElement getLastCaller();

	/**
	 * Gets the stacktrace of the caller which opens the last connection.
	 *
	 * @return the all caller
	 */
	@Description("get the call stacktrace of the last open connection")
	StackTraceElement[] getLastCallerStacktrace();

	/**
	 * Gets the number of open connections.
	 * <p>
	 * Note: Till 1.4.0 this method was named "getCount()".
	 * </p>
	 *
	 * @return the number of open connections
	 */
	@Description("get the number of open connections")
	int getOpenConnections();

	/**
	 * Gets the number of closed connections.
	 *
	 * @return the closed connections
	 * @since 1.4.1
	 */
	@Description("get the number of closed connections")
	int getClosedConnections();

	/**
	 * Gets the total sum of open and closed connections.
	 *
	 * @return the sum of connections
	 * @since 1.4.1
	 */
	@Description("get the total sum of open and closed connections")
	int getSumOfConnections();

	/**
	 * Log the caller stacktraces.
	 *
	 * @since 1.6.1
	 */
	@Description("log the caller stacktraces")
	void logCallerStacktraces();

	/**
	 * To be able to register the instance as shutdown hook you can use this
	 * (non static) method.
	 *
	 * @since 1.6.1
	 */
	@Override
	@Description("to register ConnectionMonitor as shutdown hook")
	void addMeAsShutdownHook();

	/**
	 * If you want to unregister the instance as shutdown hook you can use this
	 * method.
	 *
	 * @since 1.6.1
	 */
	@Override
	@Description("to de-register ConnectionMonitor as shutdown hook")
	void removeMeAsShutdownHook();

	/**
	 * Here you can ask if the ConnectionMonitor was already registeres ad
	 * shutdown hook.
	 *
	 * @return true if it is registered as shutdown hook.
	 * @since 1.6.1
	 */
	@Override
	@Description("returns true if ConnectionMonitor was registered as shutdown hook")
	boolean isShutdownHook();

}
