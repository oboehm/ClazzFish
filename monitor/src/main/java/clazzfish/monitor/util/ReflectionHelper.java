/*
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
 * (c)reated 09.03.2009 by oliver (ob@aosd.de)
 */
package clazzfish.monitor.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * This class is a helper class to access some fields via reflection. Normally
 * you should avoid reflection. Handle with care if you use it.
 * <p>
 * Originally this class was part of the PatternTesting project.
 * </p>
 *
 * @author <a href="boehm@javatux.de">oliver</a>
 */
public class ReflectionHelper {

	private static final Logger LOG = LoggerFactory.getLogger(ReflectionHelper.class);

	/** Utility class - no need to instantiate it */
	private ReflectionHelper() {
	}

	/**
	 * Tries to get the wanted field.
	 *
	 * @param cl the class
	 * @param name the field name
	 * @return the wanted field
	 * @throws NoSuchFieldException if the field was not found
	 */
	public static Field getField(final Class<?> cl, final String name) throws NoSuchFieldException {
		try {
			Field field = cl.getDeclaredField(name);
			field.setAccessible(true);
			return field;
		} catch (NoSuchFieldException e) {
			Class<?> superclass = cl.getSuperclass();
			if (superclass == null) {
				throw e;
			} else {
				if (LOG.isTraceEnabled()) {
					LOG.trace("using " + superclass + " to get " + cl.getName() + "." + name + "...");
				}
				return ReflectionHelper.getField(superclass, name);
			}
		}
	}

	/**
	 * Returns true if the given object have a field with the given name.
	 *
	 * @param obj
	 *            the obj
	 * @param name
	 *            the name
	 * @return true, if successful
	 * @since 1.5
	 */
	public static boolean hasField(final Object obj, final String name) {
		return hasField(obj.getClass(), name);
	}

	/**
	 * Returns true if the given class have a field with the given name.
	 *
	 * @param clazz
	 *            the clazz
	 * @param name
	 *            the name
	 * @return true, if successful
	 * @since 1.5
	 */
	public static boolean hasField(final Class<?> clazz, final String name) {
		if (StringUtils.isEmpty(name)) {
			return false;
		}
		try {
			getField(clazz, name);
			return true;
		} catch (NoSuchFieldException ex) {
			LOG.debug("Will return false because {} has no field '{}' ({}).", clazz, name, ex);
			return false;
		}
	}

	/**
	 * If it can it returns the value of the given field. If not it throws an
	 * exception.
	 *
	 * @param target
	 *            the target object
	 * @param name
	 *            the name
	 * @return the field value
	 * @throws ReflectiveOperationException
	 *             the reflective operation exception
	 */
	public static Object getFieldValue(final Object target, final String name) throws ReflectiveOperationException {
		Field field = ReflectionHelper.getField(target.getClass(), name);
		return field.get(target);
	}

	/**
	 * Sets the field value of the given target. With this method you can set
	 * private attributes, e.g. for testing.
	 *
	 * @param target
	 *            the target
	 * @param name
	 *            the name
	 * @param value
	 *            the value
	 * @throws ReflectiveOperationException
	 *             the reflective operation exception
	 * @since 1.6
	 */
	public static void setFieldValue(final Object target, final String name, final String value)
			throws ReflectiveOperationException {
		Field field = ReflectionHelper.getField(target.getClass(), name);
		field.set(target, value);
	}

