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
 * (c)reated 17.03.25 by oboehm
 */
package clazzfish.spi.git;

import clazzfish.monitor.spi.CsvXPorter;
import clazzfish.monitor.spi.CsvXPorterProvider;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link GitCsvXPorterProvider}.
 *
 * @author oboehm
 * @since 17.03.25
 */
class GitCsvXPorterProviderTest {

    private final CsvXPorterProvider provider = new GitCsvXPorterProvider();

    @Test
    void testServiceProvider() {
        ServiceLoader<CsvXPorterProvider> loader = ServiceLoader.load(CsvXPorterProvider.class);
        assertNotNull(loader);
        List<Class<?>> serviceClasses = new ArrayList<>();
        loader.forEach(provider -> serviceClasses.add(provider.getClass()));
        assertThat(serviceClasses, hasItem(GitCsvXPorterProvider.class));
    }

    @Test
    void supports() {
        assertTrue(provider.supports(URI.create("ssh://git@github.com/oboehm/ClazzFish.git")));
    }

    @Test
    void create() {
        CsvXPorter csvXPorter = provider.create();
        assertNotNull(csvXPorter);
    }

}