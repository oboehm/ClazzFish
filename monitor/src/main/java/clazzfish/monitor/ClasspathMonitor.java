/*
 * Copyright (c) 2008-2022 by Oliver Boehm
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
 * (c)reated 10.02.2009 by oliver (ob@oasd.de)
 */

package clazzfish.monitor;

import clazzfish.monitor.internal.ClasspathDigger;
import clazzfish.monitor.internal.DoubletDigger;
import clazzfish.monitor.jmx.MBeanHelper;
import clazzfish.monitor.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.ObjectName;
import java.io.*;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * To avoid classpath problems like double entries of the same class or resource
 * in the classpath there are several methods available.
 * <p>
 * To get the boot classpath the system property "sun.boot.class.path" is used
 * to get them. This will work of course only for the SunVM.
 * </p>
 * <p>
 * After an idea from
 * <a href="http://www.javaworld.com/javaworld/javatips/jw-javatip105.html">Java
 * Tip 105</a>.
 * </p>
 * <p>
 * Original this monitor class was part of the PatternTesting project where
 * startup time and performance was increased with v1.5 dramatically. To speed
 * up startup time a {@link FutureTask} is used to set up internal structures.
 * For a better performance doublets are now detected with parallel threads.
 * Also some expensive operations are cached now.
 * </p>
 * <p>
 * Because some stuff are done in parallel we no longer use a WeakHashMap but
 * {@link ConcurrentHashMap} to avoid synchronization problems. It is expected
 * that the size of cached objects is small enough and we will not run in any
 * OutOfMemory problems.
 * </p>
 *
 * @author <a href="boehm@javatux.de">oliver</a>
 */
public class ClasspathMonitor extends AbstractMonitor implements ClasspathMonitorMBean {

	private static final Logger LOG = LoggerFactory.getLogger(ClasspathMonitor.class);
	private static final Executor EXECUTOR = Executors.newCachedThreadPool();
	private static final ClasspathMonitor INSTANCE;
	/** This array is used to call the corresponding getter method. */
	private static final String[] DUMP_GETTERS = { "BootClasspath", "Classpath", "ClasspathClasses", "DoubletClasspath",
			"DoubletClasspathURIs", "Doublets", "LoadedClasses", "LoadedPackages", "IncompatibleClasses",
			"IncompatibleClasspath", "IncompatibleClasspathURIs", "UnusedClasses", "UnusedClasspath", "UsedClasspath",
			"UsedClasspathURIs" };
	private static final URI NULL_URI = URI.create("http://null");

	private final ClasspathDigger classpathDigger;
	private final DoubletDigger doubletDigger;
	private final ClassLoader cloader;
	private final String[] classpath;
	private String[] loadedClasses = new String[0];
	private final FutureTask<String[]> allClasspathClasses;
	private final FutureTask<Set<String>> unusedClasses;

	private final Map<Class<?>, URI> usedClassCache = new ConcurrentHashMap<>();
	private final Map<Class<?>, URI> usedClasspathCache = new ConcurrentHashMap<>();
	private final List<Class<?>> incompatibleClassList = new ArrayList<>();

	static {
		INSTANCE = new ClasspathMonitor();
	}

	/**
	 * Instantiates a new classpath monitor.
	 * <p>
	 * Note: The constructor is protected because it is needed by the benchmark
	 * subproject which compares different implementations.
	 * </p>
	 */
	protected ClasspathMonitor() {
		this.classpathDigger = new ClasspathDigger();
		this.cloader = this.classpathDigger.getClassLoader();
		this.classpath = this.classpathDigger.getClasspath();
		this.allClasspathClasses = getFutureCasspathClasses();
		this.unusedClasses = getFutureUnusedClasses();
		this.doubletDigger = new DoubletDigger(this.classpathDigger);
	}

	private FutureTask<String[]> getFutureCasspathClasses() {
		FutureTask<String[]> classes = new FutureTask<>(this::getClasspathClassArray);
		EXECUTOR.execute(classes);
		return classes;
	}

	private FutureTask<Set<String>> getFutureUnusedClasses() {
		FutureTask<Set<String>> classes = new FutureTask<>(this::getClasspathClassSet);
		EXECUTOR.execute(classes);
		return classes;
	}

	/**
	 * Yes, it is a Singleton because it offers only some services. So we don't
	 * need the object twice.
	 *
	 * @return the only instance
	 */
	public static ClasspathMonitor getInstance() {
		return INSTANCE;
	}

	/**
	 * With this method you can register the {@link ClasspathMonitor} with the
	 * default name.
	 * <p>
	 * You can only register the {@link ClasspathMonitor} once only. If you want
	 * to register it with another name you have to first unregister it.
	 * </p>
	 *
	 * @see ClasspathMonitor#registerAsMBean(String)
	 * @see ClasspathMonitor#unregisterAsMBean()
	 */
	public static void registerAsMBean() {
	    getInstance().registerMeAsMBean();
	}

	/**
	 * With this method you can register the {@link ClasspathMonitor} with your
	 * own name. This is e.g. useful if you have an application server with
	 * several applications.
	 * <p>
	 * You can register the {@link ClasspathMonitor} once only. If you want
	 * to register it with another name you have to first unregister it.
	 * </p>
	 *
	 * @param name e.g "my.company.ClasspathMonitor"
	 * @see ClasspathMonitor#unregisterAsMBean()
	 */
	public static void registerAsMBean(final String name) {
		registerAsMBean(MBeanHelper.getAsObjectName(name));
	}

