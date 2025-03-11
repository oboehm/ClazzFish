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
 * (c)reated 16.12.24 by oboehm
 */
package clazzfish.monitor.internal;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Unit-Test for {@link ClassDiagnostic}.
 *
 * @author oboehm
 * @since 16.12.24
 */
class ClassDiagnosticTest {

    private static final Logger log = LoggerFactory.getLogger(ClassDiagnosticTest.class);

    @Test
    void getLoadedClassesFromGC() {
        Collection<Class<?>> loadedClasses = ClassDiagnostic.getLoadedClassesFromGC();
        checkClasses(loadedClasses);
    }

    @Test
    void getLoadedClassesFromVmClassHierarchy() {
        Collection<Class<?>> loadedClasses = ClassDiagnostic.getLoadedClassesFromVmClassHierarchy();
        checkClasses(loadedClasses);
    }

    @Test
    void getLoadedClasses() {
        Collection<Class<?>> loadedClasses = ClassDiagnostic.getLoadedClasses();
        checkClasses(loadedClasses);
    }

    void checkClasses(Collection<Class<?>> loadedClasses) {
        log.info("Checking {} loaded classes...", loadedClasses.size());
        assertContains(loadedClasses, getClass());
        assertContains(loadedClasses, Object.class);
    }

    private void assertContains(Collection<Class<?>> classes, Class<?> expectedClass) {
        for (Class<?> clazz : classes) {
            if (clazz.equals(expectedClass)) {
                return;
            }
        }
        fail("Class '" + expectedClass + "' not in " + classes);
    }

}