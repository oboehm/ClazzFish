/*
 * Copyright (c) 2024 by Oli B.
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
 * (c)reated 29.11.24 by oboehm
 */
package clazzfish.monitor.stat;

import clazzfish.monitor.jmx.Description;
import clazzfish.monitor.util.Shutdownable;

import java.io.IOException;
import java.net.URI;

/**
 * JMX-Interface for the {@link ClazzStatistic}.
 *
 * @author oboehm
 * @since 2.3 (29.11.24)
 */
public interface ClazzStatisticMBean extends Shutdownable {

    /**
     * Prints the statistic to the log output.
     */
    @Description("logs the statistic")
    void logMe();

    /**
     * Exports the statistics of the collected classes to the default location.
     *
     * @return export file
     * @throws IOException in case of I/O problems
     */
    URI exportCSV() throws IOException;

    /**
     * Exports the statistics of the collected classes to the file with the
     * given filename.
     *
     * @param filename filename or URI, where the statistics should be exported to
     * @return export URI
     * @throws IOException in case of I/O problems
     */
    URI exportCSV(String filename) throws IOException;

    /**
     * Importes the statistics form the given filename or URI.
     *
     * @param filename filename or URI
     * @since 2.7
     */
    void importCSV(String filename);

    /**
     * Returns the URI where the statistic should be exported to.
     *
     * @return export URI
     */
    URI getExportURI();

}
