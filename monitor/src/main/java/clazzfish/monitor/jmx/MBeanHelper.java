/*
 * Copyright (c) 2008-2018 by Oliver Boehm
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
 * (c)reated 19.02.2009 by oliver (ob@oasd.de)
 */
package clazzfish.monitor.jmx;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;
import java.lang.management.ManagementFactory;

/**
 * This class simplifies the use of JMX and MBeans a little bit.
 * Originally it was part of the PatternTesting project.
 *
 * @author <a href="boehm@javatux.de">oliver</a>
 */
public class MBeanHelper {

	private static final Logger LOG = LoggerFactory.getLogger(MBeanHelper.class);
	private static final MBeanServer server = ManagementFactory.getPlatformMBeanServer();

	/** Utility class - no need to instantiate it */
	private MBeanHelper() {
	}

	/**
	 * Gets an MBean name for the given object.
	 *
	 * @param mbean
	 *            the mbean
	 *
	 * @return the name of the MBean
	 */
	public static String getMBeanName(final Object mbean) {
		return getMBeanName(mbean.getClass());
	}

	/**
	 * Gets an MBean name for the given object.
	 *
	 * @param mbean
	 *            the mbean
	 * @param level
	 *            the level
	 * @return the name of the MBean
	 * @since 1.4.3
	 */
	public static String getMBeanName(final Object mbean, final int level) {
		return getMBeanName(mbean.getClass(), level);
	}

	/**
	 * Converts the class name into a MBean name. For a hierachical structure of
	 * the registered MBeans take a look at <a href=
	 * "http://www.oracle.com/technetwork/java/javase/tech/best-practices-jsp-136021.html"
	 * >Java Management Extensions (JMX) - Best Practices</a>.
	 * <p>
	 * The default level for an MBean is since 1.4.3 now set to "2" (see
	 * {@link #getMBeanName(Object, int)}. This means you will find all MBeans
	 * from PatternTesting Runtime under the node "clazzfish.monitor" if
	 * you open your JMX console (e.g. the 'jconsole' from the JDK).
	 * </p>
	 *
	 * @param cl
	 *            e.g. my.good.bye.World
	 * @return a valid MBean name e.g. "my:type=good,good=bye,name=World"
	 * @see #getMBeanName(Class, int)
	 */
	public static String getMBeanName(final Class<?> cl) {
		return getMBeanName(cl, 2);
	}

	/**
	 * Converts the class name into a MBean name. For a hierachical structure of
	 * the registered MBeans take a look at <a href=
	 * "http://www.oracle.com/technetwork/java/javase/tech/best-practices-jsp-136021.html"
	 * >Java Management Extensions (JMX) - Best Practices</a>.
	 * <p>
	 * With the 2nd parameter (level) you can control the root element. If you
	 * set it i.e. to 2 the result in the jconsole would look like:
	 *
	 * <pre>
	 * my.good
	 *     bye
	 *         World
	 * </pre>
	 * <p>
	 * if the given class is "my.good.by.World".
	 * </p>
	 *
	 * @param cl
	 *            e.g. my.good.bye.World
	 * @param level
	 *            the level, e.g.
	 * @return a valid MBean name e.g. "my.good:type=bye,name=World"
	 * @since 1.4.3
	 */
	public static String getMBeanName(final Class<?> cl, final int level) {
		assert level > 0 : "level must be 1 or greater";
		String packageName = cl.getPackage().getName();
		String mbeanName = getAsMBeanType(level, packageName);
		return mbeanName + ",name=" + cl.getSimpleName();
	}

	/**
	 * Because it is not so easy to construct a correct MBean name. So this
	 * method helps to translate a simple name into an MBean name.
	 * <p>
	 * If the given name is already a a valid MBean name it will be returned
	 * untouched.
	 * </p>
	 *
	 * @param name
	 *            e.g. "one.two.For"
	 * @return e.g. "one:type=two,name=For"
	 * @since 1.6
	 */
	public static String getMBeanName(final String name) {
		if (name.contains(":")) {
			return name;
		}
		if (name.contains(".")) {
			String packageName = StringUtils.substringBeforeLast(name, ".");
			return getAsMBeanType(1, packageName) + ",name=" + StringUtils.substringAfterLast(name, ".");
		} else {
			return ":name=" + name;
		}
	}

