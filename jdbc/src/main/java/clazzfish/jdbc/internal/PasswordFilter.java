/*
 * Copyright (c) 2023 by Oli B.
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
 * (c)reated 21.04.23 by oboehm
 */
package clazzfish.jdbc.internal;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The PasswordFilter tries to detect SQL commands with password arguments
 * like "INSERT INTO users (name, password) VALUES ('James', 'secret')".
 * The values of the password is masked to hide the secret.
 *
 * @author oboehm
 * @since 2.1 (21.04.23)
 */
public class PasswordFilter {

    private static final Pattern INSERT_PATTERN = Pattern.compile("(.*)\\((.*)\\)(.*)\\((.*)\\)(.*)", Pattern.CASE_INSENSITIVE);
    private static final Pattern UPDATE_PATTERN = Pattern.compile("(.+)(PASSW[ORD]*\\s*)=(.+)", Pattern.CASE_INSENSITIVE);

    /**
     * Masks passwords in given SQL command.
     *
     * @param command e.g. "INSERT INTO users (name, password) VALUES ('James', 'secret')"
     * @return e.g. "INSERT INTO users (name, password) VALUES ('James', ...)
     */
    public static String filter(String command) {
        String normalized = command.toUpperCase().trim();
        if (normalized.contains("PASSW")) {
            if (normalized.startsWith("INSERT")) {
                return maskInsertPassword(command);
            } else {
                return maskUpdatePassword(command);
            }
        }
        return command;
    }

    // parses a string like "INSERT INTO users (name, password) VALUES ('James', 'secret')"
    private static String maskInsertPassword(String command) {
        Matcher matcher = INSERT_PATTERN.matcher(command);
        if (!matcher.matches()) {
            return command;
        }
        String[] argnames = matcher.group(2).trim().split(",");
        String[] values = matcher.group(4).trim().split(",");
        StringBuilder buf = new StringBuilder(matcher.group(1)).append('(');
        for (int i = 0; i < argnames.length; i++) {
            String arg = argnames[i].trim();
            buf.append(arg).append(", ");
            if (arg.toUpperCase().startsWith("PASSW")) {
                values[i] = "...";
            }
        }
        buf.delete(buf.length()-2, buf.length());
        buf.append(')').append(matcher.group(3)).append('(');
        for (String value : values) {
            buf.append(value).append(", ");
        }
        buf.delete(buf.length()-2, buf.length());
        buf.append(')').append(matcher.group(5));
        return buf.toString();
    }

    // parses a string like "UPDATE users SET password = 'topsecret' WHERE name = 'James'"
    private static String maskUpdatePassword(String command) {
        Matcher matcher = UPDATE_PATTERN.matcher(command + "  ");
        if (!matcher.matches()) {
            return command;
        }
        String afterAssignment = matcher.group(3).trim();
        String passwd = afterAssignment.split("\\s|,")[0];
        return matcher.group(1) + matcher.group(2) + "= ..." + afterAssignment.substring(passwd.length());
    }

}
