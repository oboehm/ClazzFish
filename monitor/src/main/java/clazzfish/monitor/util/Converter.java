/*
 * $Id: Converter.java,v 1.50 2016/12/18 20:19:36 oboehm Exp $
 *
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
 * (c)reated 19.01.2009 by oliver (ob@oasd.de)
 */
package clazzfish.monitor.util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Array;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Time;
import java.text.*;
import java.util.*;

/**
 * The Class Converter to convert objects from one type to another type.
 * <p>
 * This class was copied from the PatternTesting project.
 * </p>
 *
 * @author <a href="boehm@javatux.de">oliver</a>
 */
public final class Converter {

	private static final Logger LOG = LoggerFactory.getLogger(Converter.class);

	/** The different date patterns (date only) which are used by toDate(). */
	private static final String[] datePatterns = { "dd-MMM-yyyy", "dd-MM-yyyy", "yyyy-MMM-dd", "yyyy-MM-dd",
			"MMM-dd-yyyy", "dd MMM yyyy", "dd MM yyyy", "yyyy MMM dd", "yyyy MM dd", "MMM dd yyyy", "dd.MMM.yyyy",
			"dd.MM.yyyy", "yyyy.MMM.dd", "MMM.dd.yyyy" };

	/** The different date patterns including the time. */
	private static final String[] dateTimePatterns = getDateTimePatterns();

	/**
	 * To avoid that this class is instantiated.
	 */
	private Converter() {
	}

	/**
	 * Gets the memory as string.
	 *
	 * @param mem
	 *            the mem
	 *
	 * @return the memory as string
	 */
	public static String getMemoryAsString(final long mem) {
		if (mem < 1000) {
			return mem + " bytes";
		} else if (mem < 0x100000) {
			return (mem + 512) / 1024 + " KB";
		} else {
			return (mem + 0x80000) / 0x100000 + " MB";
		}
	}

	/**
	 * Gets the time as string with the corresponding unit. Unit can be "ms"
	 * (for milliseconds) or "seconds".
	 *
	 * @param timeInMillis
	 *            the time in millis
	 * @return the time as string
	 * @since 1.2.20
	 */
	public static String getTimeAsString(final long timeInMillis) {
		if (timeInMillis > 300000L) {
			return ((timeInMillis + 30000L) / 60000L) + " minutes";
		} else if (timeInMillis > 5000L) {
			return ((timeInMillis + 500L) / 1000L) + " seconds";
		}
		return timeInMillis + " ms";
	}

	/**
	 * Gets the time as string with the corresponding unit. Unit can be "ms"
	 * (for milliseconds) or "seconds".
	 *
	 * @param timeInMillis
	 *            the time in millis
	 * @return the time as string
	 * @since 1.4.2
	 */
	public static String getTimeAsString(final double timeInMillis) {
		return getTimeAsString(timeInMillis, Locale.getDefault());
	}

	/**
	 * Gets the time as string with the corresponding unit. Unit can be "ms"
	 * (for milliseconds) or "seconds".
	 *
	 * @param timeInMillis
	 *            the time in millis
	 * @param locale
	 *            the locale
	 * @return the time as string
	 * @since 1.4.2
	 */
	public static String getTimeAsString(final double timeInMillis, final Locale locale) {
		if (timeInMillis > 1.0) {
			return getTimeAsString((long) timeInMillis);
		}
		Format nf = new DecimalFormat("#.###", new DecimalFormatSymbols(locale));
		return nf.format(timeInMillis) + " ms";
	}

	/**
	 * Converts a classname (e.g. "java.lang.String") into a resource
	 * ("/java/lang/String.class").).
	 *
	 * @param name
	 *            e.g. "java.lang.String"
	 *
	 * @return e.g. "/java/lang/String.class"
	 */
	public static String classToResource(final String name) {
		if (name == null) {
			return null;
		}
		return name.replaceAll("\\.", "/") + ".class";
	}

	/**
	 * To resource.
	 *
	 * @param clazz
	 *            the clazz
	 *
	 * @return the string
	 */
	public static String toResource(final Class<?> clazz) {
		return classToResource(clazz.getName());
	}

