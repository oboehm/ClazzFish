/*
 * Copyright (c) 2024,2025 by Oli B.
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
 * (c)reated 16.12.24 by oboehm
 */
package clazzfish.core.jmx;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Unit-Test for {@link clazzfish.core.jmx.ClassDiagnostic}.
 *
 * @author oboehm
 * @since 16.12.24
 */
class ClassDiagnosticTest {

    private static final Logger log = LoggerFactory.getLogger(ClassDiagnosticTest.class);
    private final clazzfish.core.jmx.ClassDiagnostic classDiagnostic = new ClassDiagnostic();

    @Test
    void getLoadedClassesFromVmClassHierarchy() {
        Collection<Class<?>> loadedClasses = classDiagnostic.getLoadedClassesFromVmClassHierarchy();
        checkClasses(loadedClasses.toArray(new Class<?>[0]));
    }

    @Test
    void getLoadedClasses() {
        Class<?>[] loadedClasses = classDiagnostic.getLoadedClasses();
        checkClasses(loadedClasses);
    }

    void checkClasses(Class<?>[] loadedClasses) {
        log.info("Checking {} loaded classes...", loadedClasses.length);
        assertContains(loadedClasses, getClass());
        assertContains(loadedClasses, Object.class);
    }

    private void assertContains(Class<?>[] classes, Class<?> expectedClass) {
        for (Class<?> clazz : classes) {
            if (clazz.equals(expectedClass)) {
                return;
            }
        }
        fail("Class '" + expectedClass + "' not in " + Arrays.toString(classes));
    }

}