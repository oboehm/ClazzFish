/*
 * Copyright (c) 2024,2025 by Oliver Boehm
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
 * (c)reated 28.12.24 by oboehm
 */
package clazzfish.core.jmx;

import javax.management.*;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class simplifies the use of JMX and MBeans a little bit.
 * It replaces the old MBeanHelper class from PatternTesting.
 *
 * @author <a href="boehm@javatux.de">oliver</a>
 * @since 2.3
 */
public class MBeanFinder {

    private static final Logger log = Logger.getLogger(MBeanFinder.class.getName());
    private static final MBeanServer MBEAN_SERVER = ManagementFactory.getPlatformMBeanServer();

    /** Utility class - no need to instantiate it */
	private MBeanFinder() {
	}

	/**
	 * Gets an MBean name for the given object.
	 *
	 * @param mbean the mbean
	 * @return the name of the MBean
	 */
	public static String getMBeanName(final Object mbean) {
		return getMBeanName(mbean.getClass());
	}

	/**
	 * Converts the class name into a MBean name. For a hierarchical structure of
	 * the registered MBeans take a look at <a href=
	 * "http://www.oracle.com/technetwork/java/javase/tech/best-practices-jsp-136021.html"
	 * >Java Management Extensions (JMX) - Best Practices</a>.
	 * <p>
	 * The default level for an MBean is since 1.4.3 now set to "1". This means
	 * you will find all MBeans ClazzFish under the node "clazzfish" if you
	 * open your JMX console (e.g. the 'jconsole' from the JDK).
	 * </p>
	 *
	 * @param cl a class, e.g. my.good.bye.World
	 * @return a valid MBean name, e.g. "my:type=good,good=bye,name=World"
	 * @see #getMBeanName(Class, int)
	 */
	public static String getMBeanName(final Class<?> cl) {
		return getMBeanName(cl, 1);
	}

	/**
	 * Converts the class name into a MBean name. For a hierachical structure of
	 * the registered MBeans take a look at <a href=
	 * "http://www.oracle.com/technetwork/java/javase/tech/best-practices-jsp-136021.html"
	 * >Java Management Extensions (JMX) - Best Practices</a>.
	 * <p>
	 * With the 2nd parameter (level) you can control the root element. If you
	 * set it i.e. to 2 the result in the jconsole would look like:
	 * <pre>
	 * my.good
	 *     bye
	 *         World
	 * </pre>
	 * <p>
	 * if the given class is "my.good.by.World".
	 * </p>
	 *
	 * @param cl	e.g. my.good.bye.World
	 * @param level the level, e.g. 2
	 * @return a valid MBean name e.g. "my.good:type=bye,name=World"
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
	 * @param name e.g. "one.two.For"
	 * @return e.g. "one:type=two,name=For"
	 * @since 1.6
	 */
	public static String getMBeanName(final String name) {
		if (name.contains(":")) {
			return name;
		}
		if (name.contains(".")) {
            int lastDotIndex = name.lastIndexOf('.');
            String packageName = name.substring(0, lastDotIndex);
            String classname = name.substring(lastDotIndex + 1);
            return getAsMBeanType(1, packageName) + ",name=" + classname;
		} else {
			return ":name=" + name;
		}
	}

