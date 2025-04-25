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

import java.net.URI;
import java.util.Objects;

/**
 * The class GitCsvXPorterProvider register itself as service for SPI.
 *
 * @author oboehm
 * @since 2.6 (17.03.25)
 */
public class GitCsvXPorterProvider implements CsvXPorterProvider {

    @Override
    public CsvXPorter create() {
        return new GitCsvXPorter();
    }

    @Override
    public boolean supports(String protocol) {
        return protocol.equalsIgnoreCase("git");
    }

    @Override
    public boolean supports(URI uri) {
        String authority = Objects.toString(uri.getAuthority());
        if (authority.startsWith("git@")) {
            return true;
        } else {
            return supports(uri.getScheme());
        }
    }

}
