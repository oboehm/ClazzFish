/*
 * Copyright (c) 2026 by Oli B.
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
 * (c)reated 15.06.26 by oboehm
 */
package clazzfish.jdbc.internal;

import java.util.Arrays;

/**
 * The class Caller looks for the caller by analyzing the stacktrace.
 * Before 3.1 it was part of the {@link StasiStatement} class.
 *
 * @author oboehm
 * @since 3.1 (15.06.26)
 */
public final class Caller {

    private final StackTraceElement stackTraceElement;

    private Caller(StackTraceElement stackTraceElement) {
        this.stackTraceElement = stackTraceElement;
    }

    /**
     * Creates a caller element with the corresponding stack trace entry.
     *
     * @param ignoredClasses caller which should be ignored
     * @return caller of the of method
     */
    public static Caller of(final Class<?>... ignoredClasses) {
        StackTraceElement [] stackTraceElements = getCallerStacktrace(ignoredClasses);
        return new Caller(stackTraceElements[0]);
    }

    /**
     * Gets the caller stacktrace. To find the real caller we ignore the first 3
     * elements from the stacktrace because this is e.g. the method
     * {@link Thread#getStackTrace()} which is not relevant here.
     *
     * @param ignoredClasses the ignored classes
     * @return the caller stacktrace
     */
    public static StackTraceElement[] getCallerStacktrace(final Class<?>... ignoredClasses) {
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        for (int i = 3; i < stacktrace.length; i++) {
            String classname = stacktrace[i].getClassName();
            if (!(classname.startsWith("com.sun.proxy.") || classname.startsWith("jdk.proxy") || matches(classname, ignoredClasses))) {
                StackTraceElement[] stacktraceCaller = new StackTraceElement[stacktrace.length - i];
                System.arraycopy(stacktrace, i, stacktraceCaller, 0, stacktrace.length - i);
                return stacktraceCaller;
            }
        }
        throw new IllegalStateException("no caller found for " + Arrays.toString(ignoredClasses));
    }

    private static boolean matches(final String classname, final Class<?>... classes) {
        for (Class<?> aClass : classes) {
            if (classname.equals(aClass.getName())) {
                return true;
            }
        }
        return false;
    }

    public StackTraceElement getStackTraceElement() {
        return stackTraceElement;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " " + stackTraceElement;
    }

}