	/**
	 * Converts a package name (e.g. "java.lang") into a resource
	 * ("/java/lang").).
	 *
	 * @param name
	 *            e.g. "java.lang"
	 *
	 * @return e.g. "/java/lang"
	 */
	public static String packageToResource(final String name) {
		if (name == null) {
			return null;
		}
		return name.replaceAll("\\.", "/");
	}

	/**
	 * To resource.
	 *
	 * @param p
	 *            the p
	 *
	 * @return the string
	 */
	public static String toResource(final Package p) {
		return packageToResource(p.getName());
	}

	/**
	 * Converts a resource (e.g. "/java/lang/String.class") into its classname
	 * ("java.lang.String").
	 *
	 * @param name
	 *            e.g. "/java/lang/String.class"
	 *
	 * @return e.g. "java.lang.String"
	 */
	public static String resourceToClass(final String name) {
		if (name == null) {
			return null;
		}
		if (name.endsWith(".class")) {
			int lastdot = name.lastIndexOf('.');
			String classname = name.substring(0, lastdot).replaceAll("[/\\\\]", "\\.");
			if (classname.startsWith(".")) {
				classname = classname.substring(1);
			}
			return classname;
		} else {
			return name;
		}
	}

	/**
	 * If a URL contains illegal characters for an URI (like blanks) this should
	 * be automatically converted (e.g. to "%20")
	 *
	 * @param url
	 *            the URL to be converted
	 * @return the given URL as URI
	 */
	public static URI toURI(final URL url) {
		try {
			return normalize(url.toURI());
		} catch (URISyntaxException ex) {
			LOG.trace("Cannot convert {} to URI:", url, ex);
			return toURI(url.toExternalForm());
		}
	}

	/**
	 * If a URL contains illegal characters for an URI (like blanks) this should
	 * be automatically converted (e.g. to "%20")
	 *
	 * @param uri
	 *            the URL to be converted
	 * @return the given URL as URI
	 */
	public static URI toURI(final String uri) {
		try {
            String converted = uri.replaceAll(" ", "%20");
			return normalize(new URI(converted.replaceAll("//", "/")));
		} catch (URISyntaxException ex) {
            LOG.trace("Cannot convert '{}' to URI:", uri, ex);
            throw new IllegalArgumentException(uri + " is not a valid URI", ex);
		}
	}

	private static URI normalize(URI uri) {
		if ("file".equalsIgnoreCase(uri.getScheme())) {
			return new File(uri).toURI();
		} else {
			return uri;
		}
	}

	/**
	 * Converts an URI into a file.
	 *
	 * @param uri e.g. URI("file:/tmp")
	 * @return e.g. File("/tmp")
	 */
	public static File toFile(final URI uri) {
		try {
			return toCanonicalFile(uri);
		} catch (IllegalArgumentException iae) {
			if ("jar".equalsIgnoreCase(uri.getScheme())) {
				try {
					String fileScheme = uri.toString().substring(4);
					return toFile(new URI(fileScheme));
				} catch (URISyntaxException ex) {
					LOG.trace("Cannot convert '{}' to a file:", uri, ex);
					throw new IllegalArgumentException("not a File: " + uri, iae);
				}
			}
		}
		String path = uri.getPath();
		if (StringUtils.isEmpty(path)) {
			path = StringUtils.substringAfterLast(uri.toString(), ":");
		}
		return new File(path);
	}

	private static File toCanonicalFile(final URI uri) {
		File file = new File(uri);
		try {
			return file.getCanonicalFile();
		} catch (IOException ioe) {
			LOG.trace("Will use {} as return value:", file, ioe);
			return file;
		}
	}

	/**
	 * Converts an URI into a file.
	 *
	 * @param uri
	 *            the uri as string
	 * @return the file
	 */
	public static File toFile(final String uri) {
		try {
			return toFile(new URI(uri));
		} catch (URISyntaxException ex) {
			LOG.trace("'{}' will be handled as file name:", uri, ex);
			return new File(uri);
		}
	}

