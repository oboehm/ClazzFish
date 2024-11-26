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
 * (c)reated 22.11.24 by oboehm
 */
package clazzfish.monitor;

import clazzfish.monitor.jmx.MBeanHelper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for @link Starter}.
 *
 * @author oboehm
 * @since 22.11.24
 */
class StarterTest {

    @Test
    void main() {
        Starter.main();
        assertTrue(MBeanHelper.isRegistered("clazzfish:type=monitor,name=ClasspathMonitor"));
        assertTrue(MBeanHelper.isRegistered("clazzfish:type=monitor,name=ResourcepathMonitor"));
    }

}