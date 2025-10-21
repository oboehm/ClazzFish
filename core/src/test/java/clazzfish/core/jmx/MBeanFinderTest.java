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
 * (c)reated 28.12.24 by oboehm
 */
package clazzfish.core.jmx;

import clazzfish.core.Config;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link clazzfish.core.jmx.MBeanFinder} ...
 *
 * @author oboehm
 * @since 28.12.24
 */
class MBeanFinderTest {

    @Test
    void getMBeanName() {
        String mbeanName = MBeanFinder.getMBeanName(Config.class);
        assertEquals("clazzfish:type=core,name=Config", mbeanName);
    }

    @Test
    void getMBeanNameLevel1() {
        String mbeanName = clazzfish.core.jmx.MBeanFinder.getMBeanName(MBeanFinder.class);
        assertEquals("clazzfish:type=core,core=jmx,name=MBeanFinder", mbeanName);
    }

}