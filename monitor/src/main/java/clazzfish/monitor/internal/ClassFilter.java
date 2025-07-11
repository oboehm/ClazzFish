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
 * (c)reated 12.06.25 by oboehm
 */
package clazzfish.monitor.internal;

import clazzfish.monitor.Config;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The ClassFilter was introduced with issue #34. It allows you to exclude
 * classes which are of no interest for you.
 *
 * @author oboehm
 * @since 2.7 (12.06.25)
 */
public final class ClassFilter {

    public static ClassFilter DEFAULT = new ClassFilter();
    private final Pattern exclude;

    private ClassFilter() {
        this(Config.DEFAULT.getProperty(Config.PATTERN_EXCLUDE));
    }

    public ClassFilter(String pattern) {
        this.exclude = Pattern.compile(pattern);
    }

    public boolean isIncluded(String name) {
        Matcher matcher = exclude.matcher(name);
        return !matcher.matches();
    }

}
