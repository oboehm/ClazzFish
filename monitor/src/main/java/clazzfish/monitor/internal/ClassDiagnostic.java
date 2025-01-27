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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.JMException;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * The ClassDiagnostic is a little wrapper arounc Sun's MBean for
 * DiagnosticCommand. This MBean allows you to contact the GC to ask
 * for the loaded classes.
 *
 * @author oboehm
 * @since 2.3 (15.12.24)
 */
public final class ClassDiagnostic {

    private static final Logger log = LoggerFactory.getLogger(ClassDiagnostic.class);

    public static List<Class<?>> getLoadedClassesFromGC() {
        String mbeanName = "com.sun.management:type=DiagnosticCommand";
        try {
            Object classHistogram = ManagementFactory.getPlatformMBeanServer().invoke(
                    new ObjectName(mbeanName),
                    "gcClassHistogram",
                    new Object[]{new String[]{"-all"}},
                    new String[]{"[Ljava.lang.String;"});
            return parseClassHistogramm(classHistogram.toString());
        } catch (JMException ex) {
            log.warn("Cannot call 'getLoadedClasses(..)' from MBean \"{}\"", mbeanName, ex);
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

    private static boolean isNotRealClass(String className) {
        return className.contains("$$Lambda$") || className.startsWith("jdk.internal.reflect.Generated");
    }

}
