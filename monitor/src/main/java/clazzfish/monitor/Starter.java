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
 * (c)reated 22.11.24 by oboehm
 */
package clazzfish.monitor;

import clazzfish.monitor.rec.ClazzRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * The starter class loads automatically the Monitor classes.
 *
 * @author oboehm
 * @since 2.3 (22.11.24)
 */
public final class Starter implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(Starter.class);

    /**
     * Registers all MBeans for monitoring the classpath and resources.
     * Also the {@link ClazzRecorder} is registered for importing and
     * exporting the statistics of loaded classes.
     */
    public static void start() {
        ClasspathMonitor.getInstance().registerMeAsMBean();
        ResourcepathMonitor.getInstance().registerMeAsMBean();
        ClazzRecorder.getInstance().registerMeAsMBean();
        log.debug("ClazzFish library is started and ready.");
    }

    /**
     * Does not register all MBeans but add them also as shutdown hook.
     * So at the end of the program all collected dates are exported.
     */
    public static void record() {
        start();
        ClasspathMonitor.getInstance().addMeAsShutdownHook();
        ResourcepathMonitor.getInstance().addMeAsShutdownHook();
        ClazzRecorder.getInstance().addMeAsShutdownHook();
        log.trace("All MBeans are registered as shutdown hook.");
    }

}
