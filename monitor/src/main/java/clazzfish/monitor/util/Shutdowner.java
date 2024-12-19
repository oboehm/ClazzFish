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
package clazzfish.monitor.util;

import clazzfish.monitor.ClassloaderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class Shutdowner has the necessary methods for registration a class
 * as shutdown hook. For this you must overwrite the {@link #run()} method
 * which is called during shutdown.
 *
 * @author oboehm
 * @since 2.3 (29.11.24)
 */
public abstract class Shutdowner extends Thread implements Shutdownable {

    private static final Logger log = LoggerFactory.getLogger(Shutdowner.class);
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
     * If this class is registered as shutdown hook from a web
     * application a dump to a directory may not be possible because the
     * application server (e.g. Tomcat) has been stopped already the web
     * application instance. This was the problem as decribed in
     * <a href="https://sourceforge.net/p/patterntesting/bugs/37/">bugs/37</a>.
     * </p>
     * <p>
     * In this situation it is not possible to add the class as shutdown hook.
     * </p>
     */
    public void addMeAsShutdownHook() {
        ClassloaderType type = ClassloaderType.getCurrentClassloaderType();
        if (type.isWeb()) {
            log.info("Registration as shutdown hook is ignored inside {}.", type);
        } else if (!this.shutdownHook) {
            Runtime.getRuntime().addShutdownHook(this);
            this.shutdownHook = true;
            log.debug("{} is registered as shutdown hook", this);
        }
    }

    /**
     * If you want to unregister the instance as shutdown hook you can use this
     * (not static) method.
     */
    public void removeMeAsShutdownHook() {
        Runtime.getRuntime().removeShutdownHook(this);
        this.shutdownHook = false;
        log.debug("{} is de-registered as shutdown hook", this);
    }

}
