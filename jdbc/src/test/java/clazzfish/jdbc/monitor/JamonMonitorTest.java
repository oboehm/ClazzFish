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
 * (c)reated 25.12.2008 by oliver (ob@oasd.de)
 */
package clazzfish.jdbc.monitor;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link JamonMonitor} class.
 *
 * @author <a href="boehm@javatux.de">oliver</a>
 */
public final class JamonMonitorTest extends AbstractProfileMonitorTest {

    private JamonMonitor jamonMon;

    /**
     * Sets up the {@link JamonMonitor} for testing.
     */
    @BeforeEach
    public void setUp() {
        Monitor monitor = MonitorFactory.getMonitor();
        jamonMon = new JamonMonitor(monitor);
        this.setProfileMonitor(jamonMon);
    }

    /**
     * Test method for {@link JamonMonitor#enable()} and
     * {@link JamonMonitor#disable()}.
     */
    @Test
    public void testDisableEnable() {
        jamonMon.disable();
        assertFalse(jamonMon.isEnabled(), "disabled expected for " + jamonMon);
        jamonMon.enable();
        assertTrue(jamonMon.isEnabled(), "enabled expected for " + jamonMon);
    }

    /**
     * Test method for {@link JamonMonitor#setLastAccess(Date)}.
     */
    @Test
    public void testSetLastAccess() {
        Date now = new Date();
        jamonMon.setLastAccess(now);
        assertEquals(now, jamonMon.getLastAccess());
    }

    /**
     * The result of {@link JamonMonitor#toCsvString()} should have the same
     * number of elements as the headline.
     */
    @Test
    public void testToCsvString() {
    	String headline = jamonMon.toCsvHeadline();
    	String line = jamonMon.toCsvString();
    	assertEquals(headline.split(";").length, line.split(";").length);
    }

}
