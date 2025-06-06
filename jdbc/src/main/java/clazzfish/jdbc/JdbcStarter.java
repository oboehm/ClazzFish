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
 * (c)reated 20.12.24 by oboehm
 */
package clazzfish.jdbc;

import clazzfish.monitor.Starter;
import clazzfish.monitor.Config;
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

    /**
     * Registers all MBeans for monitoring the connection and SQL statements.
     */
    public static void start() {
        ConnectionMonitor.getInstance().registerMeAsMBean();
        SqlStatistic.getInstance().registerMeAsMBean();
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
     * So at the end of the program all collected dates are exported.
     */
    public static void record() {
        start();
        ConnectionMonitor.getInstance().addMeAsShutdownHook();
        SqlStatistic.getInstance().addMeAsShutdownHook();
        log.trace("All MBeans are registered as shutdown hook.");
    }

    /**
     * Does not register all MBeans but add them also as shutdown hook.
     * So at the end of the program all collected dates are exported to
     * the given directory
     *
     * @param dir directory where the dates are stored
     */
    public static void record(File dir) {
        Config.DEFAULT.setDumpDir(dir);
        record();
    }

    /**
     * Does not register all MBeans but add them also as shutdown hook.
     * So at the end of the program all collected dates are exported to
     * the base URI.
     *
     * @param base URI where the dates are stored
     * @since 2.5
     */
    public static void record(URI base) {
        Config.DEFAULT.setDumpURI(base);
        record();
    }

    /**
     * Registers all MBeans of jdbc module but also the statistic MBean of
     * monitor module as shutdown hook. This means at the end of the program all
     * alll statistic dates of the different modules are exported.
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
        Starter.record();
        record();
    }

    /**
     * Does not register all MBeans but add them also as shutdown hook.
     * So at the end of the program all collected dates are exported to
     * the given directory.
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
     * the base URI.
     *
     * @param base URI where the dates are stored
     * @since 2.5
     */
    public static void recordAll(URI base) {
        Config.DEFAULT.setDumpURI(base);
        recordAll();
    }

}
