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
 * (c)reated 19.02.25 by oboehm
 */
package clazzfish.monitor.spi;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests {@link CsvXPorterProvider}.
 *
 * @author oboehm
 * @since 19.02.25
 */
class CsvXPorterProviderTest {

    @Test
    void testServiceProvider() {
        ServiceLoader<CsvXPorterProvider> loader = ServiceLoader.load(CsvXPorterProvider.class);
        assertNotNull(loader);
        List<CsvXPorterProvider> services = new ArrayList<>();
        loader.forEach(services::add);
        assertFalse(services.isEmpty());
    }

}
