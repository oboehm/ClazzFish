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
 * (c)reated 20.12.24 by oboehm
 */
package clazzfish.jdbc;

import clazzfish.core.Config;
import clazzfish.monitor.Starter;
import clazzfish.monitor.spi.XPorter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;

/**
 * The JdbcStarter class loads automatically the Monitor and Statistic
 * classes.
 *
 * @author oboehm
 * @since 2.3 (20.12.24)
 */
public final class JdbcStarter {

    private static final Logger log = LoggerFactory.getLogger(JdbcStarter.class);
    private static URI dumpURI = Config.DEFAULT.getDumpURI();

    /**
     * Registers the MBeans for monitoring the connection.
     */
    public static void start() {
        ConnectionMonitor.getInstance().registerMeAsMBean();
        log.debug("ClazzFish JDBC library is started and ready.");
    }

    /**
     * Registers all MBeans of jdbc module but also all MBeans of monitor
     * module.
     */
    public static void startAll() {
        Starter.start();
        start();
    }

    /**
     * Does not register all MBeans but add them also as shutdown hook.
     * So at the end of the program all collected dates are exported to
     * "SqlStatistic.csv".
     */
    public static void record() {
        start();
        SqlStatistic statistic = SqlStatistic.getInstance();
        statistic.setXPorter(XPorter.createCsvXPorter(dumpURI));
        statistic.registerMeAsMBean();
        statistic.addMeAsShutdownHook();
        log.trace("{} is registered as shutdown hook.", statistic);
    }

    /**
     * Does not register all MBeans but add them also as shutdown hook.
     * So at the end of the program all collected dates are exported to
     * the given directory.
     *
     * @param dir directory where the dates are stored
     */
    public static void record(File dir) {
        setDumpDir(dir);
        record();
    }

    /**
     * Register all MBeans and add them also as shutdown hook.
     * So at the end of the program all collected dates are exported to
     * the base URI into "SqlStatistic.csv".
     *
     * @param base URI where the dates are stored
     * @since 2.5
     */
    public static void record(URI base) {
        setDumpURI(base);
        record();
    }

    /**
     * Registers all MBeans of jdbc module but also the statistic MBean of
     * monitor module as shutdown hook. This means at the end of the program all
     * all statistic dates of the different modules are exported (into
     * "ClazzStatistic.csv" and "SqlStatistic.csv".
     * <p>
     * If you want to export ALL dates including the dates of the monitor
     * MBeans you have to call
     * </p>
     * <ul>
     *     <li>Starter.recordAll()</li>
     *     <li>JdbcStarter.record()</li>
     * </ul>
     */
    public static void recordAll() {
        Starter.record(dumpURI);
        record();
    }

    /**
     * Does not register all MBeans but add them also as shutdown hook.
     * So at the end of the program all collected dates are exported to
     * the given directory (to "SqlStatistic.csv" and "ClazzStatistic.csv").
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
     * the base URI (into "SqlStatistic.csv" and "ClazzStatistic.csv").
     *
     * @param base URI where the dates are stored
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