	private static String getAsMBeanType(final int level, final String packageName) {
        String[] names = packageName.split("\\.");
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
	 * @param name the name
	 * @return name as object name
	 * @since 1.6
	 */
	public static ObjectName getAsObjectName(final String name) {
		try {
			return new ObjectName(MBeanFinder.getMBeanName(name));
		} catch (MalformedObjectNameException ex) {
			throw new IllegalArgumentException("illegal name: " + name, ex);
		}
	}

	/**
	 * Gets a class as {@link ObjectName}.
	 *
	 * @param mbeanClass the mbean class
	 * @return class as object name
	 * @since 1.6
	 */
	public static ObjectName getAsObjectName(final Class<?> mbeanClass) {
		String name = MBeanFinder.getMBeanName(mbeanClass);
		try {
			return new ObjectName(name);
		} catch (MalformedObjectNameException ex) {
			throw new IllegalStateException("'" + name + "' cannot be transformed to an ObjectName", ex);
		}
	}

	/**
	 * Register the given object as MBean.
	 *
	 * @param mbean the MBean for registration
	 */
	public static void registerMBean(final Object mbean) {
		String mbeanName = getMBeanName(mbean);
		registerMBean(mbeanName, mbean);
	}

	/**
	 * Register the given object as MBean.
	 *
	 * @param mbeanName the mbean name
	 * @param mbean     the mbean
	 */
	public static synchronized void registerMBean(final String mbeanName, final Object mbean) {
		try {
			ObjectName name = new ObjectName(mbeanName);
			registerMBean(name, mbean);
		} catch (MalformedObjectNameException ex) {
			log.log(Level.INFO, "Cannot register \"{0}\" as MBean ({1}).", new Object[] { mbean, ex.getMessage()});
            log.log(Level.FINE, "Details:", ex);
		}
	}

	/**
	 * Register m bean.
	 *
	 * @param name  the name
	 * @param mbean the mbean
	 */
	public static synchronized void registerMBean(final ObjectName name, final Object mbean) {
		try {
			log.log(Level.FINER, "Registering \"{0}\"...", name);
			MBEAN_SERVER.registerMBean(mbean, name);
			log.log(Level.FINE, "\"{0}\" successful registered as MBean", name);
		} catch (InstanceAlreadyExistsException ex) {
            log.log(Level.FINE, "'{}' is already registered.", name);
            log.log(Level.FINER, "Details:", ex);
        } catch (JMException ex) {
            log.log(Level.INFO, "Cannot register <{0}> as MBean ({1}).", new Object[] {mbean, ex.getMessage()});
            log.log(Level.FINE, "Details:", ex);
		}
	}

	/**
	 * Unregister an MBean.
	 *
	 * @param name the name
	 */
	public static synchronized void unregisterMBean(final ObjectName name) {
		try {
			MBEAN_SERVER.unregisterMBean(name);
			log.log(Level.FINE, "MBean {0} successful unregistered", name);
		} catch (JMException ex) {
            log.log(Level.INFO, "Cannot unregister <{0}> ({1}).", new Object[] {name, ex.getMessage()});
            log.log(Level.FINE, "Details:", ex);
		}
	}

	/**
	 * Checks if is registered.
	 *
	 * @param mbeanName the mbean name
	 * @return true, if is registered
	 */
	public static boolean isRegistered(final String mbeanName) {
		ObjectName name = MBeanFinder.getAsObjectName(mbeanName);
		return isRegistered(name);
	}

	/**
	 * Checks if is registered.
	 *
	 * @param name the name
	 * @return true, if is registered
	 * @since 1.6
	 */
	public static boolean isRegistered(final ObjectName name) {
		try {
			ObjectInstance mbean = MBEAN_SERVER.getObjectInstance(name);
			return (mbean != null);
		} catch (InstanceNotFoundException ex) {
            log.log(Level.FINER, "\"{0}\" not found ({1})'", new Object[] {name, ex.getMessage()});
            log.log(Level.FINEST, "Details:", ex);
			return false;
		}
	}

	/**
	 * Creates a {@link TabularDataSupport} object.
	 *
	 * @param rowType   the row type
	 * @param itemNames the item names
	 * @return the tabular data support
	 * @throws OpenDataException if an element's value of itemNames is not an
	 * 							 item name defined in rowType.
	 */
	public static TabularDataSupport createTabularDataSupport(final CompositeType rowType, final String[] itemNames)
			throws OpenDataException {
		TabularType tabularType = new TabularType("propertyTabularType", "properties tabular",
				rowType, itemNames);
		return new TabularDataSupport(tabularType);
	}

	/**
	 * Finds an MBean with the given names.
	 *
	 * @param mbeanNames names of the MBeans to be looked for
	 * @return an instance of the MBean or null if no one is found
	 */
	public static ObjectInstance findMBean(String... mbeanNames) {
		for (String name : mbeanNames) {
			try {
				ObjectName objectName = new ObjectName(name);
				return MBEAN_SERVER.getObjectInstance(objectName);
			} catch (InstanceNotFoundException | MalformedObjectNameException ex) {
                log.log(Level.FINE, "\"{0}\" is not registered as MBean ({1})'", new Object[] {name, ex.getMessage()});
                log.log(Level.FINER, "Details:", ex);
			}
		}
		log.log(Level.FINE, "No MBean \"{0}\" found.", Arrays.toString(mbeanNames));
		return null;
	}

}