	/**
	 * With this method you can register the {@link ClasspathMonitor} with your
	 * own name. This is e.g. useful if you have an application server with
	 * several applications.
	 * <p>
	 * You can only register the {@link ClasspathMonitor} once only. If you want
	 * to register it with another name you have to first unregister it.
	 * </p>
	 *
	 * @param name the name
	 * @see ClasspathMonitor#unregisterAsMBean()
	 * @since 1.6
	 */
	public static synchronized void registerAsMBean(final ObjectName name) {
		if (isRegisteredAsMBean()) {
			LOG.debug("MBean already registered - registerAsMBean(\"{}\") ignored.", name);
		} else {
		    getInstance().registerMeAsMBean(name);
		}
	}

	/**
	 * Unregister ClasspathMonitor as MBean.
	 */
	public static void unregisterAsMBean() {
	    getInstance().unregisterMeAsMBean();
	}

	/**
	 * If you want to ask JMX if bean is already registered you can ask the
	 * MBeanHelper (if you know the object name) or you can ask this method.
	 *
	 * @return true if MBean is already registered.
	 * @since 1.0
	 */
	public static boolean isRegisteredAsMBean() {
	    return getInstance().isMBean();
	}

	/**
	 * Which class.
	 *
	 * @param name the classname
	 * @return the URI
	 * @see ClasspathMonitorMBean#whichClass(String)
	 */
	@Override
	public URI whichClass(final String name) {
		String resource = Converter.classToResource(name);
		return classpathDigger.whichResource(resource);
	}

	/**
	 * Where is the given class found in the classpath? Which class was loaded
	 * from which URI?.
	 *
	 * @param clazz
	 *            the class
	 *
	 * @return file or jar path
	 */
	public URI whichClass(final Class<?> clazz) {
		LOG.trace("Searching {} in classpath.", clazz);
		URI uri = this.usedClassCache.get(clazz);
		if (uri == null) {
			uri = whichClass(clazz.getName());
			if (uri == null) {
				LOG.trace("{} was not found in classpath.", clazz);
			} else {
				this.usedClassCache.put(clazz, uri);
			}
		}
		LOG.trace("Searching {} in classpath finished, {} found.", clazz, uri);
		return uri;
	}

	/**
	 * Returns the jar file or path where the given classname was found.
	 *
	 * @param classname
	 *            e.g. java.lang.String
	 *
	 * @return jar or path as URI
	 */
	public URI whichClassPath(final String classname) {
		String resource = Converter.classToResource(classname);
		return whichResourcePath(resource);
	}

	/**
	 * Returns the jar file or path where the given class was found.
	 *
	 * @param clazz
	 *            e.g. Sting.class
	 *
	 * @return jar or path as URI
	 */
	public URI whichClassPath(final Class<?> clazz) {
		return whichClassPath(clazz.getName());
	}

	/**
	 * Which class path.
	 *
	 * @param p
	 *            the p
	 *
	 * @return the uRI
	 */
	public URI whichClassPath(final Package p) {
		String resource = Converter.toResource(p);
		return whichResourcePath(resource);
	}

	/**
	 * Returns the jar file or path where the given resource was found.
	 *
	 * @param resource
	 *            e.g. log4j.properties
	 *
	 * @return jar or path as URI
	 */
	public URI whichResourcePath(final String resource) {
		URI uri = this.classpathDigger.whichResource(resource);
		return ClasspathHelper.getParent(uri, resource);
	}

	/**
	 * Which class jar.
	 *
	 * @param clazz the clazz
	 * @return the jar file
	 */
	public JarFile whichClassJar(final Class<?> clazz) {
		return whichClassJar(clazz.getName());
	}

	/**
	 * Which class jar.
	 *
	 * @param classname
	 *            the classname
	 *
	 * @return the jar file
	 */
	public JarFile whichClassJar(final String classname) {
		String resource = Converter.classToResource(classname);
		return whichResourceJar(resource);
	}

	/**
	 * Returns the path of the given clazz.
	 *
	 * @param clazz the class
	 * @return class path
	 * @since 1.1
	 */
	public Path whichPath(final Class<?> clazz) {
		return whichPath(Converter.classToResource(clazz.getName()));
	}

	/**
	 * Returns the path of the given resource image. This method was introduced
	 * for the module system as described in
	 * <a href="https://openjdk.java.net/jeps/220">JEP 220</a> (Modular
	 * Run-Time Images).
	 *
	 * @param resource the resource
	 * @return resource path
	 * @since 1.1
	 */
	public Path whichPath(final String resource) {
		URI uri = whichResourcePath(resource);
		if ("jrt".equals(uri.getScheme())) {
			FileSystem fs = FileSystems.getFileSystem(URI.create("jrt:/"));
			return fs.getPath("modules", uri.getPath(), resource);
		} else {
			try {
				uri = this.classpathDigger.whichResource(resource);
				return Paths.get(uri.toURL().getPath());
			} catch (MalformedURLException ex) {
				LOG.warn("Cannot convert {} to URL:", uri, ex);
				return Paths.get(uri);
			}
		}
	}

	/**
	 * Which resource jar.
	 *
	 * @param resource
	 *            the resource
	 *
	 * @return the jar file
	 */
	public JarFile whichResourceJar(final String resource) {
		return whichResourceJar(this.whichResourcePath(resource));
	}

