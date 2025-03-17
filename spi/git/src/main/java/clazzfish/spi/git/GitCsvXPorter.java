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
 * (c)reated 15.03.25 by oboehm
 */
package clazzfish.spi.git;

import clazzfish.monitor.spi.CsvXPorter;

import java.io.IOException;
import java.net.URI;
import java.util.List;

/**
 * Die Klasse GitCsvXPorter ...
 *
 * @author oboehm
 * @since 2.6 (15.03.25)
 */
public class GitCsvXPorter implements CsvXPorter {

    @Override
    public void exportCSV(URI uri, String csvHeadLine, List<String> csvLines) throws IOException {
        throw new UnsupportedOperationException("exportCSV not yet implemented");
    }

}
