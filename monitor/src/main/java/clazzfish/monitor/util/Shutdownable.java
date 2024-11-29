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

import clazzfish.monitor.jmx.Description;

/**
 * With Shutownable this class can be marked which can be registered as
 * shutdonw hook.
 *
 * @author oboehm
 * @since 2.3 (28.11.24)
 */
public interface Shutdownable extends Runnable {

    /**
     * To be able to register the instance as shutdown hook you can use this
     * (non static) method.
     */
    @Description("to register monitor as shutdown hook")
    void addMeAsShutdownHook();

    /**
     * If you want to unregister the instance as shutdown hook you can use this
     * method.
     */
    @Description("to de-register monitor as shutdown hook")
    void removeMeAsShutdownHook();

    /**
     * Here you can ask if the instance was already registeres ad shutdown hook.
     *
     * @return true if it is registered as shutdown hook.
     */
    @Description("returns true if monitor was registered as shutdown hook")
    boolean isShutdownHook();

}
