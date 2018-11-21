/*
 * Copyright (c) 2015 by Oli B.
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
 * (c)reated 19.12.2015 by Oli B. (ob@aosd.de)
 */

package clazzfish.jdbc.monitor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link SimpleProfileMonitorFactory}.
 *
 * @author oboehm (ob@aosd.de)
 * @since 1.6 (19.12.2015)
 */
public final class SimpleProfileMonitorFactoryTest extends ProfileMonitorFactoryTest {

	private final SimpleProfileMonitorFactory simpleFactory = (SimpleProfileMonitorFactory) this.getFactory();

	/**
	 * Gets the profile monitor factory.
	 *
	 * @return the profile monitor factory
	 * @see ProfileMonitorFactoryTest#createProfileMonitorFactory()
	 */
	@Override
	protected SimpleProfileMonitorFactory createProfileMonitorFactory() {
		return new SimpleProfileMonitorFactory(new SimpleProfileMonitor("SIMPLE-TEST"));
	}

	/**
	 * Test method for {@link SimpleProfileMonitorFactory#getMonitor(String)}.
	 */
	@Test
	public void testGetMonitor() {
		simpleFactory.setMaxNumMonitors(5);
		addMethodsOf(this.getClass());
		assertEquals(5, simpleFactory.getMonitors().length);
	}

	/**
	 * Test method for {@link ProfileMonitorFactory#setMaxNumMonitors(int)}.
	 */
	@Test
	public void testSetMaxNumMonitorsWithResize() {
		addMethodsOf(ProfileMonitorFactory.class);
		simpleFactory.setMaxNumMonitors(3);
		ProfileMonitor[] monitors = simpleFactory.getMonitors();
		assertEquals(3, monitors.length);
	}

}
