/*
 * Copyright (c) 2014-2018 by Oliver Boehm
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
 * (c)reated 26.08.2014 by oliver (ob@oasd.de)
 */

package clazzfish.monitor;

import clazzfish.monitor.util.ReflectionHelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.*;

import java.net.URLClassLoader;

/**
 * This enum type can detect if a classloader is a Tomcat, WebLogic or Websphere
 * classloader. Originally it was part of the PatternTesting project.
 *
 * @author oliver
 */
public enum ClassloaderType {

	/** Unknown classloader. */
	UNKNOWN("unknown"),

	/** The default classloader of the Sun VM. */
	SUN("sun.misc.Launcher$AppClassLoader"),

	/** The new classloader of Open-JDK since Java 11. */
	JDK("jdk.internal.loader.ClassLoaders$AppClassLoader"),

	/** The URLClassLoader where many appserver are based on. */
	NET(URLClassLoader.class),

	/** Tomcat's classloader (till Tomcat 7). */
	TOMCAT("org.apache.catalina.loader.WebappClassLoader", true, "repositoryURLs"),

    /** Tomcat-8's classloader. */
    TOMCAT8("org.apache.catalina.loader.WebappClassLoaderBase", true, "localRepositories"),

	/** Weblogic's classloader. */
	WEBLOGIC("weblogic.utils.classloaders.ChangeAwareClassLoader", true),

	/** The Websphere classloader from IBM. */
	WEBSPHERE("com.ibm.ws.classloader.CompoundClassLoader", true, "localClassPath"),

	/** Surefire provides an isolated classloader. */
	SUREFIRE("org.apache.maven.surefire.booter.IsolatedClassLoader");

	private static final Logger LOG = LoggerFactory.getLogger(ClassloaderType.class);
	private Class<? extends ClassLoader> classLoader = null;
	private final String classname;
	private final String fieldname;
	private final boolean web;

	ClassloaderType(final Class<? extends ClassLoader> classLoader) {
		this(classLoader.getName(), false);
		this.classLoader = classLoader;
	}

	ClassloaderType(final String classname) {
		this(classname, false);
	}

	ClassloaderType(final String classname, final boolean web) {
		this(classname, web, "");
	}

    ClassloaderType(final String classname, final boolean web, final String fieldname) {
        this.classname = classname;
        this.fieldname = fieldname;
        this.web = web;
    }
    
    /**
     * Here you can ask if the classloader type is a web classloader or not.
     * 
     * @since 1.7.2
     * @return true for Tomcat or application servers
     */
    public boolean isWeb() {
        return web;
    }

	/**
	 * Converts the given classloader into the corresponding type.
	 * <p>
	 * We use also the fieldname to match the possible classloader because in
	 * some case (e.g. for Tomcat) the classname is not sufficient.
	 * </p>
	 *
	 * @param cloader the classloader
	 * @return the classpath type
	 */
	public static ClassloaderType toClassloaderType(final ClassLoader cloader) {
		ClassloaderType[] clTypes = ClassloaderType.values();
		for (ClassloaderType type : clTypes) {
            if (isAssignableClassloader(cloader.getClass(), type)
                    && (StringUtils.isEmpty(type.fieldname)
                            || ReflectionHelper.hasField(cloader, type.fieldname))) {
				return type;
			}
		}
        for (ClassloaderType type : clTypes) {
            if (ReflectionHelper.hasField(cloader, type.fieldname)) {
                return type;
            }
            if ((type.classLoader != null) && type.classLoader.isAssignableFrom(cloader.getClass())) {
                return type;
            }            
        }
		LOG.debug("{} is considered to be a default classloader (SUN).", cloader);
		return SUN;
	}

    @SuppressWarnings("unchecked")
    private static boolean isAssignableClassloader(final Class<? extends ClassLoader> cloaderClass, ClassloaderType type) {
        if (cloaderClass.getName().equals(type.classname)) {
            return true;
        }
        Class<?> superclass = cloaderClass.getSuperclass();
        if ((superclass == null) || superclass.equals(URLClassLoader.class)) {
            return false;
        } else {
            return isAssignableClassloader((Class<ClassLoader>) superclass, type);
        }
    }
	
	/**
     * Looks, what type of classloader the current thread is using.
     * 
	 * @since 1.7.2
	 * @return e.g. {@link #SUN} for the default classloader
	 */
	public static ClassloaderType getCurrentClassloaderType() {
	    ClassLoader cloader = Thread.currentThread().getContextClassLoader();
	    return ClassloaderType.toClassloaderType(cloader);
	}

	/**
	 * Checks if the given classloader is known and therefore supported. We do
	 * not use the fieldname to check if the classloader is supported because we
	 * do not know if it is really the correct classloader.
	 *
	 * @param classloaderName
	 *            the classloader name
	 * @return true, if is supported
	 */
	public static boolean isSupported(final String classloaderName) {
		ClassloaderType[] types = ClassloaderType.values();
		for (ClassloaderType type : types) {
			if (classloaderName.equals(type.classname)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Gets the classpath from.
	 *
	 * @param cloader
	 *            the cloader
	 * @return the classpath from
	 */
	public Object getClasspathFrom(final ClassLoader cloader) {
		try {
			return ReflectionHelper.getFieldValue(cloader, this.fieldname);
		} catch (ReflectiveOperationException ex) {
			throw new IllegalArgumentException(cloader + ": cannot access field '" + this.fieldname + "'", ex);
		}
	}

}
