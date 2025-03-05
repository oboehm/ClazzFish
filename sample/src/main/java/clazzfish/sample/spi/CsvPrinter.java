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
 * (c)reated 01.03.25 by oboehm
 */
package clazzfish.sample.spi;

import clazzfish.monitor.spi.CsvXPorter;
import clazzfish.monitor.stat.ClazzRecord;

import java.net.URI;
import java.util.List;

/**
 * The class CsvPrinter is an example for an implementation of the
 * {@link CsvXPorter} interface. It just prints the dead classes of the
 * clazzfish.sample package to stdout.
 *
 * @author oboehm
 * @since 2.5 (01.03.25)
 */
public class CsvPrinter implements CsvXPorter {

    /**
     * Prints only the dead classes to stdout.
     *
     * @param uri         print URI (which is ignored)
     * @param csvHeadLine head line (not needed here)
     * @param csvLines    CSV lines with the class and count infos
     */
    @Override
    public void exportCSV(URI uri, String csvHeadLine, List<String> csvLines) {
        System.err.println("\n===== DEAD CLASSES =====");
        for (String line : csvLines) {
            ClazzRecord clazzRecord = ClazzRecord.fromCSV(line);
            if ((clazzRecord.count() == 0) && clazzRecord.classname().startsWith("clazzfish.sample")) {
                System.err.println(clazzRecord.classname());
            }
        }
    }

}
