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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Die Klasse BoringClassLoader is used for boring into some internals of
 * a {@link ClassLoader}. It used "io.github.classgraph" for boring and
 * encapsulate the needed API calls.
 * <p>
 * Sincd 2.8 the dependency to "io.github.classgraph" was removed to reduce
 * the dependencies.
 * </p>
 *
 * @author oboehm
 * @since 2.0 (15.02.23)
 */
public class BoringClassLoader extends ClassLoader {

    private static final Logger log = LoggerFactory.getLogger(BoringClassLoader.class);
    private final Map<String, Class<?>> loadedClasses = new HashMap<>();

    public static BoringClassLoader DEFAULT_CLOADER = new BoringClassLoader();

    /**
     * Creates a {@link BoringClassLoader} from the given classLoder.
     * If the given classLoader itself is a {@link BoringClassLoader} it will
     * be returned. In this case no new {@link BoringClassLoader} is created.
     *
     * @param classLoader the parent {@link ClassLoader}
     * @return a new {@link BoringClassLoader} or the classLoader itself
     */
    public static BoringClassLoader of(ClassLoader classLoader) {
        if (classLoader instanceof BoringClassLoader) {
            return (BoringClassLoader) classLoader;
        } else {
            return new BoringClassLoader(classLoader);
        }
    }

    private BoringClassLoader() {
        this(Thread.currentThread().getContextClassLoader());
    }

    private BoringClassLoader(ClassLoader parent) {
        super(parent);
    }

    /**
     * Gets all package names. Also the packages which are not yet loaded.
     *
     * @return set of all package names
     */
    public Set<String> getAllPackageNames() {
        Set<String> packageNames = new HashSet<>();
        for (Package pkg : getPackages()) {
            packageNames.add(pkg.getName());
        }
        packageNames.remove("");
        return packageNames;
    }

    /**
     * Gets the laoded classes which normally is stored in the classes field
     * of the parent classloader. But since Java 11 it is no longer possible
     * to access this field by reflection. So now the loaded packages are
     * scanned for the classes which might be loaded.
     * <p>
     * The actual implementation uses the DiagnosticCommand MBean as described
     * in <a href=
     * "https://stackoverflow.com/questions/75008706/how-can-i-get-the-name-and-package-of-all-the-classes-loaded-in-the-java-jvm">
     * Stackoverflow</a>. Other tries with e.g. with the ClassPath class from
     * Googles Guava were not successful.
     * </p>
     *
     * @return all loaded classes
     */
    public Set<Class<?>> getLoadedClasses() {
        return ClassDiagnostic.getLoadedClasses();
    }

    @Override
    protected Class<?> findClass(String name) {
        Class<?> found = loadedClasses.get(name);
        if (found != null) {
            return found;
        }
        try {
            found = super.findClass(name);
            loadedClasses.put(name, found);
        } catch (ClassNotFoundException ex) {
            log.debug("Class '{}' was not found by superclass.", name);
            log.trace("Details:", ex);
        }
        return found;
    }

}
