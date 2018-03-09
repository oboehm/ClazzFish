package clazzfish.monitor;

import clazzfish.monitor.jmx.Description;

import java.net.URI;

/**
 * All the methods which might help you to find classpath problems are collected
 * in this JMX interface. Most of the methods returns a String because this can
 * be easily shown in the JConsole.
 * <p>
 * Originally this interface was part of the PatternTesting project where part
 * of this interface was moved to ResourcepathMonitorMBean with v1.6.4 to
 * make this interface more clearly. And it is no longer Serializable because
 * there is no need for it - all informations can be restored from the
 * classpath.
 * </p>
 *
 * @author oliver
 */
@Description("Classpath Monitor to be able to inspect the classpath and to find doublets")
public interface ClasspathMonitorMBean extends AbstractMonitorMBean {

	/**
	 * Looks if the given classname can be found in the classpath.
	 * <p>
	 * To avoid problems like "java.rmi.UnmarshalException: failed to unmarshal
	 * class java.lang.Object; nested exception is: java.io.IOException: unknown
	 * protocol: zip" no longer a URL but URI is now returned
	 * </p>
	 *
	 * @param name of a class e.g. "java.lang.String"
	 * @return URI of the given resource (or null if resource was not found)
	 */
	@Description("return the URI if the given class")
	URI whichClass(String name);

	/**
	 * Get the number of different versions of the given class.
	 *
	 * @param classname the name of the class
	 * @return how often the classname was found in the classpath
	 */
	@Description("how often the given class is found in the classpath")
	int getNoClasses(String classname);

	/**
	 * Is the given class a doublet, e.g. can it be found several times in the
	 * classpath? This method is the successor of the old isDoublet method which
	 * expects a String as argument.
	 *
	 * @param clazz the class
	 * @return true if more than one classname or resource was found in the
	 *         classpath
	 * @throws java.util.NoSuchElementException if no classname or resource was found
	 */
	@Description("is the given classname or resource found more than once in the classpath?")
	boolean isDoublet(Class<?> clazz);

	/**
	 * Returns the first doublet of the given class. This method is the
	 * successor of the old isDoublet method which expects a String as argument.
	 *
	 * @param clazz the class
	 * @return the first doublet
	 */
	@Description("returns the first doublet of the given classname or resource")
	URI getFirstDoublet(Class<?> clazz);

	/**
	 * Looks for each loaded class if it is a doublet or not.
	 *
	 * @return a sorted array with the found doublets
	 */
	@Description("returns a sorted array of all found doublets")
	String[] getDoublets();

	/**
	 * Looks for each found doublet in which classpath it was found.
	 *
	 * @return the classpath where doublets were found
	 */
	@Description("returns the classpath where doublets were found")
	String[] getDoubletClasspath();

	/**
	 * Returns the n'th doublet of the given class. This method is the successor
	 * of the old isDoublet method which expects a String as argument.
	 *
	 * @param clazz the clazz
	 * @param n number of wanted doublet
	 * @return the n'th doublet URL
	 */
	@Description("returns the n'th doublet of the given classname or resource")
	URI getDoublet(Class<?> clazz, int n);

	/**
	 * Returns the packages which were loaded by the classloader. The loaded
	 * packages are returned as string array so that it can be displayed by the
	 * 'jconsole'.
	 *
	 * @return the packages as string array
	 */
	@Description("returns an array of all loaded packages")
	String[] getLoadedPackages();

	/**
	 * Returns the classes which were loaded by the classloader. The loaded
	 * packages are returned as string array so that it can be displayed by the
	 * 'jconsole'.
	 *
	 * @return the classes as string array
	 */
	@Description("returns an array of all loaded classes")
	String[] getLoadedClasses();

	/**
	 * Is the given classname already loaded?.
	 *
	 * @param classname the name of the class
	 * @return true if given classname is already loaded
	 */
	@Description("is the given classname already loaded?")
	boolean isLoaded(String classname);

	/**
	 * Returns the classes which are not yet loaded. These are the classes
	 * returned by getClasspathClasses() but not by getLoadedClasses().
	 *
	 * @return the classes which are not yet loaded
	 */
	@Description("returns all classes which are not yet loaded")
	String[] getUnusedClasses();

