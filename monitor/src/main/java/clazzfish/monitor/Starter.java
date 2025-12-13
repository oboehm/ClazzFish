/*
 * Copyright (c) 2024,2025 by Oli B.
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

import clazzfish.core.Config;
import clazzfish.core.stat.ClazzStatistic;
import clazzfish.monitor.jmx.AgentFinder;
import clazzfish.monitor.spi.XPorter;
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
    private static URI dumpURI = Config.DEFAULT.getDumpURI();

    /**
     * Registers all MBeans for monitoring the classpath and resources.
     * Also the {@link ClazzStatistic} is registered for importing and
     * exporting the statistics of loaded classes.
     */
    public static void start() {
        ClasspathMonitor.getInstance().registerMeAsMBean();
        ResourcepathMonitor.getInstance().registerMeAsMBean();
        log.debug("ClazzFish library is started and ready.");
    }

    /**
     * Does not register all MBeans but add also the ClazzStatistic as shutdown
     * hook.
     */
    public static void record() {
        start();
        AgentFinder agentFinder = new AgentFinder();
        if (agentFinder.isAgentAvailable()) {
            agentFinder.setDumpURI(dumpURI);
        }
        if (isAgentRecording(agentFinder)) {
            log.trace("Dumping of statistic to {} is done by ClasspathAgent.", dumpURI);
        } else {
            ClazzStatistic statistic = ClazzStatistic.of(XPorter.createCsvXPorter(dumpURI));
            statistic.addMeAsShutdownHook();
            log.trace("{} is registered as shutdown hook.", statistic);
        }
    }

    private static boolean isAgentRecording(AgentFinder agentFinder) {
        if (agentFinder.isAgentAvailable()) {
            return agentFinder.isDumping();
        } else {
            log.debug("No agent available.");
            return false;
        }
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
     * Register all MBeans and add also the ClazzStatistic as shutdown
     * hook.
     *
     * @param base URI where the statistic is stored
     * @since 2.5
     */
    public static void record(URI base) {
        setDumpURI(base);
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
        setDumpDir(dir);
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
        setDumpURI(base);
        recordAll();
    }

    private static void setDumpURI(URI uri) {
        dumpURI = uri;
    }

    private static void setDumpDir(File dir) {
        setDumpURI(dir.toURI());
    }

}
