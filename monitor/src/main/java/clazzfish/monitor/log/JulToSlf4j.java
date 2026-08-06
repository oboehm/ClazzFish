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
 * (c)reated 06.08.26 by oboehm
 */
package clazzfish.monitor.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * The JulToSlf4j class routes the java.util.logging (JUL) records of the
 * clazzfish-core classes to the SLF4J facade so that they are handled by the
 * configured SLF4J binding. If the optional jul-to-slf4j bridge is not on the
 * classpath the installation is skipped silently.
 *
 * @author oboehm
 * @since 3.1
 */
public final class JulToSlf4j {

    private static final Logger LOG = LoggerFactory.getLogger(JulToSlf4j.class);
    private static boolean installed = false;

    private JulToSlf4j() {
    }

    /**
     * Installs the JUL to SLF4J bridge. The installation is done only once.
     */
    public static synchronized void install() {
        if (installed) {
            return;
        }
        try {
            Class<?> bridge = Class.forName("org.slf4j.bridge.SLF4JBridgeHandler");
            invokeStatic(bridge, "removeHandlersForRootLogger");
            invokeStatic(bridge, "install");
            LOG.debug("java.util.logging is bridged to SLF4J.");
        } catch (ClassNotFoundException cnfe) {
            LOG.debug("jul-to-slf4j bridge is not on the classpath.");
        } catch (ReflectiveOperationException roe) {
            LOG.warn("Cannot install the jul-to-slf4j bridge:", roe);
        } finally {
            installed = true;
        }
    }

    private static void invokeStatic(Class<?> bridge, String name)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method method = bridge.getMethod(name);
        method.invoke(null);
    }

}
