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

import clazzfish.monitor.internal.Config;
import clazzfish.monitor.stat.ClazzStatistic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;

/**
 * The Starter class loads automatically the Monitor classes.
 *
 * @author oboehm
 * @since 2.3 (22.11.24)
 */
public final class Starter {

    private static final Logger log = LoggerFactory.getLogger(Starter.class);

    /**
     * Registers all MBeans for monitoring the classpath and resources.
     * Also the {@link ClazzStatistic} is registered for importing and
     * exporting the statistics of loaded classes.
     */
    public static void start() {
        ClasspathMonitor.getInstance().registerMeAsMBean();
        ResourcepathMonitor.getInstance().registerMeAsMBean();
        ClazzStatistic.getInstance().registerMeAsMBean();
        log.debug("ClazzFish library is started and ready.");
    }

    /**
     * Does not register all MBeans but add also the ClazzStatistic as shutdown
     * hook.
     */
    public static void record() {
        start();
        ClazzStatistic.getInstance().addMeAsShutdownHook();
        log.trace("ClazzStatistic MBeans is registered as shutdown hook.");
    }

    /**
     * Does not register all MBeans but add them also as shutdown hook.
     * So at the end of the program all collected dates are exported.
     */
    public static void recordAll() {
        record();
        ClasspathMonitor.getInstance().addMeAsShutdownHook();
        ResourcepathMonitor.getInstance().addMeAsShutdownHook();
        log.trace("All MBeans are registered as shutdown hook.");
    }

    /**
     * Does not register all MBeans but add also the ClazzStatistic as shutdown
     * hook.
     *
     * @param dir directory where the statistic is stored
     */
    public static void record(File dir) {
        Config.DEFAULT.setDumpDir(dir);
        record();
    }


    /**
     * Does not register all MBeans but add also the ClazzStatistic as shutdown
     * hook.
     *
     * @param base URI where the statistic is stored
     * @since 2.5
     */
    public static void record(URI base) {
        Config.DEFAULT.setDumpURI(base);
        record();
    }

    /**
     * Does not register all MBeans but add them also as shutdown hook.
     * So at the end of the program all collected dates are exported to
     * the given directory
     *
     * @param dir directory where the dates are stored
     */
    public static void recordAll(File dir) {
        Config.DEFAULT.setDumpDir(dir);
        recordAll();
    }

    /**
     * Does not register all MBeans but add them also as shutdown hook.
     * So at the end of the program all collected dates are exported to
     * the given directory
     *
     * @param base URI where the statistic is stored
     * @since 2.5
     */
    public static void recordAll(URI base) {
        Config.DEFAULT.setDumpURI(base);
        recordAll();
    }

}
