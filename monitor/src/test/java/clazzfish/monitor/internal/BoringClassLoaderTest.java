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

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Unit-Test fuer {@link BoringClassLoader} ...
 *
 * @author oboehm
 * @since 20.02.23
 */
class BoringClassLoaderTest {

    @Test
    void getLoadedClasses() {
        BoringClassLoader cl = new BoringClassLoader();
        Collection<Class<?>> loadedClasses = cl.getLoadedClasses();
        assertFalse(loadedClasses.isEmpty());
    }

}