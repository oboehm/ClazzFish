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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     * Registers all MBeans of jdbc module but also all MBeans of monitor
     * module as shutdown hook. This means at the end of the program all
     * collected dates of the different modules are exported
     */
    public static void recordAll() {
        Starter.record();
        record();
    }

}
