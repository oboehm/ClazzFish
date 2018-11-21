/*
 * $Id: JamonMonitorFactoryTest.java,v 1.5 2016/10/24 20:22:05 oboehm Exp $
 *
 * Copyright (c) 2014 by Oliver Boehm
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
 * (c)reated 22.04.2014 by oliver (ob@oasd.de)
 */

package clazzfish.jdbc.monitor;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

/**
 * Unit tests for {@link JamonMonitorFactory} class.
 *
 * @author oliver
 * @since 1.4.2 (22.04.2014)
 */
public final class JamonMonitorFactoryTest extends ProfileMonitorFactoryTest {

	/**
	 * Gets the profile monitor factory.
	 *
	 * @return the profile monitor factory
	 * @see ProfileMonitorFactoryTest#createProfileMonitorFactory()
	 */
	@Override
	protected ProfileMonitorFactory createProfileMonitorFactory() {
		return new JamonMonitorFactory(new SimpleProfileMonitor("JAMON-TEST"));
	}

    /**
     * Test method for {@link JamonMonitorFactory#start(String, ProfileMonitor)}.
     */
    @Test
    public void testStart() {
        ProfileMonitor mon = JamonMonitorFactory.start("testStart", "TWO");
        mon.stop();
        ProfileMonitor[] monitors = JamonMonitorFactory.getMonitors("TWO");
        assertThat(Arrays.toString(monitors), containsString("testStart"));
    }

    /**
	 * Test method for {@link ProfileMonitorFactory#setMaxNumMonitors(int)}.
	 * Because we cannot reduce the number of monitors with JAMon if the new
	 * number is too small we want to see at least a warning in the log.
	 */
	@Test
	public void testSetMaxNumMonitorsWithResize() {
		ProfileMonitorFactory factory = this.getFactory();
		addMethodsOf(ProfileMonitorFactory.class);
		factory.setMaxNumMonitors(3);
	}

}