	/**
	 * Which resource jar.
	 *
	 * @param resource
	 *            the resource
	 *
	 * @return the jar file
	 */
	public static JarFile whichResourceJar(final URI resource) {
		if (resource == null) {
			return null;
		}
		File file = Converter.toFile(resource);
		try {
			return new JarFile(file);
		} catch (IOException ioe) {
			LOG.debug("Cannot read " + file + ":", ioe);
			return null;
		}
	}

	/**
	 * Gets the resources.
	 *
	 * @param name the resource name
	 * @return the resources
	 */
	public Enumeration<URL> getResources(final String name) {
		return this.classpathDigger.getResources(name);
	}

	/**
	 * Gets the resource.
	 *
	 * @param name the resource name
	 * @return the resource
	 */
	public static URL getResource(final String name) {
		return ClasspathMonitor.class.getResource(name);
	}

	/**
	 * Gets the no classes.
	 *
	 * @param cl the class
	 * @return number of classes
	 */
	public int getNoClasses(final Class<?> cl) {
		return getNoClasses(cl.getName());
	}

	/**
	 * Gets the no classes.
	 *
	 * @param classname
	 *            the classname
	 * @return number of classes
	 * @see ClasspathMonitorMBean#getNoClasses(String)
	 */
	@Override
	public int getNoClasses(final String classname) {
		return ResourcepathMonitor.getInstance().getNoResources(Converter.classToResource(classname));
	}

	/**
	 * Is the given class a doublet, i.e. can it be found more than once in the
	 * classpath?
	 * <p>
	 * Note: Because this method is needed by other methods like
	 * {@link #getIncompatibleClassList()} the result of it is cached since 1.5.
	 * This speeds up this method about the factor 2000 and more:
	 * </p>
	 *
	 * <pre>
	 * Implementation | Mode  | Score    | Unit
	 * ---------------+-------+----------+-------
	 * old (till 1.4) | thrpt |    35514 | ops/s
	 * new (cached)   | thrpt | 92968300 | ops/s
	 * </pre>
	 * <p>
	 * This results were calculated with the help of JMH and was executed on a
	 * Dell Latitude e6520 with i5 processor (i5-2540M CPU @ 2.60GHz).
	 * </p>
	 * <p>
	 * Since 1.6.2 this functionality is now located in the extracted
	 * {@link DoubletDigger} class.
	 * </p>
	 *
	 * @param clazz
	 *            the class to be checked
	 * @return true if class is found more than once in the classpath
	 * @see ClasspathMonitorMBean#isDoublet(Class)
	 */
	@Override
	public boolean isDoublet(final Class<?> clazz) {
		return this.doubletDigger.isDoublet(clazz);
	}

	/**
	 * Gets the first doublet.
	 *
	 * @param clazz
	 *            the class
	 * @return the first doublet
	 * @see ClasspathMonitorMBean#getFirstDoublet(Class)
	 */
	@Override
	public URI getFirstDoublet(final Class<?> clazz) {
		return getDoublet(clazz, 1);
	}

	/**
	 * Gets the doublet.
	 *
	 * @param clazz the clazz
	 * @param nr the nr of the doublet (starting at 0)
	 * @return the doublet
	 */
	@Override
	public URI getDoublet(final Class<?> clazz, final int nr) {
		return this.doubletDigger.getDoublet(clazz, nr);
	}

	/**
	 * Gets the doublet list.
	 *
	 * @return the doublet list
	 */
	public synchronized List<Class<?>> getDoubletList() {
		return this.doubletDigger.getDoubletClassList();
	}

	/**
	 * Gets the doublets.
	 *
	 * @return the doublets
	 * @see ClasspathMonitorMBean#getDoublets()
	 */
	@Override
	public String[] getDoublets() {
		return this.doubletDigger.getDoubletClasses();
	}

	/**
	 * Gets the doublet classpath.
	 *
	 * @return the doublet classpath
	 */
	@Override
	public String[] getDoubletClasspath() {
		URI[] classpathURIs = this.getDoubletClasspathURIs();
		return toStringArray(classpathURIs);
	}

	/**
	 * Gets the doublet classpath as array of URIs
	 *
	 * @return an array with the doublets
	 */
	public URI[] getDoubletClasspathURIs() {
		LOG.trace("Calculating doublet-classpath.");
		Set<URI> classpathSet = this.getClasspathSet(this.getDoubletList());
		return classpathSet.toArray(new URI[0]);
	}

	private SortedSet<URI> getClasspathSet(final List<Class<?>> classes) {
		Collection<String> classResources = new ArrayList<>();
		for (Class<?> cl : classes) {
			classResources.add(Converter.toResource(cl));
		}
		return this.classpathDigger.getResourcepathSet(classResources);
	}

	/**
	 * Dumps the internal fields of the classloader (after an idea from "Java
	 * ist auch eine Insel").
	 *
	 * @return the class loader details
	 */
	public String getClassLoaderDetails() {
		StringBuilder sbuf = new StringBuilder("dump of " + cloader + ":\n");
		for (Class<?> cl = this.cloader.getClass(); cl != null; cl = cl.getSuperclass()) {
			sbuf.append('\t');
			dumpFields(sbuf, cl, this.cloader);
		}
		return sbuf.toString().trim();
	}

