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
 * (c)reated 28.12.24 by oboehm
 */
package clazzfish.monitor.jmx;

import clazzfish.monitor.ClasspathMonitor;
import clazzfish.monitor.stat.ClazzStatistic;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link MBeanFinder} ...
 *
 * @author oboehm
 * @since 28.12.24
 */
class MBeanFinderTest {

    @Test
    void getMBeanName() {
        String mbeanName = MBeanFinder.getMBeanName(ClasspathMonitor.class);
        assertEquals("clazzfish:type=monitor,name=ClasspathMonitor", mbeanName);
    }

    @Test
    void getMBeanNameLevel1() {
        String mbeanName = MBeanFinder.getMBeanName(ClazzStatistic.class);
        assertEquals("clazzfish:type=monitor,monitor=stat,name=ClazzStatistic", mbeanName);
    }

}