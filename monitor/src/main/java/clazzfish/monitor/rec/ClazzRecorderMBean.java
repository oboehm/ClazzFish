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
package clazzfish.monitor.rec;

import clazzfish.monitor.util.Shutdownable;

import java.io.File;
import java.io.IOException;

/**
 * JMX-Interface for the {@link ClazzRecorder}.
 *
 * @author oboehm
 * @since 2.3 (29.11.24)
 */
public interface ClazzRecorderMBean extends Shutdownable {

    /**
     * Exports the statistics of the collected classes to the default file..
     *
     * @return export file
     * @throws IOException in case of I/O problems
     */
    File exportCSV() throws IOException;

    /**
     * Exports the statistics of the collected classes to the file with the
     * given filename.
     *
     * @param filename the filename, where the statistics should be exported to
     * @return export file
     * @throws IOException in case of I/O problems
     */
    File exportCSV(String filename) throws IOException;

    /**
     * Returns the name of the default export file.
     *
     * @return export file
     */
    File getExportFile();

}