	private static void dumpFields(final StringBuilder sbuf, final Class<?> cl, final Object obj) {
		Field[] fields = cl.getDeclaredFields();
		try {
			AccessibleObject.setAccessible(fields, true);
		} catch (InaccessibleObjectException ex) {
			LOG.info("Start JVM with '--add-opens java.base/jdk.internal.loader=ALL-UNNAMED' to dump fields of {}.", cl);
			LOG.debug("Details:", ex);
		}
		for (int i = 0; i < fields.length; i++) {
			sbuf.append(fields[i]);
			sbuf.append(" = ");
			try {
				sbuf.append(fields[i].get(obj));
			} catch (IllegalAccessException ex) {
				LOG.info("Cannot access field {} {}.", i, fields[i]);
				LOG.debug("Details:", ex);
				sbuf.append("???");
			}
			sbuf.append('\n');
		}
	}

	/**
	 * Checks if is classloader supported.
	 *
	 * @return true if it is a known classloader
	 */
	@Override
	public boolean isClassloaderSupported() {
		return this.classpathDigger.isClassloaderSupported();
	}

	/**
	 * Returns some information about the classloader. At least the user should
	 * be informed if it is a unknown classloader which is not supported or not
	 * tested.
	 *
	 * @return e.g. "unknown classloader xxx - classpath can be wrong"
	 */
	@Override
	public String getClassloaderInfo() {
		String info = this.isClassloaderSupported() ? "supported" : "unsupported";
		return this.cloader.getClass().getName() + " (" + info + ")";
	}

	/**
	 * Gets the loaded packages.
	 *
	 * @return the loaded packages
	 *
	 * @see ClasspathMonitorMBean#getLoadedPackages()
	 */
	@Override
	public String[] getLoadedPackages() {
		Package[] packages = this.classpathDigger.getLoadedPackageArray();
		String[] strings = new String[packages.length];
		for (int i = 0; i < packages.length; i++) {
			strings[i] = packages[i].toString();
		}
		Arrays.sort(strings);
		return strings;
	}

	/**
	 * This method is used by PatternTesting Samples (in packages.jsp). So it is
	 * left here as wrapper around ClasspathDigger.
	 *
	 * @return array of packages
	 */
	public Package[] getLoadedPackageArray() {
		return this.classpathDigger.getLoadedPackageArray();
	}

	/**
	 * If you want to dump all packages you can use this method. The output will
	 * be sorted.
	 *
	 * @return each package in a single line
	 */
	public String getLoadedPackagesAsString() {
		String[] packages = getLoadedPackages();
		StringBuilder sbuf = new StringBuilder();
		for (String pkg : packages) {
			sbuf.append(pkg);
			sbuf.append('\n');
		}
		return sbuf.toString().trim();
	}

	/**
	 * Returns a list of classes which were loaded by the classloader.
	 *
	 * @return list of classes
	 */
	public synchronized List<Class<?>> getLoadedClassList() {
		return classpathDigger.getLoadedClasses();
	}

	/**
	 * As MBean a string array could be displayed by the 'jconsole'. A class
	 * array not.
	 *
	 * @return the classes as sorted string array
	 */
	@Override
	public synchronized String[] getLoadedClasses() {
		List<Class<?>> classes = getLoadedClassList();
		if (classes.size() != loadedClasses.length) {
			loadedClasses = new String[classes.size()];
			for (int i = 0; i < loadedClasses.length; i++) {
				loadedClasses[i] = classes.get(i).toString();
			}
			Arrays.sort(loadedClasses);
		}
		return loadedClasses.clone();
	}

	/**
	 * Gets the loaded classes as string.
	 *
	 * @return the loaded classes as string
	 */
	public String getLoadedClassesAsString() {
		List<Class<?>> classes = getLoadedClassList();
		classes.sort(new ObjectComparator());
		StringBuilder sbuf = new StringBuilder();
		for (Class<?> aClass : classes) {
			sbuf.append(aClass.toString().trim());
			sbuf.append('\n');
		}
		return sbuf.toString().trim();
	}

	/**
	 * Checks if the given classname is loaded. Why does we use not Class as
	 * parameter here? If you would allow a parameter of type "Class" this class
	 * will be problably loaded before and this method will return always true!
	 *
	 * @param classname name of the class
	 * @return true if class is loaded
	 */
	@Override
	public boolean isLoaded(final String classname) {
		return this.classpathDigger.isLoaded(classname);
	}

	/**
	 * Unused classes are classes which are not loaded but which are found in
	 * the classpath.
	 *
	 * @return unused classes
	 *
	 * @see #getLoadedClasses()
	 * @see #getClasspathClasses()
	 */
	@Override
	public String[] getUnusedClasses() {
		List<Class<?>> classes = getLoadedClassList();
		Collection<String> used = new ArrayList<>();
		Set<String> unusedSet = this.getUnusedClassSet();
		for (Class<?> cl : classes) {
			String classname = cl.getName();
			if (unusedSet.contains(classname)) {
				used.add(classname);
			}
		}
		unusedSet.removeAll(used);
		String[] unused = new String[unusedSet.size()];
		unusedSet.toArray(unused);
		return unused;
	}

	private Set<String> getUnusedClassSet() {
		try {
			return this.unusedClasses.get();
		} catch (InterruptedException e) {
			LOG.warn("Was interrupted before got result from {}:", this.unusedClasses, e);
			Thread.currentThread().interrupt();
		} catch (ExecutionException e) {
			LOG.warn("Cannot execute get of {}:", this.unusedClasses, e);
		}
		return this.getClasspathClassSet();
	}

