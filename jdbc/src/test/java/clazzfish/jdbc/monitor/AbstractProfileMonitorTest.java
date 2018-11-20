/*
 * Copyright (c) 2008 by Oliver Boehm
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
 * (c)reated 19.12.2008 by oliver (ob@oasd.de)
 */

package clazzfish.jdbc.monitor;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Abstract super class to test all classes which implements the
 * ProfileMonitor interface.
 *
 * @author <a href="boehm@javatux.de">oliver</a>
 */
public abstract class AbstractProfileMonitorTest {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractProfileMonitorTest.class);
    private ProfileMonitor profMon;

    /**
     * Sets the profile monitor.
     *
     * @param profMon the profMon to set
     */
    protected void setProfileMonitor(final ProfileMonitor profMon) {
        this.profMon = profMon;
    }

    /**
     * Test get hits.
     */
    @Test
    public final void testGetHits() {
        profMon.add(0.1);
        profMon.add(0.2);
        LOG.info("profMon = " + profMon);
        assertEquals(2, profMon.getHits());
        assertEquals(0.2, profMon.getLastValue(), 0.01);
    }

    /**
     * Test min max.
     */
    @Test
    public final void testMinMax() {
        profMon.add(0.1);
        profMon.add(0.2);
        profMon.add(0.3);
        LOG.info("profMon = " + profMon);
        assertEquals(0.1, profMon.getMin(), 0.01);
        assertEquals(0.3, profMon.getMax(), 0.01);
    }

    /**
     * Test last value.
     */
    @Test
    public final void testLastValue() {
        long t0 = System.currentTimeMillis();
        long tn0 = System.nanoTime();
        profMon.start();
        profMon.stop();
        long tn = System.nanoTime() - tn0;
        long t = System.currentTimeMillis() - t0;
        double diff = t - profMon.getLastValue();
        LOG.info("measured difference: " + diff + " ms / overhead: " + tn
				+ " ns");
        assertEquals(8.0, diff, 16.0);
    }

    /**
     * Test method for {@link AbstractProfileMonitor#getLastTime()}. Watch the
     * log and look at the printed time - this method is only a half-automatic
     * test. The following result is expected (and tested) for small values:
     * <ul>
     *  <li>"0 ms" and not "0.0 ms" or "0.000 ms"</li>
     *  <li>"0.004 ms" and not "0.0040 ms" (i.e. only 3 digits </li>
     * </ul>
     */
    @Test
    public final void testLastTime() {
        profMon.start();
        profMon.stop();
        String time = profMon.getLastTime();
        LOG.info("time = {}", time);
        assertFalse(time.equals("0.0 ms"), "expected: 0 ms");
        assertFalse(time.equals("0.000 ms"), "expected: 0 ms");
        if (!time.equals("0 ms")) {
            assertTrue(time.matches("0\\.\\d+ ms"), "expected format: #.### ms");
        }
    }

    /**
     * Test get avg.
     */
    @Test
    public final void testGetAvg() {
        profMon.reset();
        profMon.start();
        LOG.info("{} is started.");
        profMon.stop();
        LOG.info("avg/avgActive/active={}/{}", profMon.getAvg(), profMon.getAvgActive());
        assertEquals(profMon.getLastValue(), profMon.getAvg(), 16.0);
    }

    /**
     * Test method for {@link AbstractProfileMonitor#getActive()}.
     */
    @Test
    public final void testGetActive() {
    	double active = profMon.getActive();
    	assertTrue(active >= 0.0, "positive number expected: " + active);
    }

    /**
     * Test method for {@link AbstractProfileMonitor#getFirstAccess()}
     * and {@link AbstractProfileMonitor#getLastAccess()}.
     */
    @Test
    public final void testGetFirstAccess() {
        Date firstAccess = profMon.getFirstAccess();
        profMon.start();
        profMon.stop();
        Date lastAccess = profMon.getLastAccess();
        assertTrue(!firstAccess.after(lastAccess), "expected: " + firstAccess + " < " + lastAccess);
    }

    /**
     * Gets the gets the units.
     */
    @Test
    public final void testGetUnits() {
    	String units = profMon.getUnits();
    	assertTrue(StringUtils.isNotEmpty(units), "units expected");
    	LOG.info("units = {}", units);
    }

    /**
     * Test add.
     */
    @Test
    public final void testAdd() {
        profMon.reset();
        profMon.add(0.1);
        assertEquals(profMon.getLastValue(), 0.1, 0.01);
    }

    /**
     * Test to short string.
     */
    @Test
    public final void testToShortString() {
    	String normalString = profMon.toString();
    	String shortString = profMon.toShortString();
    	LOG.info("profMon = \"{}\"", shortString);
    	assertTrue(shortString.length() <= normalString.length(), "too long: " + shortString);
    }

    /**
     * Here we test the equals method.
     */
    @Test
    public final void testEquals() {
        assertEquals(profMon, profMon);
    }

}
