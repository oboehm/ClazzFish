/*
 * $Id: SimpleProfileMonitorTest.java,v 1.13 2016/12/10 20:55:22 oboehm Exp $
 *
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The Class SimpleProfileMonitorTest.
 *
 * @author <a href="boehm@javatux.de">oliver</a>
 * @version $Revision: 1.13 $
 * @since 25.12.2008
 */
public final class SimpleProfileMonitorTest extends AbstractProfileMonitorTest {

    private static SimpleProfileMonitor rootMonitor = new SimpleProfileMonitor();

    /**
     * Sets up the {@link SimpleProfileMonitor} for testing.
     */
    @BeforeEach
    public void setUp() {
        this.setProfileMonitor(new SimpleProfileMonitor());
    }

    /**
     * Test signature hits.
     */
    @Test
    public void testSignatureHits() {
        SimpleProfileMonitor mon1 = createSimpleProfileMonitor();
        SimpleProfileMonitor mon2 = createSimpleProfileMonitor();
        mon1.add(0.1);
        mon2.add(0.2);
        assertEquals(2, mon1.getHits());
    }

    private SimpleProfileMonitor createSimpleProfileMonitor() {
        return getProfileMonitor("test");
    }

    private static SimpleProfileMonitor getProfileMonitor(String sig) {
        SimpleProfileMonitor monitor = rootMonitor.getMonitor(sig);
        if (monitor == null) {
            monitor = new SimpleProfileMonitor(sig, rootMonitor);
        }
        return monitor;
    }

    /**
     * In the past there was an error for two empty
     * {@link SimpleProfileMonitor}s - they are not equal but the
     * compareTo()-Method returns a "0".
     */
    @Test
    public void testCompareTo() {
        SimpleProfileMonitor mon1 = new SimpleProfileMonitor();
        SimpleProfileMonitor mon2 = new SimpleProfileMonitor();
        assertEquals(mon1, mon2);
        assertEquals(0, mon1.compareTo(mon2));
    }

    /**
	 * Two monitors with different labels should be never returns a 0 as a
	 * result. Otherwise we got some troubles with the use of
	 * {@link SimpleProfileMonitor} inside a Set.
	 *
	 * @since 1.6
	 */
    @Test
    public void testCompareWithDifferentLabel() {
        SimpleProfileMonitor mon1 = new SimpleProfileMonitor("one");
        SimpleProfileMonitor mon2 = new SimpleProfileMonitor("two");
        Set<SimpleProfileMonitor> monitors = new ConcurrentSkipListSet<>();
        monitors.add(mon1);
        monitors.add(mon2);
        assertEquals(2, monitors.size());
    }

    /**
     * Test method for {@link SimpleProfileMonitor#getMonitor(String)}. For
     * some reasons it can happen that a null value is given as label. This
     * should not result in a NPE.
     */
    @Test
    public void testGetMonitorNull() {
    	rootMonitor.getMonitor(null);
    }

    /**
	 * Test method for
	 * {@link SimpleProfileMonitor#removeMonitor(SimpleProfileMonitor)}.
	 */
    @Test
    public void testRemoveMonitor() {
    	int n = rootMonitor.getNumberOfMonitors();
    	SimpleProfileMonitor child = new SimpleProfileMonitor("testRemoveMonitor");
    	rootMonitor.addChild(child);
    	child.start();
    	assertEquals(n+1, rootMonitor.getNumberOfMonitors());
    	child.stop();
    	rootMonitor.removeMonitor(child);
    	assertEquals(n, rootMonitor.getNumberOfMonitors());
    }

    /**
	 * Test method for
	 * {@link SimpleProfileMonitor#removeMonitor(SimpleProfileMonitor)}. The
	 * problem with the implememtation is the implementation of the Comparable
	 * interface. If the range changes the underlying Set of monitors causes
	 * problems. DON'T CHANGE VALUES INSIDE A SET!
	 */
    @Test
    public void testRemoveMonitorRepeated() {
    	SimpleProfileMonitor[] monitors = new SimpleProfileMonitor[5];
    	for (int i = 0; i < monitors.length; i++) {
    		monitors[i] = new SimpleProfileMonitor(UUID.randomUUID().toString());
    		monitors[i].start();
    		monitors[i].stop();
    		rootMonitor.addChild(monitors[i]);
		}
    	monitors[2].start();
    	int size = rootMonitor.getNumberOfMonitors();
    	monitors[2].stop();
        for (SimpleProfileMonitor monitor : monitors) {
            rootMonitor.removeMonitor(monitor);
            size--;
            assertEquals(size, rootMonitor.getNumberOfMonitors());
        }
    }

}