	/**
	 * Scans the classpath for all classes. For performance reason we return no
	 * longer a copy but the origin array. Don't do changes for the returned
	 * array!
	 *
	 * @return all classes as String array
	 */
	@Override
	public String[] getClasspathClasses() {
		try {
			return this.allClasspathClasses.get();
		} catch (InterruptedException e) {
			LOG.warn("Was interrupted before got result from {}:", this.allClasspathClasses, e);
			Thread.currentThread().interrupt();
		} catch (ExecutionException e) {
			LOG.warn("Cannot execute get of {}:", this.allClasspathClasses, e);
		}
		return this.getClasspathClassArray();
	}

	private Set<String> getClasspathClassSet() {
		String[] classes = getClasspathClasses();
		return new TreeSet<>(Arrays.asList(classes));
	}

	/**
	 * Gets the classes of a given package name as collection.
	 *
	 * @param packageName
	 *            the package name
	 * @return the classpath class list
	 */
	public Collection<String> getClasspathClassList(final String packageName) {
		Collection<String> classlist = new ArrayList<>();
		String[] classes = this.getClasspathClasses();
		String prefix = packageName.endsWith(".") ? packageName : packageName + ".";
		for (String aClass : classes) {
			if (aClass.startsWith(prefix)) {
				classlist.add(aClass);
			}
		}
		return classlist;
	}

	/**
	 * Gets the classes of the given type.
	 *
	 * @param <T>
	 *            the generic type
	 * @param packageName
	 *            the package name
	 * @param type
	 *            the type
	 * @return the classes
	 */
	@SuppressWarnings("unchecked")
	public <T> Collection<Class<? extends T>> getClassList(final String packageName, final Class<T> type) {
		Collection<Class<? extends T>> classes = new ArrayList<>();
		Collection<Class<?>> concreteClasses = getConcreteClassList(packageName);
		for (Class<?> clazz : concreteClasses) {
			if (type.isAssignableFrom(clazz)) {
				classes.add((Class<T>) clazz);
				LOG.trace("subclass of {} found: {}", type, clazz);
			}
		}
		return classes;
	}

	/**
	 * Gets a list of concrete classes. These are classes which can be
	 * instantiated, i.e. they are not abstract and have a default constructor.
	 *
	 * @param packageName
	 *            the package name
	 * @return the concrete class list
	 */
	@SuppressWarnings("unchecked")
	public Collection<Class<?>> getConcreteClassList(final String packageName) {
		assert packageName != null;
		Collection<String> classList = this.getClasspathClassList(packageName);
		Collection<Class<?>> classes = new ArrayList<>(classList.size());
		for (String classname : classList) {
			try {
				Class<Object> clazz = (Class<Object>) Class.forName(classname);
				if (!canBeInstantiated(clazz)) {
					LOG.trace("{} will be ignored (can't be instantiated).", clazz);
					continue;
				}
				classes.add(clazz);
			} catch (ClassNotFoundException e) {
				LOG.debug("Class '{}' is ignored because it was not found:", classname, e);
			} catch (NoClassDefFoundError e) {
				LOG.debug("Class '{}' is ignored because definition was not found:", classname, e);
			} catch (ExceptionInInitializerError ex) {
				LOG.debug("Class '{}' is ignored because it cannot be initialized:", classname, ex);
			}
		}
		return classes;
	}

	private static boolean canBeInstantiated(final Class<?> clazz) {
		if (clazz.isInterface()) {
			return false;
		}
		int mod = clazz.getModifiers();
		if (Modifier.isAbstract(mod)) {
			return false;
		}
		try {
			clazz.getConstructor();
			return true;
		} catch (SecurityException ex) {
			LOG.info("Cannot access default ctor {}:", clazz, ex);
			return false;
		} catch (NoSuchMethodException ex) {
			LOG.trace("Cannot get default ctor of {}:", clazz, ex);
			LOG.debug("{} has no default constructor.", clazz);
			return false;
		}
	}

	private String[] getClasspathClassArray() {
        Set<String> classSet = this.classpathDigger.getClasses();
		return classSet.toArray(new String[0]);
	}

	/**
	 * Gets the loaded classpath (without the bootclasspath) as sorted set.
	 * Since v1.5 this method is about 5 times faster than the old
	 * implementation by caching the result from previous calls.
	 * <p>
	 * Here the result from running some benchmarks with the old and new
	 * implementation: Profiling with JMH gives the following result:
	 * </p>
	 *
	 * <pre>
	 * Implementation | Mode  | Score             | Error (99.9%) | Unit
	 * ---------------+-------+-------------------+---------------+-------
	 * new (v1.5)     | thrpt | 6.495745459743192 | 0.14376149437 | ops/ms
	 * old (v1.4)     | thrpt | 1.167003226917474 | 0.01126705718 | ops/ms
	 * new (v1.5)     | avgt  | 0.149637883043804 | 0.00084667052 | ms/op
	 * old (v1.4)     | avgt  | 0.860845377760151 | 0.02615918865 | ms/op
	 * </pre>
	 * <p>
	 * As framework for the benchmarks
	 * <a href="http://openjdk.java.net/projects/code-tools/jmh/">JMH</a> was
	 * used.
	 * </p>
	 *
	 * @return the loaded classpath (excluding the bootclasspath)
	 */
	public SortedSet<URI> getUsedClasspathSet() {
		// TODO: optimize it, use BoringClassLoader#getUsedClasspath()
		SortedSet<URI> usedClasspathSet = new TreeSet<>();
		List<Class<?>> loadedClassList = this.getLoadedClassList();
		for (Class<?> clazz : loadedClassList) {
			URI classpathUri = this.getClasspathOf(clazz);
			if (!NULL_URI.equals(classpathUri)) {
				usedClasspathSet.add(classpathUri);
			}
		}
		return usedClasspathSet;
	}