	/**
	 * Converts an URI into a file and returns it as absolute pathname.
	 *
	 * @param uri
	 *            e.g. URI("file:/tmp")
	 *
	 * @return e.g. "/tmp"
	 */
	public static String toAbsolutePath(final URI uri) {
		File file = toFile(uri);
		return file.getAbsolutePath();
	}

	/**
	 * Converts an object into its string representation. A null object is
	 * mapped to "" (empty string).
	 *
	 * @param obj
	 *            the obj
	 *
	 * @return "" if the given object is null
	 */
	public static String toString(final Object obj) {
		if (obj == null) {
			return "";
		}
		try {
			if (obj instanceof Object[]) {
				return toString((Object[]) obj);
			}
			if (obj instanceof Enumeration<?>) {
				return toString((Enumeration<?>) obj);
			}
			return obj.toString();
		} catch (RuntimeException e) {
			LOG.debug("Returning empty string because of " + e);
			return "";
		}
	}

	/**
	 * To string.
	 *
	 * @param enumeration
	 *            the enumeration
	 * @return the string
	 */
	public static String toString(final Enumeration<?> enumeration) {
		if (!enumeration.hasMoreElements()) {
			return "";
		}
		StringBuilder sbuf = new StringBuilder();
		while (enumeration.hasMoreElements()) {
			sbuf.append(" ,").append(enumeration.nextElement());
		}
		return sbuf.substring(2);
	}

	/**
	 * Converts an object to its toString representation. If the resuulting
	 * string would be too long it will be abbreviated.
	 * <p>
	 * If the resulting toString representation looks like the default
	 * implementation of {@link Object#toString()} the package name will be
	 * removed from the result. For other toString results
	 * {@link StringUtils#abbreviate(String, int)} will be used to cut it if
	 * necessary.
	 * </p>
	 *
	 * @param obj
	 *            the obj
	 *
	 * @return the string
	 */
	public static String toShortString(final Object obj) {
		if (obj == null) {
			return "";
		}
		if (obj instanceof Class<?>) {
			return ((Class<?>) obj).getSimpleName();
		}
		if (obj instanceof Number) {
			return toShortString((Number) obj);
		}
		if (obj instanceof Time) {
			return toShortString((Time) obj);
		}
		if (obj.getClass().isArray()) {
			return toShortArrayString(obj);
		}
		return toAbbreviatedString(obj);
	}

	private static String toAbbreviatedString(final Object obj) {
		String s = toString(obj);
		if (s.startsWith(obj.getClass().getName())) {
			int lengthPackagename = obj.getClass().getPackage().getName().length();
			return s.substring(lengthPackagename + 1);
		}
		return StringUtils.abbreviate(s, 24);
	}

	private static String toShortArrayString(final Object obj) {
		StringBuilder sbuf = new StringBuilder();
		int n = Array.getLength(obj);
		if (n == 0) {
			return "";
		}
		sbuf.append(Array.get(obj, 0));
		for (int i = 1; i < n; i++) {
			sbuf.append(", ");
			if (i > 2) {
				sbuf.append("...");
				break;
			}
			sbuf.append(Array.get(obj, i));
		}
		return sbuf.toString();
	}

	/**
	 * To short string.
	 *
	 * @param number
	 *            the number
	 * @return the string
	 */
	public static String toShortString(final Number number) {
		NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
		nf.setMaximumFractionDigits(5);
		nf.setGroupingUsed(false);
		return nf.format(number);
	}

	/**
	 * If the given time string has 0 seconds it is returned as "hh:mm". If not
	 * it is returned as "hh:mm:ss".
	 *
	 * @param time
	 *            the time
	 * @return e.g. 12:00 for high noon
	 */
	public static String toShortString(final Time time) {
		String s = time.toString();
		if (s.endsWith(":00")) {
			return s.substring(0, s.length() - 3);
		}
		return s;
	}

