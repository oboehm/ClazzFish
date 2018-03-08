/**
 * Copyright (c) 2009-2018 by Oliver Boehm
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
 * (c)reated 27.07.2009 by oliver (ob@oasd.de)
 */

package clazzfish.monitor.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

/**
 * This class contains some helper classes which are used inside
 * ClasspathMonitor <b>and</b> ClasspathDigger. Some of the methods were
 * formerly placed in ClasspathMonitor but also needed by ClasspathDigger. To
 * avoid dependencies from ClasspathDigger to ClasspathMonitor the common
 * methods were moved to this class here.
 * <p>
 * Originally this class was part of the PatternTesting project.
 * </p>
 *
 * @author oliver
 */
public final class ClasspathHelper {

	private static final Logger LOG = LoggerFactory.getLogger(ClasspathHelper.class);

	/** to avoid instantiation */
	private ClasspathHelper() {
	}

	/**
	 * Don't use @ProfileMe here! This method is also used when ProfileStatistic
	 * is set up!.
	 *
	 * @param path
	 *            e.g. "jar:file:/a/b/c.jar!/d/e.class
	 * @param resource
	 *            e.g. "/d/e.class"
	 * @return e.g. "jar:file:/a/b/c.jar"
	 */
	public static URI getParent(final URI path, final String resource) {
		String p = removeTrailingSlash(path.toString());
		String res = removeTrailingSlash(resource);
		String parent = StringUtils.removeEnd(p, res);
		parent = removeTrailingSlash(parent);
		if (parent.endsWith("!")) {
			parent = parent.substring(0, (parent.length() - 1));
		}
		return Converter.toURI(parent);
	}

	private static String removeTrailingSlash(final String s) {
		if (s.endsWith("/") || s.endsWith("\\")) {
			return s.substring(0, (s.length() - 1));
		}
		return s;
	}

	/**
	 * Gets the parent of the given class.
	 *
	 * @param path
	 *            e.g. "jar:file:/a/b/c.jar!/d/e.class
	 * @param clazz
	 *            e.gl. "d.e.class"
	 * @return e.g. "jar:file:/a/b/c.jar"
	 */
	public static URI getParent(final URI path, final Class<?> clazz) {
		String resource = Converter.toResource(clazz);
		return getParent(path, resource);
	}

}