	private URI getClasspathOf(final Class<?> clazz) {
		URI classpathUri = this.usedClasspathCache.get(clazz);
		if (classpathUri == null) {
			URI classUri = this.whichClass(clazz);
			if (classUri == null) {
				LOG.trace("URI for {} was not found (probably a proxy class).", clazz);
				classpathUri = NULL_URI;
			} else {
				classpathUri = ClasspathHelper.getParent(classUri, clazz);
			}
			this.usedClasspathCache.put(clazz, classpathUri);
		}
		return classpathUri;
	}

	/**
	 * It might be that the used classpath changes during the calculation of it.
	 * So it is only a good approximation how it looks at the moment of the
	 * call.
	 *
	 * @return the used classpath
	 */
	@Override
	public String[] getUsedClasspath() {
		URI[] uris = this.getUsedClasspathURIs();
		return toStringArray(uris);
	}

    /**
     * It might be that the used classpath changes during the calculation of it.
     * So it is only a good approximation how it looks at the moment of the
     * call.
     *
     * @return the used classpath as array of URIs
     */
	public URI[] getUsedClasspathURIs() {
		LOG.debug("calculating used classpath...");
		SortedSet<URI> classpathSet = this.getUsedClasspathSet();
		return classpathSet.toArray(new URI[0]);
	}

	/**
	 * The unused classpath is the path which are not used by the classloader.
	 *
	 * @return the unused classpath (sorted)
	 */
	@Override
	public String[] getUnusedClasspath() {
		LOG.debug("calculating unused classpath...");
		SortedSet<String> unused = new TreeSet<>();
		LOG.trace(Arrays.class + " loaded (to get corrected used classpath");
		String[] used = this.getUsedClasspath();
		for (String s : classpath) {
			String path = new File(s).getAbsolutePath();
			if (Arrays.binarySearch(used, path) < 0) {
				unused.add(path);
			}
		}
		String[] a = new String[unused.size()];
		return unused.toArray(a);
	}

	/**
	 * The unused classpath is the path which are not used by the classloader.
	 *
	 * @return the unused classpath (sorted)
	 */
	public SortedSet<URI> getUnusedClasspathSet() {
		return toUriSet(this.getUnusedClasspath());
	}

	/**
	 * To get the boot classpath the sytem property "sun.boot.class.path" is
	 * used to get them. This will work of course only for the SunVM and only
	 * till Java 8.
	 *
	 * @return the boot classpath as String array
	 */
	@Override
	public String[] getBootClasspath() {
		return this.classpathDigger.getBootClasspath();
	}

	/**
	 * The classpath is stored in the system property "java.class.path". And
	 * this is the classpath which will be returned.
	 *
	 * @return the classpath as String array
	 */
	@Override
	public String[] getClasspath() {
		return this.classpath.clone();
	}

	/**
	 * The classpath is stored in the system property "java.class.path" as
	 * sorted set.
	 *
	 * @return the classpath set
	 * @since 2.0
	 */
	public SortedSet<URI> getClasspathSet() {
		return toUriSet(this.classpath);
	}
	
	private static SortedSet<URI> toUriSet(String[] array) {
		SortedSet<URI> set = new TreeSet<>();
		for (String s : array) {
			File f = new File(s);
			if (f.isDirectory()) {
				set.add(f.toURI());
			} else {
				set.add(URI.create("jar:" + f.toURI()));
			}
		}
		return set;
	}

	/**
	 * Gets the serial version uid.
	 *
	 * @param classname name of the class
	 * @return the serialVersionUID
	 * @throws IllegalAccessException if class can't be accessed
	 */
	@Override
	public Long getSerialVersionUID(final String classname) throws IllegalAccessException {
		try {
			Class<?> clazz = Class.forName(classname);
			return getSerialVersionUID(clazz);
		} catch (ClassNotFoundException e) {
			LOG.debug("Class '" + classname + "' not found:", e);
			return null;
		}
	}

	/**
	 * Gets the serial version uid.
	 *
	 * @param clazz
	 *            the clazz
	 *
	 * @return the serial version uid
	 *
	 * @throws IllegalAccessException
	 *             the illegal access exception
	 */
	public Long getSerialVersionUID(final Class<?> clazz) throws IllegalAccessException {
		try {
			Field field = ReflectionHelper.getField(clazz, "serialVersionUID");
			return (Long) field.get(null);
		} catch (NoSuchFieldException ex) {
			LOG.debug(clazz + " has no serialVersionUID:", ex);
			return null;
		}
	}

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
	 * @param clazz
	 *            the clazz
	 *
	 * @return the attribute entries of the Manifest (or emtpy array if no
	 *         Manifest or no attributes are found)
	 */
	public String[] getManifestEntries(final Class<?> clazz) {
		return this.getManifestEntries(clazz.getName());
	}

