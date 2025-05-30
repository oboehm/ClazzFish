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

import java.net.URI;

/**
 * The interface CsvXPorterProvider describes the provider for SPI.
 *
 * @author oboehm
 * @since 2.5 (19.02.25)
 */
public interface CsvXPorterProvider {

    /**
     * Creates an object which implements the {@link CsvXPorter} interface.
     *
     * @return a CsvXPorter object
     */
    CsvXPorter create();

    /**
     * Indicates if the provider supports a given protocol.
     *
     * @param protocol e.g. "file"
     * @return true or false
     */
    boolean supports(String protocol);

    /**
     * Indicates if the provider supports the given URI.
     *
     * @param uri e.g. "file://hello/world"
     * @return true or false
     * @since 2.6
     */
    default boolean supports(URI uri) {
        return supports(uri.getScheme());
    }

}
