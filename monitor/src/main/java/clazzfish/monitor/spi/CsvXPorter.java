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
 * (c)reated 18.02.25 by oboehm
 */
package clazzfish.monitor.spi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;

/**
 * The interface CsvXPorter contains the needed methods to import and export
 * a CSV file for different locations.
 *
 * @author oboehm
 * @since 2.5 (18.02.25)
 */
public interface CsvXPorter {

    Logger log = LoggerFactory.getLogger(CsvXPorter.class);

    /**
     * Interface for exporting CSV.
     *
     * @param uri         URI wher the CSV should be exported to
     * @param csvHeadLine CSV header
     * @param csvLines    CSV lines
     * @throws IOException in case of I/O problems
     */
    void exportCSV(URI uri, String csvHeadLine, List<String> csvLines) throws IOException;

    /**
     * Interface for importing a CSV. This method should be called in
     * {@link #exportCSV(URI, String, List)} to aggregate the count of the
     * actual export with former exports.
     *
     * @param uri URI where the CSV should be imported from
     * @return list of CSV lines (including head line)
     * @throws IOException in case of I/O problems
     */
    default List<String> importCSV(URI uri) throws IOException {
        log.info("Return empty list because 'importCSV({})' is not supported.", uri);
        return Collections.EMPTY_LIST;
    }

}
