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
package clazzfish.core.spi;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The interface CsvXPorter contains the needed methods to import and export
 * a CSV file for different locations.
 *
 * @author oboehm
 * @since 2.5 (18.02.25)
 */
public interface CsvXPorter {

    Logger log = Logger.getLogger(CsvXPorter.class.getName());

    /**
     * Simple getter for the import and export URI
     *
     * @return URI
     * @since 3.0
     */
    URI getURI();

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
     * Interface for exporting CSV.
     *
     * @param csvHeadLine CSV header
     * @param csvLines    CSV lines
     * @throws IOException in case of I/O problems
     * @since 3.0
     */
    default void exportCSV(String csvHeadLine, List<String> csvLines) throws IOException {
        exportCSV(getURI(), csvHeadLine, csvLines);
    }

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
        log.log(Level.INFO, "Return empty list because 'importCSV({0})' is not supported.", uri);
        return Collections.EMPTY_LIST;
    }

    /**
     * Interface for importing a CSV. This method should be called in
     * {@link #exportCSV(URI, String, List)} to aggregate the count of the
     * actual export with former exports.
     *
     * @return list of CSV lines (including head line)
     * @throws IOException in case of I/O problems
     * @since 3.0
     */
    default List<String> importCSV() throws IOException {
        return importCSV(getURI());
    }

}