	/**
	 * Converts an object into a long representation as the normal toString
	 * method. E.g. maps are splitted into several lines.
	 *
	 * @param obj
	 *            the obj
	 * @return the string
	 * @since 1.4
	 */
	public static String toLongString(final Object obj) {
		if (obj instanceof Map<?, ?>) {
			return toLongString((Map<?, ?>) obj);
		} else if (obj instanceof Object[]) {
			return toLongString((Object[]) obj);
		}
		return toString(obj);
	}

	/**
	 * An empty array or null is mapped to "[]" (empty array).
	 *
	 * @param array e.g. an int array {1, 2, 3}
	 * @return e.g. "[ 1, 2, 3 ]"
	 */
	public static String toString(final Object[] array) {
		return toString("[ ", " ]", array);
	}

	/**
	 * Maps an array to its string representation.
	 *
	 * @param prefix the prefix, e.g. "[ "
	 * @param postfix the postfix, e.g. " ]"
	 * @param array e.g. an int array {1, 2, 3}
	 * @return e.g. "[ 1, 2, 3 ]"
	 */
	public static String toString(String prefix, String postfix, Object[] array) {
		if (ArrayUtils.isEmpty(array)) {
			return prefix.trim() + postfix.trim();
		}
		StringBuilder sbuf = new StringBuilder(prefix);
		sbuf.append(toString(array[0]));
		for (int i = 1; i < array.length; i++) {
			sbuf.append(", ");
			sbuf.append(toString(array[i]));
		}
		sbuf.append(postfix);
		return sbuf.toString();
	}

	/**
	 * To short string.
	 *
	 * @param array
	 *            the array
	 *
	 * @return the string
	 */
	public static String toShortString(final Object[] array) {
		if (ArrayUtils.isEmpty(array)) {
			return "";
		}
		StringBuilder sbuf = new StringBuilder(toShortString(array[0]));
		for (int i = 1; i < array.length; i++) {
			if (i > 2) {
				sbuf.append(", ...");
				break;
			}
			sbuf.append(", ");
			sbuf.append(toShortString(array[i]));
		}
		return sbuf.toString();
	}

