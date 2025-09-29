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
 * (c)reated 26.09.25 by oboehm
 */
package clazzfish.core;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Unit tests for {@link ResourceWalker}.
 *
 * @author oboehm
 * @since 26.09.25
 */
class ResourceWalkerTest {

    private static final File CLASSES_DIR = new File("target", "test-classes");

    @Test
    void getAllResources() throws IOException {
        ResourceWalker walker = new ResourceWalker(CLASSES_DIR);
        Collection<String> resources = getResourcesFrom(walker);
        assertThat(resources, hasItem("/logging.properties"));
    }

    @Test
    void getPropertiesResources() throws IOException {
        ResourceWalker walker = new ResourceWalker(CLASSES_DIR, ".properties");
        Collection<String> resources = getResourcesFrom(walker);
        assertThat(resources, hasItem("/logging.properties"));
    }

    private static Collection<String> getResourcesFrom(ResourceWalker walker) throws IOException {
        Collection<String> resources = walker.getResources();
        assertThat(resources, not(empty()));
        for (String rsc : resources) {
            assertThat(rsc, not(endsWith(".class")));
        }
        return resources;
    }

    @Test
    void getClasses() throws IOException {
        ResourceWalker classWalker = new ResourceWalker(CLASSES_DIR);
        Collection<String> classes = classWalker.getClasses();
        assertFalse(classes.isEmpty(), "no classes found");
        String firstClass = classes.iterator().next();
        assertFalse(firstClass.startsWith("."), firstClass + " is not a classname");
    }

}