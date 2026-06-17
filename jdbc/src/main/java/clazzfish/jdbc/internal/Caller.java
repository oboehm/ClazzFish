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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * The class Caller looks for the caller by analyzing the stacktrace.
 * Before 3.1 it was part of the {@link StasiStatement} class.
 *
 * @author oboehm
 * @since 3.1 (15.06.26)
 */
public final class Caller {

    private static final Logger log = LoggerFactory.getLogger(Caller.class);
    private static final Map<StackTraceElement, Caller> WEAK_CACHE = new WeakHashMap<>();
    private static final Map<Caller, Set<StackTraceElement[]>> STACKTRACE_CACHE = new WeakHashMap<>();

    private final StackTraceElement stackTraceElement;

    private Caller(StackTraceElement stackTraceElement) {
        this.stackTraceElement = stackTraceElement;
    }

    /**
     * Creates a caller element with the corresponding stack trace entry.
     * To avoid to many objects of the same caller the created instances
     * are cached.
     *
     * @param ignoredClasses caller which should be ignored
     * @return caller of the of method
     */
    public static Caller of(final Class<?>... ignoredClasses) {
        StackTraceElement [] stackTraceElements = getCallerStacktrace(ignoredClasses);
        return of(stackTraceElements[0]);
    }

    /**
     * Creates a caller element with the given stack trace entry.
     * To avoid too many objects of the same caller the created instances
     * are cached.
     *
     * @param stackTraceElement the stacktrace element of the caller
     * @return caller of the of method
     */
    public static Caller of(StackTraceElement stackTraceElement) {
        return WEAK_CACHE.computeIfAbsent(stackTraceElement, Caller::new);
    }

    /**
     * Gets the caller stacktrace. To find the real caller we ignore the first
     * element from the stacktrace because this is e.g. the method
     * {@link Thread#getStackTrace()} which is not relevant here.
     * <p>
     * In case of a connection pool we have many caller with the same#
     * stacktrace. For this rease we cache the stacktrace to reduche the
     * number of doublettes.
     * </p>
     *
     * @param ignoredClasses the ignored classes
     * @return the caller stacktrace
     */
    public static StackTraceElement[] getCallerStacktrace(final Class<?>... ignoredClasses) {
        StackTraceElement[] stacktraceCaller = getUncachedCallerStacktrace(ignoredClasses);
        Caller caller = Caller.of(stacktraceCaller[0]);
        Set<StackTraceElement[]> cached = STACKTRACE_CACHE.get(caller);
        if (cached == null) {
            cached = new HashSet<>();
            cached.add(stacktraceCaller);
            STACKTRACE_CACHE.put(caller, cached);
        } else {
            for (StackTraceElement[] element : cached) {
                if (Arrays.equals(element, stacktraceCaller)) {
                    log.trace("Using cached stacktrace as result.");
                    return element;
                }
            }
            cached.add(stacktraceCaller);
        }
        return stacktraceCaller;
    }

    private static StackTraceElement[] getUncachedCallerStacktrace(final Class<?>... ignoredClasses) {
        Class<?>[] clazzes = Arrays.copyOf(ignoredClasses, ignoredClasses.length + 1);
        clazzes[ignoredClasses.length] = Caller.class;
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        for (int i = 1; i < stacktrace.length; i++) {
            String classname = stacktrace[i].getClassName();
            if (!(classname.startsWith("com.sun.proxy.") || classname.startsWith("jdk.proxy") || matches(classname, clazzes))) {
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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Caller)) return false;
        Caller caller = (Caller) o;
        return Objects.equals(stackTraceElement, caller.stackTraceElement);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(stackTraceElement);
    }

}
