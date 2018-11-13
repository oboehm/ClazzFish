/*
 * $Id: StackTraceScanner.java,v 1.11 2016/12/18 20:19:36 oboehm Exp $
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
 * (c)reated 24.01.2014 by oliver (ob@oasd.de)
 */

package clazzfish.monitor.util;

import clazzfish.monitor.exception.NotFoundException;
import org.apache.commons.lang3.ArrayUtils;

import java.util.regex.Pattern;

/**
 * This class allows you to scan the stacktrace for different stuff.
 *
 * @author oliver
 * @since 1.4.1 (24.01.2014)
 */
public class StackTraceScanner {

	/** Utility class - no need to instantiate it. */
	private StackTraceScanner() {
	}

	/**
	 * Gets the caller class by examing the stacktrace.
	 *
	 * @return the caller class
	 */
	public static Class<?> getCallerClass() {
		return getCallerClass(new Pattern[0]);
	}

	/**
	 * Gets the caller class by examing the stacktrace.
	 *
	 * @param excluded
	 *            a list of filters which should be not considered as caller
	 * @return the caller of
	 */
	public static Class<?> getCallerClass(final Pattern... excluded) {
		return getCallerClass(excluded, new Class<?>[0]);
	}

	/**
	 * Gets the caller class by examing the stacktrace.
	 *
	 * @param excludedMethods
	 *            the excluded methods
	 * @param excludedClasses
	 *            the excluded classes
	 * @return the caller class
	 */
	public static Class<?> getCallerClass(final Pattern[] excludedMethods, final Class<?>... excludedClasses) {
		StackTraceElement[] stackTrace = getCallerStackTrace(excludedMethods, excludedClasses);
		String classname = stackTrace[0].getClassName();
		try {
			return Class.forName(classname);
		} catch (ClassNotFoundException ex) {
			throw new NotFoundException(classname, ex);
		}
	}

	/**
	 * Gets the caller stack trace of the method or constructor which calls it.
	 *
	 * @return the caller stack trace
	 * @since 1.4.2 (17.05.2014)
	 */
	public static StackTraceElement[] getCallerStackTrace() {
		return getCallerStackTrace(new Pattern[0]);
	}

	/**
	 * Gets the caller stack trace of the method or constructor which calls it.
	 *
	 * @param excluded
	 *            a list of filters which should be not considered as caller
	 * @return the caller stack trace
	 * @since 1.4.2 (17.05.2014)
	 */
	public static StackTraceElement[] getCallerStackTrace(final Pattern... excluded) {
		return getCallerStackTrace(excluded, new Class<?>[0]);
	}

	/**
	 * Gets the caller stack trace of the method or constructor which calls it.
	 *
	 * @param excludedMethods
	 *            the excluded methods
	 * @param excludedClasses
	 *            the excluded classes
	 * @return the caller stack trace
	 * @since 1.4.2 (17.05.2014)
	 */
	public static StackTraceElement[] getCallerStackTrace(final Pattern[] excludedMethods,
			final Class<?>... excludedClasses) {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		int i = 2;
		String scannerClassName = StackTraceScanner.class.getName();
		for (; i < stackTrace.length - 1; i++) {
			if (!scannerClassName.equals(stackTrace[i].getClassName())) {
				break;
			}
		}
		for (; i < stackTrace.length - 1; i++) {
			if (!matches(stackTrace[i].getMethodName(), excludedMethods)
					&& !matches(stackTrace[i].getClassName(), excludedClasses)) {
				break;
			}
		}
		return ArrayUtils.subarray(stackTrace, i, stackTrace.length);
	}

	private static boolean matches(final String methodName, final Pattern... excluded) {
		for (Pattern anExcluded : excluded) {
			if (anExcluded.matcher(methodName).matches()) {
				return true;
			}
		}
		return false;
	}

	private static boolean matches(final String className, final Class<?>... excluded) {
		for (Class<?> anExcluded : excluded) {
			if (className.equals(anExcluded.getName())) {
				return true;
			}
		}
		return false;
	}
}