	/**
	 * If a MANIFEST is found for a given class the attributes in this file
	 * should be returned as a string array. E.g. for commons-lang-2.3.jar the
	 * string array may look like
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
	 * @param classname
	 *            (must be in the classpath, otherwise you'll get a
	 *            IllegalArgumentException)
	 *
	 * @return the attribute entries of the Manifest (or emtpy array if no
	 *         Manifest or no attributes are found)
	 */
	@Override
	public String[] getManifestEntries(final String classname) {
		URI classpathURI = whichClassPath(classname);
		if (classpathURI == null) {
			throw new IllegalArgumentException(classname + " not found in classpath");
		}
		File path = Converter.toFile(classpathURI);
		return getManifestEntries(path);
	}

	/**
	 * Gets the manifest entries.
	 *
	 * @param path e.g. a classpath or a JAR file
	 * @return the attribute entries of the manifest file
	 */
	private String[] getManifestEntries(final File path) {
		if (path.isFile()) {
			try {
				return getManifestEntries(new JarFile(path));
			} catch (IOException ioe) {
				LOG.info("No manifest found in " + path + ":", ioe);
				return new String[0];
			}
		}
		File manifestFile = new File(path, "META-INF/MANIFEST.MF");
		if (!manifestFile.exists()) {
			LOG.debug("File '{}' does not exist.", manifestFile);
			return new String[0];
		}
		try (InputStream istream = new FileInputStream(manifestFile)) {
			Manifest manifest = new Manifest(istream);
			return getManifestEntries(manifest);
		} catch (IOException ioe) {
			LOG.info("Cannot read '" + manifestFile + "':", ioe);
			return new String[0];
		}
	}

	/**
	 * We look for the manifest file for the given JAR file.
	 *
	 * @param jarfile
	 *            the jarfile
	 *
	 * @return the entries as string array (may be an empty array if no manifest
	 *         file was found)
	 */
	private String[] getManifestEntries(final JarFile jarfile) {
		Manifest manifest;
		try {
			manifest = jarfile.getManifest();
		} catch (IOException ioe) {
			LOG.info("No manifest found in '" + jarfile + "':", ioe);
			return new String[0];
		}
		if (manifest == null) {
			LOG.debug("No manifest found in '{}'.", jarfile);
			return new String[0];
		}
		return getManifestEntries(manifest);
	}

	/**
	 * Gets the manifest entries.
	 *
	 * @param manifest
	 *            the manifest
	 *
	 * @return the manifest entries
	 */
	private String[] getManifestEntries(final Manifest manifest) {
		Attributes attributes = manifest.getMainAttributes();
		String[] manifestEntries = new String[attributes.size()];
		Set<Map.Entry<Object, Object>> entries = attributes.entrySet();
		Iterator<Map.Entry<Object, Object>> iterator = entries.iterator();
		for (int i = 0; i < manifestEntries.length; i++) {
			Map.Entry<Object, Object> entry = iterator.next();
			manifestEntries[i] = entry.getKey() + ": " + entry.getValue();
		}
		return manifestEntries;
	}

	/**
	 * Looks for each doublet if it has a different doublets. For the
	 * performance reason it looks in the incompatibleClassList from the last
	 * time if it is already found. This is done because normally the number of
	 * incompatible classes does not decrease.
	 *
	 * @return a sorted list of incompatible classes
	 */
    @SuppressWarnings("squid:S2250")
	public synchronized List<Class<?>> getIncompatibleClassList() {
		List<Class<?>> doublets = this.getDoubletList();
		for (Class<?> clazz : doublets) {
			if (incompatibleClassList.contains(clazz)) {
				continue;
			}
			String resource = Converter.classToResource(clazz.getName());
			Enumeration<URL> resources = getResources(resource);
			try {
				URL url = resources.nextElement();
				ArchivEntry archivEntry = new ArchivEntry(url);
				while (resources.hasMoreElements()) {
					url = resources.nextElement();
					ArchivEntry doubletEntry = new ArchivEntry(url);
					if (!archivEntry.equals(doubletEntry)) {
						incompatibleClassList.add(clazz);
						break;
					}
				}
			} catch (NoSuchElementException nse) {
				LOG.warn("{} is not added to incompatible class list:", clazz, nse);
			}
		}
		return Collections.unmodifiableList(incompatibleClassList);
	}

	/**
	 * Incompatible classes are doublets with different byte codes.
	 *
	 * @return doublet classes with different byte codes.
	 */
	@Override
	public String[] getIncompatibleClasses() {
		LOG.debug("Calculating incompatible classes...");
		List<Class<?>> classList = this.getIncompatibleClassList();
		String[] classes = new String[classList.size()];
		for (int i = 0; i < classes.length; i++) {
			classes[i] = classList.get(i).toString();
		}
		return classes;
	}

	/**
	 * Gets the incompatible classpath.
	 *
	 * @return the classpathes where incompatible classes were found
	 */
	@Override
	public String[] getIncompatibleClasspath() {
		URI[] classpathURIs = this.getIncompatibleClasspathURIs();
		return toStringArray(classpathURIs);
	}

	/**
	 * Gets the incompatible classpath as array of URIs.
	 *
     * @return the classpathes where incompatible classes were found
	 */
	public URI[] getIncompatibleClasspathURIs() {
		LOG.debug("calculating incompatible-classpath...");
		Set<URI> classpathSet = getClasspathSet(this.getIncompatibleClassList());
		return classpathSet.toArray(new URI[0]);
	}