	/**
	 * Gets the loaded classpath (without the bootclasspath) as URI array.
	 *
	 * @return the loaded classpath (sorted)
	 */
	@Description("returns the classpath with the loaded classes")
	String[] getUsedClasspath();

	/**
	 * The unused classpath is this path which are not used in past.
	 *
	 * @return the unused classpath (sorted)
	 */
	@Description("returns the classpath which were not yet used")
	String[] getUnusedClasspath();

	/**
	 * Gets the boot classpath.
	 *
	 * @return boot classpath
	 */
	@Description("return ths boot classpath")
	String[] getBootClasspath();

	/**
	 * Gets the normal classpath which is also available by the system property
	 * "java.class.path".
	 *
	 * @return the classpath as String array
	 */
	@Description("returns the complete classpath which is seen by the application")
	String[] getClasspath();

	/**
	 * Looks in the normal classpath after all classes and returns it as String
	 * array.
	 *
	 * @return all found classes in the classpath
	 */
	@Description("returns all classes of the classpath")
	String[] getClasspathClasses();

	/**
	 * Gets the serial version uid.
	 *
	 * @param classname the name of the class
	 * @return the serialVersionUID of the given class (or null if the class
	 *         does not have one)
	 * @throws IllegalAccessException e.g. if there is no SerialVersionUID
	 */
	@Description("returns the servialSerialVersionUID of the given class")
	Long getSerialVersionUID(String classname) throws IllegalAccessException;

	/**
	 * If a MANIFEST is found for a given class the attributes in this file
	 * should be returned as a string array. E.g. for commons-lang-2.3.jar the
	 * string array may looks like
	 *
	 * <pre>
	 * Manifest-Version: 1.0
	 * Ant-Version: Apache Ant 1.6.5
	 * Created-By: 1.3.1_09-85 ("Apple Computer, Inc.")
	 * Package: org.apache.commons.lang
	 * Extension-Name: commons-lang
	 * Specification-Version: 2.3
	 * Specification-Vendor: Apache Software Foundation
	 * Specification-Title: Commons Lang
	 * Implementation-Version: 2.3
	 * Implementation-Vendor: Apache Software Foundation
	 * Implementation-Title: Commons Lang
	 * Implementation-Vendor-Id: org.apache
	 * X-Compile-Source-JDK: 1.3
	 * X-Compile-Target-JDK: 1.1
	 * </pre>
	 *
	 * @param classname the name of the class
	 * @return the attribute entries of the Manifest (or emtpy array if no
	 *         Manifest or no attributes are found)
	 */
	@Description("return the manifest entries for the given class")
	String[] getManifestEntries(String classname);

	/**
	 * Incompatible classes are doublets with different byte codes.
	 *
	 * @return doublet classes with different byte codes
	 */
	@Description("returns the doublet classes with different byte codes")
	String[] getIncompatibleClasses();

	/**
	 * Gets the incompatible classpath.
	 *
	 * @return the classpathes where incompatible classes were found
	 */
	@Description("returns the classpath where incompatible classes were found")
	String[] getIncompatibleClasspath();

	/**
	 * Checks if is classloader supported.
	 *
	 * @return true if it is a known classloader
	 */
	@Description("returns true for classloader which are known and tested")
	boolean isClassloaderSupported();

	/**
	 * Returns some information about the classloader. At least the user should
	 * be informed if it is a unknown classloader which is not supported or not
	 * tested.
	 *
	 * @return e.g. "unknown classloader xxx - classpath can be wrong"
	 */
	@Description("returns some infos about the found classloader")
	String getClassloaderInfo();

	/**
	 * Is multi threading enabled?.
	 *
	 * @return true if multi threading is enabled for this class.
	 */
	boolean isMultiThreadingEnabled();

	/**
	 * Here you can enable or disable the multi threading mode.
	 *
	 * @param enabled true, if multithreading should be enabled
	 */
	@Description("experimental, don't change it!")
	void setMultiThreadingEnabled(boolean enabled);

}
