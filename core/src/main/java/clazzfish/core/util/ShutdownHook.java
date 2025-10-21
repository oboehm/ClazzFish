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
 * (c)reated 13.10.25 by oboehm
 */
package clazzfish.core.util;

import java.util.logging.Logger;

/**
 * This class has the necessary methods for registration a class
 * as shutdown hook. For this you must overwrite the {@link #run()} method
 * which is called during shutdown.
 *
 * @author oboehm
 * @since 3.0
 */
public class ShutdownHook extends Thread implements Shutdownable {

    private static final Logger log = Logger.getLogger(ShutdownHook.class.getName());
    private boolean shutdownHook = false;

    /**
     * Here you can ask if the class was already registered as shutdown hook.
     *
     * @return true if it is registered as shutdown hook.
     */
    public synchronized boolean isShutdownHook() {
        return shutdownHook;
    }

    /**
     * To be able to register the class as shutdown hook via JMX we can't use
     * a static method - this is the reason why this additional method was
     * added.
     * <p>
     * NOTE: Don't add a class as shutdown hook in a web environment, e.g.
     * Tomcat or other servlet container. It may avoid Tomcat from shuttong
     * down as expected.
     * </p>
     */
    public void addMeAsShutdownHook() {
        Runtime.getRuntime().addShutdownHook(this);
        this.shutdownHook = true;
        log.fine(this + " is registered as shutdown hook.");
    }

    /**
     * If you want to unregister the instance as shutdown hook you can use this
     * (not static) method.
     */
    public void removeMeAsShutdownHook() {
        if (this.shutdownHook) {
            Runtime.getRuntime().removeShutdownHook(this);
            this.shutdownHook = false;
            log.fine(this + " is de-registered as shutdown hook.");
        }
    }

}
