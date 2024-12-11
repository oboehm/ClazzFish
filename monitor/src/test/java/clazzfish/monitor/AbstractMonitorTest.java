/*
 * $Id: AbstractMonitorTest.java,v 1.8 2017/01/07 22:03:25 oboehm Exp $
 *
 * Copyright (c) 2016 by Oliver Boehm
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
 * (c)reated 25.02.2016 by oliver (ob@oasd.de)
 */

package clazzfish.monitor;

import clazzfish.monitor.util.Shutdownable;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link AbstractMonitor} class.
 *
 * @author oliver
 */
public abstract class AbstractMonitorTest {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractMonitorTest.class);

    /**
     * Gets the monitor for testing.
     *
     * @return the monitor
     */
    protected abstract AbstractMonitor getMonitor();

    /**
     * Test if monitor can be add and removed as shutdown hook.
     */
    @Test
    public void testAddMeAsShutdownHook() {
        testAddAsShutdownHook(getMonitor());
    }

    /**
     * Test if monitor can be add and removed as shutdown hook. This static
     * method was added for ConnectionMonitorTest in db package. This test
     * class has already an (abstract) super class.
     *
     * @param mon the monitor for testing.
     */
    public static void testAddAsShutdownHook(final Shutdownable mon) {
        LOG.info("testAddAsShutdownHook() is started with {}.", mon);
        mon.addMeAsShutdownHook();
        assertTrue(mon.isShutdownHook(), mon + " should be registered as shutdown hook");
        mon.removeMeAsShutdownHook();
        assertFalse(mon.isShutdownHook(), mon + " should be de-registered as shutdown hook");
    }

    /**
     * Here we call the run method to see if it runs without an exception. The
     * run method will be called during shutdown.
     */
    @Test
    public void testRun() {
        getMonitor().run();
    }

    /**
     * Test method for {@link AbstractMonitor#registerMeAsMBean()}.
     */
    @Test
    public void testRegisterMeAsMBean() {
        AbstractMonitor monitor = getMonitor();
        monitor.registerMeAsMBean();
        assertTrue(monitor.isMBean(), "not registered: " + getMonitor());
        monitor.unregisterMeAsMBean();
        assertFalse(monitor.isMBean(), "registered: " + monitor);
    }

    @Test
    public void testGetDumpDir() {
        File dumpDir = getMonitor().getDumpDir();
        assertNotNull(dumpDir);
        if (dumpDir.exists()) {
            assertTrue(dumpDir.isDirectory());
        }
    }

    /**
     * Test method for {@link AbstractMonitor#dumpMe(File)}.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testDumpMe() throws IOException {
        checkDumpMe(getMonitor(), 1);
    }

    /**
     * Check dump me.
     *
     * @param monitor the monitor
     * @param expected number of expected of files
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void checkDumpMe(AbstractMonitor monitor, int expected) throws IOException {
        LOG.info("testDumpMe() is started with {}.", monitor);
        File dumpDir = monitor.dumpMe();
        checkDumpDir(dumpDir, expected);
        File targetDumpDir = new File("target", "dump");
        monitor.dumpMe(targetDumpDir.getPath());
        checkDumpDir(targetDumpDir, expected);
    }

    private static void checkDumpDir(File dumpDir, int expected) throws IOException {
        try {
            LOG.info("Result was dumped to '{}'.", dumpDir);
            assertTrue(dumpDir.isDirectory(), "not a directory: " + dumpDir);
            File[] dumpFiles = dumpDir.listFiles();
            assert dumpFiles != null;
            if (dumpFiles.length != expected) {
                fail(String.format("%d files expected, got: %s", expected, Arrays.toString(dumpFiles)));
            }
        } finally {
            FileUtils.deleteDirectory(dumpDir);
            LOG.info("Directory '{}' is deleted.", dumpDir);
        }
    }

}

