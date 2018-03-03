/*
 * Copyright (c) 2018 by Oliver Boehm
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * (c)reated 01.03.2018 by oboehm (ob@oasd.de)
 */
package clazzfish.monitor.internal;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Unit tests for {@link ResourceWalker} class.
 */
public class ResourceWalkerTest {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceWalker.class);
    private static final File CLASSES_DIR = new File("target", "test-classes");

    /**
     * Test method for {@link ResourceWalker#getResources()}
     *
     * @throws IOException in case of trouble
     */
    @Test
    public void testGetResources() throws IOException {
        ResourceWalker walker = new ResourceWalker(CLASSES_DIR, ".xml");
        Collection<String> resources = getResourcesFrom(walker);
        assertThat(resources, hasItem("/log4j2.xml"));
    }
    
    private static Collection<String> getResourcesFrom(ResourceWalker walker) throws IOException {
        Collection<String> resources = walker.getResources();
        assertThat(resources, not(empty()));
        LOG.info("{} resources found: {}", resources.size(), resources);
        for (String rsc : resources) {
            assertThat(rsc, not(endsWith(".class")));
        }
        return resources;
    }

}