	private static String getAsMBeanType(final int level, final String packageName) {
		String[] names = StringUtils.split(packageName, ".");
		int n = (level >= names.length) ? names.length - 1 : level;
		StringBuilder domain = new StringBuilder(names[0]);
		for (int i = 1; i < n; i++) {
			domain.append(".");
			domain.append(names[i]);
		}
		String type = names[n];
		StringBuilder mbeanName = new StringBuilder(domain);
		mbeanName.append(":type=");
		mbeanName.append(type);
		for (int i = n + 1; i < names.length; i++) {
			mbeanName.append(",").append(names[i - 1]).append("=").append(names[i]);
		}
		return mbeanName.toString();
	}

	/**
	 * Gets a class as {@link ObjectName}.
	 *
	 * @param name
	 *            the name
	 * @return name as object name
	 * @since 1.6
	 */
	public static ObjectName getAsObjectName(final String name) {
		try {
			return new ObjectName(MBeanHelper.getMBeanName(name));
		} catch (MalformedObjectNameException ex) {
			throw new IllegalArgumentException("illegal name: " + name, ex);
		}
	}

	/**
	 * Gets a class as {@link ObjectName}.
	 *
	 * @param mbeanClass
	 *            the mbean class
	 * @return class as object name
	 * @since 1.6
	 */
	public static ObjectName getAsObjectName(final Class<?> mbeanClass) {
		String name = MBeanHelper.getMBeanName(mbeanClass);
		try {
			return new ObjectName(name);
		} catch (MalformedObjectNameException ex) {
			throw new IllegalStateException("'" + name + "' cannot be transformed to an ObjectName", ex);
		}
	}

	/**
	 * Register the given object as MBean.
	 *
	 * @param mbean
	 *            the mbean
	 * @throws JMException
	 *             if registration fails
	 */
	public static void registerMBean(final Object mbean) throws JMException {
		String mbeanName = getMBeanName(mbean);
		registerMBean(mbeanName, mbean);
	}

	/**
	 * Register the given object as MBean.
	 *
	 * @param mbeanName
	 *            the mbean name
	 * @param mbean
	 *            the mbean
	 */
	public static synchronized void registerMBean(final String mbeanName, final Object mbean) {
		try {
			ObjectName name = new ObjectName(mbeanName);
			registerMBean(name, mbean);
		} catch (MalformedObjectNameException ex) {
			LOG.info("Cannot register '{}' as MBean:", mbean, ex);
		}
	}

	/**
	 * Register m bean.
	 *
	 * @param name
	 *            the name
	 * @param mbean
	 *            the mbean
	 */
	public static synchronized void registerMBean(final ObjectName name, final Object mbean) {
		try {
			LOG.trace("Registering '{}'...", name);
			server.registerMBean(mbean, name);
			LOG.debug("'{}' successful registered as MBean", name);
		} catch (InstanceAlreadyExistsException ex) {
			LOG.debug("'{}' is already registered.", name);
			LOG.trace("Details:", ex);
		} catch (MBeanRegistrationException ex) {
			LOG.info("Cannot register <{}> as MBean:", mbean, ex);
		} catch (NotCompliantMBeanException ex) {
			LOG.info("<{}> is not a compliant MBean:", mbean, ex);
		}
	}

	/**
	 * Unregister an MBean.
	 *
	 * @param mbeanName
	 *            the mbean name
	 */
	public static synchronized void unregisterMBean(final String mbeanName) {
		try {
			ObjectName name = new ObjectName(mbeanName);
			unregisterMBean(name);
		} catch (MalformedObjectNameException ex) {
			throw new IllegalArgumentException("'" + mbeanName + "' is not a valid name", ex);
		}
	}