	/**
	 * To get all uninitialized field you can call this method.
	 *
	 * @param obj
	 *            the object where to get the fields from
	 *
	 * @return a collecion of unitialized fields (can be empty)
	 */
	public static Collection<Field> getUninitializedNonStaticFields(final Object obj) {
		Collection<Field> unitializedFields = new ArrayList<>();
		Field[] fields = obj.getClass().getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			try {
				fields[i].setAccessible(true);
				if ((fields[i].get(obj) == null) && !isStatic(fields[i])) {
					unitializedFields.add(fields[i]);
				}
			} catch (IllegalArgumentException e) {
				LOG.info(e + " => " + fields[i] + " ignored");
			} catch (IllegalAccessException e) {
				LOG.debug("Cannot access " + fields[i] + " => ignored: ", e);
			}
		}
		return unitializedFields;
	}

	/**
	 * Checks if the given field is static.
	 *
	 * @param field
	 *            the field
	 * @return true, if is static
	 */
	public static boolean isStatic(final Field field) {
		int m = field.getModifiers();
		return Modifier.isStatic(m);
	}

	/**
	 * To short string.
	 *
	 * @param field
	 *            the field
	 *
	 * @return the string
	 */
	public static String toShortString(final Field field) {
		return field.getType().getSimpleName() + " " + field.getName();
	}

	/**
	 * To short string.
	 *
	 * @param fields
	 *            the fields
	 *
	 * @return the string
	 */
	public static String toShortString(final Collection<Field> fields) {
		StringBuilder sbuf = new StringBuilder();
		for (Iterator<Field> iterator = fields.iterator(); iterator.hasNext();) {
			Field field = iterator.next();
			sbuf.append(", ");
			sbuf.append(toShortString(field));
		}
		return sbuf.substring(2);
	}

	/**
	 * Gets the method.
	 *
	 * @param cl
	 *            the cl
	 * @param name
	 *            the name
	 * @param args
	 *            the args
	 * @return the method
	 * @throws NoSuchMethodException
	 *             the no such method exception
	 */
	public static Method getMethod(final Class<?> cl, final String name, final Object... args)
			throws NoSuchMethodException {
		Class<?> parameterTypes[] = new Class<?>[args.length];
		for (int i = 0; i < args.length; i++) {
			parameterTypes[i] = args[i].getClass();
		}
		return getMethod(cl, name, parameterTypes);
	}

	/**
	 * Gets the method.
	 *
	 * @param cl
	 *            e.g. class java.lang.ClassLoader
	 * @param name
	 *            e.g. "getPackages"
	 * @param parameterTypes
	 *            the parameter types
	 * @return a Method object
	 * @throws NoSuchMethodException
	 *             the no such method exception
	 */
	public static Method getMethod(final Class<?> cl, final String name, final Class<?>... parameterTypes)
			throws NoSuchMethodException {
		try {
			return cl.getDeclaredMethod(name, parameterTypes);
		} catch (NoSuchMethodException origException) {
			try {
				return getMethodFromSuperclass(cl, name, origException, parameterTypes);
			} catch (NoSuchMethodException derived) {
				LOG.debug("Method '{}' also not found in superclass:", name, derived);
				throw origException;
			}
		}
	}

	private static Method getMethodFromSuperclass(final Class<?> cl, final String name,
			NoSuchMethodException origException, final Class<?>... parameterTypes) throws NoSuchMethodException {
		try {
			return findMethod(cl, name, parameterTypes);
		} catch (NoSuchMethodException nsme) {
			Class<?> superclass = cl.getSuperclass();
			if (superclass == null) {
				LOG.debug("No method '{}' and no superclass for {} found:", name, cl, nsme);
				throw origException;
			} else {
				return getMethod(superclass, name, parameterTypes);
			}
		}
	}

	private static Method findMethod(final Class<?> cl, final String name, final Class<?>... parameterTypes)
			throws NoSuchMethodException {
		Method[] methods = cl.getDeclaredMethods();
		for (int i = 0; i < methods.length; i++) {
			if (name.equals(methods[i].getName()) && matchesParameters(methods[i], parameterTypes)) {
				return methods[i];
			}
		}
		throw new NoSuchMethodException(
				cl.getName() + "." + name + "(" + Converter.toShortString(parameterTypes) + ")");
	}

	private static boolean matchesParameters(final Method method, final Class<?>[] matchingTypes) {
		Class<?>[] parameterTypes = method.getParameterTypes();
		if (parameterTypes.length != matchingTypes.length) {
			return false;
		}
		for (int i = 0; i < parameterTypes.length; i++) {
			if (!matches(parameterTypes[i], matchingTypes[i])) {
				return false;
			}
		}
		return true;
	}

	private static boolean matches(final Class<?> parameterType, final Class<?> matchingType) {
		if (parameterType.isAssignableFrom(matchingType)) {
			return true;
		}
		if (parameterType.isPrimitive()) {
			Map<Class<?>, Class<?>> mapping = new HashMap<>();
			mapping.put(boolean.class, Boolean.class);
			mapping.put(byte.class, Byte.class);
			mapping.put(char.class, Character.class);
			mapping.put(short.class, Short.class);
			mapping.put(int.class, Integer.class);
			mapping.put(long.class, Long.class);
			mapping.put(float.class, Float.class);
			mapping.put(double.class, Double.class);
			mapping.put(boolean.class, Boolean.class);
			Class<?> mappedType = mapping.get(parameterType);
			if (mappedType == null) {
				LOG.warn("unknown primitive type \"" + parameterType + "\" not yet supported - sorry!");
				return false;
			}
			return matchingType.equals(mappedType);
		}
		return false;
	}

	/**
	 * Invoke method. This could be also a protected or private method.
	 *
	 * @param target
	 *            the target
	 * @param name
	 *            the method name
	 * @param args
	 *            the args for the method
	 * @return the result of the method.
	 */
	public static Object invokeMethod(final Object target, final String name, final Object... args) {
		try {
			Method method = getMethod(target.getClass(), name, args);
			method.setAccessible(true);
			return method.invoke(target, args);
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException(
					"no method \"" + name + "(" + toParamString(args) + ")\" in " + target.getClass(), e);
		} catch (IllegalAccessException iae) {
			throw new IllegalStateException("can't access method \"" + name + "\" in " + target.getClass(), iae);
		} catch (InvocationTargetException ite) {
			throw new IllegalStateException("exception in method \"" + name + "\" of " + target.getClass(),
					ite.getTargetException());
		}
	}

	private static String toParamString(final Object[] args) {
		Class<?> paramTypes[] = new Class<?>[args.length];
		for (int i = 0; i < args.length; i++) {
			paramTypes[i] = args[i].getClass();
		}
		return Converter.toShortString(paramTypes);
	}

	/**
	 * Checks if the given object has something which looks like an id.
	 *
	 * @param obj
	 *            the obj
	 * @return true, if successful
	 * @since 1.6 (01.06.2015)
	 */
	public static boolean hasId(final Object obj) {
		return hasId(obj.getClass());
	}

	/**
	 * Checks if the given class has something which looks like an id attribute.
	 *
	 * @param clazz
	 *            the clazz
	 * @return true, if successful
	 */
	public static boolean hasId(final Class<?> clazz) {
		if (getIdField(clazz) != null) {
			return true;
		}
		return getIdGetter(clazz) != null;
	}

	/**
	 * Gets the id of the given object.
	 *
	 * @param obj
	 *            the obj
	 * @return the id
	 * @since 1.6 (01.06.2015)
	 */
	public static Object getId(final Object obj) {
		Field idField = getIdField(obj.getClass());
		try {
			if (idField != null) {
				idField.setAccessible(true);
				return idField.get(obj);
			}
			Method getter = getIdGetter(obj.getClass());
			if (getter == null) {
			    throw new IllegalArgumentException("no getter for '" + obj + "' available");
			}
			getter.setAccessible(true);
			return getter.invoke(obj);
		} catch (ReflectiveOperationException ex) {
			throw new IllegalArgumentException("cannot get id of " + obj, ex);
		}
	}

	private static Field getIdField(final Class<?> clazz) {
		Field[] fields = clazz.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			if ("id".equalsIgnoreCase(fields[i].getName())) {
				return fields[i];
			}
		}
		Class<?> superclass = clazz.getSuperclass();
		return superclass == null ? null : getIdField(superclass);
	}

	private static Method getIdGetter(final Class<?> clazz) {
		Method[] methods = clazz.getDeclaredMethods();
		for (int i = 0; i < methods.length; i++) {
			if ("getid".equalsIgnoreCase(methods[i].getName())) {
				return methods[i];
			}
		}
		Class<?> superclass = clazz.getSuperclass();
		return superclass == null ? null : getIdGetter(superclass);
	}

}