	/**
	 * You can register the instance as shutdown hook. If the VM is terminated
	 * the attributes are logged and dumped to a text file in the tmp directory.
	 *
	 * @see #logMe()
	 * @see #dumpMe()
	 * @see #addMeAsShutdownHook()
	 * @see #removeMeAsShutdownHook()
	 */
	public static synchronized void addAsShutdownHook() {
		INSTANCE.addMeAsShutdownHook();
	}

	/**
	 * If you have registered the instance you can now de-register it.
	 *
	 * @see #addAsShutdownHook()
	 * @see #removeMeAsShutdownHook()
	 * @since 1.0
	 */
	public static synchronized void removeAsShutdownHook() {
		INSTANCE.removeMeAsShutdownHook();
	}

	/**
	 * Logs the different array to the log output.
	 */
	@Override
	public void logMe() {
		try {
			StringWriter writer = new StringWriter();
			this.dumpMe(writer);
			LOG.info(writer.toString());
		} catch (IOException cannothappen) {
			LOG.warn("Will only dump classloader info:", cannothappen);
			LOG.info(this.getClassloaderInfo());
		}
	}

	/**
	 * This operation dumps the different MBean attributes to the given
	 * directory.
	 * <p>
	 * Note: Till 1.5 the different attributes are dumped to a single file. Now
	 * each attribute is dumped into its own file. The given file parameter is
	 * the directory for these files.
	 * </p>
	 * <p>
	 * To dump the different attributes (which are mainly given as array) we use
	 * the internal 'dumpArray()' method. This method uses reflection to call
	 * the corresponding getter method for that attribute. The reason for this
	 * approach is that we can better control exceptions if they happens in one
	 * of the getter method. If it happens it is ignored (but logged) and the
	 * next attribute is dumped.
	 * </p>
	 *
	 * @param dumpDir
	 *            the directory where the attributes are dumped to.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @since 1.5
	 */
	@Override
	public void dumpMe(final File dumpDir) throws IOException {
	    this.dump(dumpDir, DUMP_GETTERS);
		dumpClassloaderInfo(dumpDir);
		copyResource("CpMonREADME.txt", new File(dumpDir, "README.txt"));
	}

	/**
	 * This operation dumps the different MBean attributes. Use this method if
	 * you want to have all relevant infos in <em>one</em> stream. This is e.g.
	 * used by the {@link #logMe()} method.
	 *
	 * @param unbuffered
	 *            the writer (can be unbuffered)
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void dumpMe(final Writer unbuffered) throws IOException {
		BufferedWriter writer = new BufferedWriter(unbuffered);
		dump(writer, DUMP_GETTERS);
		dumpClassloaderInfo(writer);
		writer.flush();
	}

	private void dumpClassloaderInfo(final File dir) throws IOException {
		File dumpFile = new File(dir, "ClassloaderInfo.txt");
		LOG.debug("Dumping classloader info to {}...", dumpFile);
		try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(dumpFile), StandardCharsets.UTF_8)) {
			dumpClassloaderInfo(new BufferedWriter(writer));
			writer.flush();
		}
	}

	private void dumpClassloaderInfo(final BufferedWriter writer) throws IOException {
		dumpHeadline(writer, "ClassloaderInfo");
		writer.write(this.getClassloaderInfo());
		writer.newLine();
		writer.newLine();
		dumpHeadline(writer, "=== ClassLoaderDetails ===");
		writer.write(this.getClassLoaderDetails());
		writer.newLine();
		dumpHeadline(writer, "ClassloaderInfo (end)");
		writer.flush();
	}

	/**
	 * We don't want to use the toString() implementation of the Thread class so
	 * we use our own one.
	 *
	 * @return the string
	 * @see Thread#toString()
	 */
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " for " + this.cloader;
	}

//	/**
//	 * This main method is only for testing the ClasspathMonitor together with
//	 * the 'jconsole'. On a Java5 VM start it with the following options:
//	 * -Dcom.sun.management.jmxremote.local.only=false
//	 * -Dcom.sun.management.jmxremote
//	 *
//	 * @param args
//	 *            the args
//	 * @throws JMException
//	 *             the JM exception
//	 */
//	public static void main(final String[] args) throws JMException {
//		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
//		ObjectName name = new ObjectName("patterntesting.runtime.monitor:type=ClasspathMonitor");
//		ClasspathMonitor hello = new ClasspathMonitor();
//		Object mbean = new AnnotatedStandardMBean(hello, ClasspathMonitorMBean.class);
//		mbs.registerMBean(mbean, name);
//		ThreadUtil.sleep(300, TimeUnit.SECONDS);
//	}

	/**
	 * Is multi threading enabled? Multi threading is automatically enabled if
	 * more than one processor is found. Otherwise you can use set via the
	 * system property "multiThreadingEnabled=true" to activate it.
	 *
	 * @return true if multi threading is enabled for this class.
	 * @see ClasspathMonitorMBean#isMultiThreadingEnabled()
	 * @since 0.9.7
	 */
	@Override
	public boolean isMultiThreadingEnabled() {
		return this.doubletDigger.isMultiThreadingEnabled();
	}

	/**
	 * Here you can enable or disable the (experimental) multi threading mode to
	 * see if it is really faster on a mult-core machine.
	 *
	 * @param enabled
	 *            the enabled
	 * @see ClasspathMonitorMBean#setMultiThreadingEnabled(boolean)
	 * @since 0.9.7
	 */
	@Override
	public void setMultiThreadingEnabled(final boolean enabled) {
		this.doubletDigger.setMultiThreadingEnabled(enabled);
	}

}