	/**
	 * Unregister an MBean.
	 *
	 * @param name
	 *            the name
	 */
	public static synchronized void unregisterMBean(final ObjectName name) {
		try {
			server.unregisterMBean(name);
			LOG.debug("MBean " + name + " successful unregistered");
		} catch (MBeanRegistrationException ex) {
			LOG.info("Cannot unregister '" + name + "':", ex);
		} catch (InstanceNotFoundException ex) {
			LOG.info("'" + name + "' not found:", ex);
		}
	}

	/**
	 * Checks if is registered.
	 *
	 * @param mbeanName
	 *            the mbean name
	 * @return true, if is registered
	 */
	public static boolean isRegistered(final String mbeanName) {
		ObjectName name = MBeanHelper.getAsObjectName(mbeanName);
		return isRegistered(name);
	}

	/**
	 * Checks if is registered.
	 *
	 * @param name
	 *            the name
	 * @return true, if is registered
	 * @since 1.6
	 */
	public static boolean isRegistered(final ObjectName name) {
		try {
			ObjectInstance mbean = server.getObjectInstance(name);
			return (mbean != null);
		} catch (InstanceNotFoundException ex) {
			LOG.trace("'" + name + "' not found:", ex);
			return false;
		}
	}

	/**
	 * Checks if is registered.
	 *
	 * @param obj
	 *            the obj
	 *
	 * @return true, if is registered
	 */
	public static boolean isRegistered(final Object obj) {
		String mbeanName = getMBeanName(obj.getClass());
		return isRegistered(mbeanName);
	}

	/**
	 * Gets the object instance.
	 *
	 * @param name
	 *            the name
	 * @return the object instance
	 * @throws InstanceNotFoundException
	 *             the instance not found exception
	 */
	public static ObjectInstance getObjectInstance(final String name) throws InstanceNotFoundException {
		try {
			ObjectName mbeanName = new ObjectName(name);
			return getObjectInstance(mbeanName);
		} catch (MalformedObjectNameException e) {
			throw new IllegalArgumentException(name, e);
		}
	}

	/**
	 * Gets the object instance.
	 *
	 * @param mbeanName
	 *            the mbean name
	 * @return the object instance
	 * @throws InstanceNotFoundException
	 *             the instance not found exception
	 */
	public static ObjectInstance getObjectInstance(final ObjectName mbeanName) throws InstanceNotFoundException {
		return server.getObjectInstance(mbeanName);
	}

	/**
	 * Gets the attribute.
	 *
	 * @param mbeanName
	 *            the mbean name
	 * @param attributeName
	 *            the attribute name
	 * @return the attribute
	 * @throws InstanceNotFoundException
	 *             the instance not found exception
	 * @throws JMException
	 *             the jM exception
	 */
	public static Object getAttribute(final String mbeanName, final String attributeName) throws JMException {
		try {
			ObjectName mbean = new ObjectName(mbeanName);
			return getAttribute(mbean, attributeName);
		} catch (MalformedObjectNameException e) {
			throw new IllegalArgumentException(mbeanName, e);
		}
	}

	/**
	 * Gets the attribute.
	 *
	 * @param mbean
	 *            the mbean
	 * @param attributeName
	 *            the attribute name
	 * @return the attribute
	 * @throws InstanceNotFoundException
	 *             the instance not found exception
	 * @throws JMException
	 *             the jM exception
	 */
	public static Object getAttribute(final ObjectName mbean, final String attributeName) throws JMException {
		try {
			return server.getAttribute(mbean, attributeName);
		} catch (AttributeNotFoundException e) {
			throw new IllegalArgumentException(attributeName, e);
		}
	}

	/**
	 * Creates a {@link TabularDataSupport} object.
	 *
	 * @param rowType
	 *            the row type
	 * @param itemNames
	 *            the item names
	 * @return the tabular data support
	 * @throws OpenDataException
	 *             the open data exception
	 */
	public static TabularDataSupport createTabularDataSupport(final CompositeType rowType, final String[] itemNames)
			throws OpenDataException {
		TabularType tabularType = new TabularType("propertyTabularType", "properties tabular", rowType, itemNames);
		return new TabularDataSupport(tabularType);
	}

}
