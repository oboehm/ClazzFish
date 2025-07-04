/*
 * Copyright (c) 2024 by Oli B.
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
 * (c)reated 15.12.24 by oboehm
 */
package clazzfish.monitor.internal;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.JMException;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The ClassDiagnostic is a little wrapper around Sun's MBean for
 * DiagnosticCommand. This MBean allows you to contact the GC to ask
 * for the loaded classes.
 *
 * @author oboehm
 * @since 2.3 (15.12.24)
 */
public final class ClassDiagnostic {

    private static final Logger log = LoggerFactory.getLogger(ClassDiagnostic.class);
    private static final String DIAGNOSTIC_COMMAND = "com.sun.management:type=DiagnosticCommand";

    /**
     * This is a shortcut for the call of the preferred method.
     *
     * @return a set of loaded classes
     */
    public static Set<Class<?>> getLoadedClasses() {
        return getLoadedClassesFromVmClassHierarchy();
    }

    /**
     * This is a shortcut for the call of the preferred method.
     *
     * @return a set of loaded classnames
     */
    public static Set<String> getLoadedClassnames() {
        return getLoadedClassnamesFromVmClassHierarchy();
    }

    /**
     * Ask the GC (garbage collector) via JMX which classes are loaded.
     * <p>
     * NOTE: The GC handles only instantiated classes. I.e. static classes (and
     * propably abstract classes) are not returned.
     * </p>
     *
     * @return a list of instantiated classes
     * @deprecated replaced by {@link #getLoadedClassesFromVmClassHierarchy()}
     */
    @Deprecated(forRemoval = true)
    public static List<Class<?>> getLoadedClassesFromGC() {
        try {
            Object classHistogram = ManagementFactory.getPlatformMBeanServer().invoke(
                    new ObjectName(DIAGNOSTIC_COMMAND),
                    "gcClassHistogram",
                    new Object[]{new String[]{"-all"}},
                    new String[]{"[Ljava.lang.String;"});
            return parseClassHistogramm(classHistogram.toString());
        } catch (JMException ex) {
            log.warn("Cannot call 'gcClassHistogram(..)' from MBean \"{}\"", DIAGNOSTIC_COMMAND, ex);
            return new ArrayList<>();
        }
    }

    private static List<Class<?>> parseClassHistogramm(String histogram) {
        List<Class<?>> classes = new ArrayList<>();
        String[] lines = histogram.split("\n");
        for (int i = 2; i < lines.length-1; i++) {
            String[] parts = lines[i].trim().split("\\s+");
            String className = parts[3];
            if (isNotRealClass(className)) {
                log.trace("'{}' is ignored because it is not a real class name.", classes);
                continue;
            }
            try {
                Class<?> cl = Class.forName(className);
                classes.add(cl);
            } catch (ClassNotFoundException ex) {
                log.debug("Class '{}' could not be loaded ({}).", className, ex.getMessage());
                log.trace("Details:", ex);
            }
        }
        return classes;
    }

    public static Set<Class<?>> getLoadedClassesFromVmClassHierarchy() {
        Set<Class<?>> classes = new HashSet<>();
        Set<String> loadedClassesNames = getLoadedClassnames();
        for (String className : loadedClassesNames) {
            try {
                log.trace("Try to get class '{}'...", className);
                Class<?> cl = Class.forName(className);
                classes.add(cl);
            } catch (ClassNotFoundException | Error ex) {
                log.debug("Class '{}' could not be loaded ({}).", className, ex.getMessage());
                log.trace("Details:", ex);
            }
        }
        return classes;
    }

    public static Set<String> getLoadedClassnamesFromVmClassHierarchy() {
        try {
            Object classHierarchy = ManagementFactory.getPlatformMBeanServer().invoke(
                    new ObjectName(DIAGNOSTIC_COMMAND),
                    "vmClassHierarchy",
                    new Object[]{new String[]{""}},
                    new String[]{"[Ljava.lang.String;"});
            return parseClassnamesHierarchy(classHierarchy.toString());
        } catch (JMException ex) {
            log.warn("Cannot call 'vmClassHierarchy(..)' from MBean \"{}\"", DIAGNOSTIC_COMMAND, ex);
            return new HashSet<>();
        }
    }

    private static Set<String> parseClassnamesHierarchy(String hierarchy) {
        Set<String> classes = new HashSet<>();
        String[] lines = hierarchy.split("\n");
        for(String l : lines) {
            String className = StringUtils.substringBefore(l.trim(), '/').trim();
            if (className.startsWith("|")) {
                className = StringUtils.substringAfterLast(className, "|--");
            }
            if (isNotRealClass(className)) {
                log.trace("'{}' is ignored because it is not a real class name.", classes);
            } else {
                classes.add(className);
            }
        }
        return classes;
    }

    private static boolean isNotRealClass(String className) {
        return className.contains("$$Lambda$")
                || className.startsWith("jdk.internal.reflect.Generated")
                || className.startsWith("java.lang.invoke.LambdaForm$")
                || className.equals("org.apache.logging.log4j.core.net.MulticastDnsAdvertiser")
                ;
    }

}
