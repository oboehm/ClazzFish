/*
 * Copyright (c) 2012-2018 by Oliver Boehm
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
 * (c)reated 17.03.2012 by oliver (ob@oasd.de)
 */

package clazzfish.core.logging;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * We want a {@link Formatter} which does not log in two lines as the
 * SimpleFormatter but only in one line.
 *
 * @author oliver (ob@aosd.de)
 */
public final class SingleLineFormatter extends Formatter {

    private final DateFormat dateFormat = new SimpleDateFormat("MMM dd HH:mm:ss");

    /**
     * Formats the given record in one line.
     *
     * @param record the record
     * @return the string
     * @see Formatter#format(LogRecord)
     */
    @Override
    public String format(final LogRecord record) {
        String msg = formatMessage(record);
        Date eventTime = new Date(record.getMillis());
        return dateFormat.format(eventTime) + " " + record.getLevel() + ": " + msg + "\n";
    }

    @Override
    public synchronized String formatMessage(LogRecord record) {
        if (record.getParameters() != null) {
            return MessageFormat.format(record.getMessage(), record.getParameters());
        }
        return record.getMessage();
    }

}
