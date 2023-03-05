/*
 * Copyright (c) 2023 by Oli B.
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
 * (c)reated 15.02.23 by oboehm
 */
package clazzfish.monitor.internal;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Die Klasse BoringClassLoader is used for boring into some internals of
 * a {@link ClassLoader}.
 *
 * @author oboehm
 * @since 2.0 (15.02.23)
 */
public class BoringClassLoader extends ClassLoader {

    private static final Logger log = LoggerFactory.getLogger(BoringClassLoader.class);
    private final ClassLoader parent;
    private final Map<String, Class<?>> loadedClasses = new HashMap<>();

    public BoringClassLoader() {
        this(Thread.currentThread().getContextClassLoader());
    }

    public BoringClassLoader(ClassLoader parent) {
        super(parent);
        this.parent = parent;
    }

    public Set<Class<?>> getLoadedClasses() {
        Set<Class<?>> loadedClassSet = new HashSet<>();
        Package[] packages = parent.getDefinedPackages();
        String[] packageNames = Arrays.stream(packages).map(Package::getName).toArray(String[]::new);
        try (ScanResult scanResult = new ClassGraph()
                .enableClassInfo()
                .acceptPackages(packageNames)
                .verbose(log.isTraceEnabled())
                .scan()) {
            ClassInfoList list = scanResult.getAllClasses();
            for (ClassInfo info : list) {
                String classname = info.getName();
                try {
                    Class<?> loaded = super.findLoadedClass(classname);
                    if (loaded == null) {
                        Class<?> clazz = Class.forName(classname);
                        loadedClassSet.add(clazz);
                    } else {
                        loadedClassSet.add(loaded);
                    }
                } catch (ClassNotFoundException | NoClassDefFoundError | UnsatisfiedLinkError ex) {
                    log.debug("'{}' is not found as class ({}).", classname, ex.getMessage());
                    log.trace("Details:", ex);
                }
            }
        }
        return loadedClassSet;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        Class<?> found = super.findClass(name);
        if (found != null) {
            loadedClasses.put(name, found);
            return found;
        }
        return loadedClasses.get(name);
    }

//    private static Map<String, Class<?>> scanStacktrace(StackTraceElement[] stacktrace) {
//        Map<String, Class<?>> stacktraceClasses = new HashMap<>();
//        for (StackTraceElement element : stacktrace) {
//            String classname = element.getClassName();
//            try {
//                stacktraceClasses.put(classname, Class.forName(classname));
//            } catch (ClassNotFoundException ex) {
//                log.debug("Class '{}' not found:", classname, ex);
//            }
//        }
//        return stacktraceClasses;
//    }

}
