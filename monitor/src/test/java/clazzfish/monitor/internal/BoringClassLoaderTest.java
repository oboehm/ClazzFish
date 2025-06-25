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
 * (c)reated 20.02.23 by oboehm
 */
package clazzfish.monitor.internal;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Unit-Test fuer {@link BoringClassLoader} ...
 *
 * @author oboehm
 * @since 20.02.23
 */
class BoringClassLoaderTest {

    private final BoringClassLoader classLoader = BoringClassLoader.DEFAULT_CLOADER;

    @Test
    void getLoadedClasses() {
        Collection<Class<?>> loadedClasses = classLoader.getLoadedClasses();
        assertFalse(loadedClasses.isEmpty());
        assertThat(loadedClasses, hasItem(getClass()));
        for (Class<?> cl : loadedClasses) {
            if ("clazzfish.monitor.internal.DeadClass".equals(cl.getName())) {
                fail(cl + " is a dead class and never loaded");
            }
        }
    }

    @Test
    void getLoadedClassesTwice() {
        Collection<Class<?>> l1 = classLoader.getLoadedClasses();
        Collection<Class<?>> l2 = classLoader.getLoadedClasses();
        assertThat(l2, hasItems(l1.toArray(new Class<?>[0])));
    }

    @Test
    void getAllPackageNames() {
        Set<String> packageNames = classLoader.getAllPackageNames();
        assertThat(packageNames, not(hasItem("")));
        Package[] packages = Package.getPackages();
        for (Package pkg : packages) {
            assertThat(packageNames, hasItem(pkg.getName()));
        }
    }

}