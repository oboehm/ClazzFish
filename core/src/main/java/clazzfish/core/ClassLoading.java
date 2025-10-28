/*
 * Copyright (c) 2025 by Oli B.
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
 * (c)reated 27.10.25 by oboehm
 */
package clazzfish.core;

/**
 * This interface identifies the methods which are necessary to get the
 * loaded classes.
 *
 * @since 3.0 (27-Oct-2025)
 * @author Oli B.
 */
public interface ClassLoading {

    /**
     * Get the classpath.
     *
     * @return classpath
     */
    default String[] getClasspath() {
        return ClasspathInspector.getClasspath();
    }

    /**
     * Returns the classes which were loaded by the classloader.
     * <p>
     * NOTE: Although the 'jconsole' displays the classes as 'unavailable'
     * do not remove it. It is used by the ClasspathAgent class to provide
     * the loaded classes.
     * </p>
     *
     * @return the classes as string array
     */
    Class<?>[] getLoadedClasses();

    /**
     * Returns the classes which were loaded by the classloader.
     * The loaded packages are returned as string array so that it can
     * be displayed by the 'jconsole'.
     *
     * @return the classnames as string array
     */
    String[] getLoadedClassnames();

}
