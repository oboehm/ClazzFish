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
 * (c)reated 25.02.25 by oboehm
 */
package clazzfish.monitor.spi;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * The XPorter class helps you to find the correct provider for a
 * {@link CsvXPorter} class.
 *
 * @author oboehm
 * @since 2.5 (25.02.25)
 */
public class XPorter {

    public static List<CsvXPorterProvider> getProviders() {
        ServiceLoader<CsvXPorterProvider> loader = ServiceLoader.load(CsvXPorterProvider.class);
        List<CsvXPorterProvider> providers = new ArrayList<>();
        loader.forEach(providers::add);
        return providers;
    }

    public static CsvXPorter createCsvXPorter(String protocol) {
        for (CsvXPorterProvider provider : getProviders()) {
            if (provider.supports(protocol)) {
                return provider.create();
            }
        }
        throw new IllegalArgumentException("Unsupported protocol: " + protocol);
    }

}