	/**
	 * If you want to print the system properties as key-value pairs (e.g.
	 * "java.version=1.6.0_17...") you can use this method here.
	 *
	 * @param pairs
	 *            e.g. the system properties
	 * @return "key=value" lines (separated by newlines)
	 * @since 1.4
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static String toLongString(final Map<?, ?> pairs) {
		StringBuilder buf = new StringBuilder();
		Set<?> keys = new TreeSet(pairs.keySet());
		for (Iterator<?> iterator = keys.iterator(); iterator.hasNext();) {
			Object key = iterator.next();
			buf.append(key);
			buf.append('=');
			buf.append(pairs.get(key));
			buf.append('\n');
		}
		return buf.toString();
	}

	/**
	 * If you want to print each element of an array into a single line you can
	 * use this method here.
	 *
	 * @param array
	 *            the array
	 * @return the string
	 * @since 1.4.2
	 */
	public static String toLongString(final Object[] array) {
		if (array instanceof StackTraceElement[]) {
			return toLongString((StackTraceElement[]) array);
		}
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < array.length; i++) {
			buf.append('[');
			buf.append(i);
			buf.append("]\t");
			buf.append(toString(array[i]));
			buf.append('\n');
		}
		return buf.toString();
	}

	/**
	 * This implementation prints the stacktrace in a similar way as the
	 * printStacktrace method of {@link Throwable} does it.
	 *
	 * @param stacktrace
	 *            the array
	 * @return the string
	 * @since 1.4.2
	 */
	public static String toLongString(final StackTraceElement[] stacktrace) {
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < stacktrace.length; i++) {
			buf.append("\tat ");
			buf.append(stacktrace[i]);
			buf.append('\n');
		}
		return buf.toString();
	}

	/**
	 * Each object inside the array is converted into its toString()
	 * representation.
	 * <p>
	 * Since 1.5 a null object is converted into an empty string ("").
	 * </p>
	 *
	 * @param array
	 *            e.g. an int array {1, 2, 3}
	 * @return e.g. {"1", "2", "3"}
	 * @since 27-Jul-2009
	 */
	@SuppressWarnings("deprecation")
	public static String[] toStringArray(final Object[] array) {
		String[] strings = new String[array.length];
		for (int i = 0; i < strings.length; i++) {
			strings[i] = ObjectUtils.toString(array[i]);
		}
		return strings;
	}

	/**
	 * Each object inside the Set is converted into its toString()
	 * representation.
	 *
	 * @param set
	 *            the set
	 *
	 * @return the given Set as array
	 *
	 * @since 27-Jul-2009
	 */
	public static String[] toStringArray(final Set<? extends Object> set) {
		Object[] objects = new Object[set.size()];
		objects = set.toArray(objects);
		return toStringArray(objects);
	}

	/**
	 * Converts a date to string using the default locale.
	 *
	 * @param date
	 *            a valid date
	 * @param pattern
	 *            e.g. "dd-MMM-yyyy"
	 * @return e.g. "26-Nov-2009"
	 */
	public static String toString(final Date date, final String pattern) {
		DateFormat df = new SimpleDateFormat(pattern);
		return df.format(date);
	}

	/**
	 * Converts a date to string using the default locale.
	 *
	 * @param date
	 *            a valid date
	 * @param pattern
	 *            e.g. "dd-MMM-yyyy"
	 * @param locale
	 *            e.g. new Locale("de")
	 * @return e.g. "30-Mai-2010" (with German locale)
	 */
	public static String toString(final Date date, final String pattern, final Locale locale) {
		DateFormat df = new SimpleDateFormat(pattern, locale);
		return df.format(date);
	}

	/**
	 * Converts variable number of arguments into an object array.
	 *
	 * @param objects
	 *            the objects
	 * @return the object[]
	 */
	public static Object[] toArray(final Object... objects) {
		return objects;
	}

	/**
	 * Converts a string to a date by trying different date patterns. If the
	 * string can't be converted an IllegalArgumentException will be thrown.
	 *
	 * @param s
	 *            e.g. "28-Nov-2009" or "Thu Nov 26 14:30:25 CET 2009"
	 * @return a valid date or NULL_DATE (if an empty string is given)
	 */
	public static Date toDate(final String s) {
		try {
			return toDate(s, false);
		} catch (IllegalArgumentException iae) {
			LOG.trace("Cannot convert '" + s + "' to Date with lenient=false:", iae);
			return toDate(s, true);
		}
	}

	/**
	 * Converts a string to a date by trying different date patterns. To speed
	 * up this method we look first if the string is long enough to hold a date
	 * <em>and</em> time value. If not only the datePatterns are probed. This
	 * speeds up this method about a factor of 5 (from about 10 ms to 2 ms on a
	 * MacBook Pro with 2 GHz).
	 *
	 * @param s
	 *            e.g. "28-Nov-2009"
	 * @param lenient
	 *            the lenient
	 * @return the date
	 */
	private static Date toDate(final String s, final boolean lenient) {
		if (StringUtils.isEmpty(s)) {
			return new Date(0L);
		}
		String[] patterns = dateTimePatterns;
		if (s.length() < 12) {
			patterns = datePatterns;
		}
		for (int i = 0; i < patterns.length; i++) {
			try {
				return toDate(s, patterns[i], lenient);
			} catch (IllegalArgumentException iae) {
				if (LOG.isTraceEnabled()) {
					LOG.trace("Cannot convert '" + s + "' to Date with pattern " + patterns[i] + ":", iae);
				}
			}
		}
		throw new IllegalArgumentException("can't convert \"" + s + "\" to date");
	}

	/**
	 * Here we combine date and time patterns. If it takes too long, cache the
	 * result!
	 * <p>
	 * The default time pattern which is used by the {@link Date#toString()}
	 * method is "EEE MMM d HH:mm:ss zzz yyyy". This pattern is added ad first
	 * pattern.
	 * </p>
	 *
	 * @since 1.1
	 * @return combined date and time patterns
	 */
	private static String[] getDateTimePatterns() {
		String[] timePatterns = { "H:m:s", "H:m", "h:m", "K:m", "k:m" };
		List<String> patterns = new ArrayList<>(datePatterns.length * (timePatterns.length + 1) + 1);
		patterns.add("EEE MMM d HH:mm:ss zzz yyyy");
		for (int i = 0; i < datePatterns.length; i++) {
			for (int j = 0; j < timePatterns.length; j++) {
				patterns.add(datePatterns[i] + " " + timePatterns[j]);
			}
		}
		patterns.addAll(Arrays.asList(datePatterns));
		return patterns.toArray(new String[patterns.size()]);
	}

	/**
	 * Converts a string to a date with the help of the given pattern. If the
	 * string can't be converted an IllegalArgumentException will be thrown.
	 *
	 * @param s
	 *            e.g. "28-Nov-2009"
	 * @param pattern
	 *            e.g. "dd-MMM-yyyy"
	 * @return a valid date or NULL_DATE (if an empty string is given)
	 */
	public static Date toDate(final String s, final String pattern) {
		return toDate(s, pattern, true);
	}

	private static Date toDate(final String s, final String pattern, final boolean lenient) {
		DateFormat df = new SimpleDateFormat(pattern);
		df.setLenient(lenient);
		try {
			return df.parse(s);
		} catch (ParseException e1) {
			if (LOG.isTraceEnabled()) {
				LOG.trace("trying to match " + s + " with locale \"en\"...");
			}
			df = new SimpleDateFormat(pattern, new Locale("en"));
			df.setLenient(lenient);
			try {
				return df.parse(s);
			} catch (ParseException e2) {
				throw new IllegalArgumentException("\"" + s + "\" does not match pattern " + pattern, e2);
			}
		}
	}

	/**
	 * Converts a time string to a {@link Time} value. In contradiction to
	 * {@link Time#valueOf(String)} it works also if the time string is given in
	 * the format "hh:mm".
	 *
	 * @param s
	 *            e.g. "12:00" for high noon
	 * @return the time
	 */
	public static Time toTime(final String s) {
		String withSeconds = (StringUtils.countMatches(s, ":") == 2) ? s : s + ":00";
		return Time.valueOf(withSeconds);
	}

	/**
	 * Converts an Enumeration into a SortedSet.
	 *
	 * @param enumeration
	 *            the Enumeration
	 * @return the SortedSet
	 * @since 1.0
	 */
	public static SortedSet<?> toSortedSet(final Enumeration<?> enumeration) {
		SortedSet<Object> set = new TreeSet<>();
		while (enumeration.hasMoreElements()) {
			Object element = enumeration.nextElement();
			set.add(element);
		}
		return set;
	}

	/**
	 * Serializes the given object and returns it as byte array.
	 *
	 * @param object
	 *            the object to be serialized
	 * @return the object as byte array
	 * @throws NotSerializableException
	 *             the not serializable exception
	 */
	public static byte[] serialize(final Serializable object) throws NotSerializableException {
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		try {
			ObjectOutputStream oostream = new ObjectOutputStream(byteStream);
			oostream.writeObject(object);
			oostream.close();
			byteStream.close();
		} catch (NotSerializableException nse) {
			throw nse;
		} catch (IOException canthappen) {
			throw new IllegalArgumentException(object + " can't be serialized", canthappen);
		} catch (RuntimeException ex) {
			throw new IllegalArgumentException(object.getClass() + " can't be serialized", ex);
		}
		return byteStream.toByteArray();
	}

	/**
	 * Deserializes the given byte array and returns it as object.
	 *
	 * @param bytes
	 *            the byte array
	 * @return the deserialized object
	 * @throws ClassNotFoundException
	 *             if byte array can't be deserialized
	 */
	public static Serializable deserialize(final byte[] bytes) throws ClassNotFoundException {
		ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
		try {
			ObjectInputStream oistream = new ObjectInputStream(byteStream);
			Object object = oistream.readObject();
			IOUtils.closeQuietly(oistream);
			return (Serializable) object;
		} catch (IOException canthappen) {
			throw new IllegalArgumentException(toShortString(bytes) + " can't be deserialized", canthappen);
		}
	}

}
