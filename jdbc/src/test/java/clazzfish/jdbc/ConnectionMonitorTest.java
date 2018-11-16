/*
 * Copyright (c) 2018 by Oliver Boehm
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * (c)reated 16.11.2018 by oboehm (ob@oasd.de)
 */
package clazzfish.jdbc;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ConnectionMonitor}.
 */
public class ConnectionMonitorTest extends AbstractDbTest {
    
    private static final ConnectionMonitor monitor = ConnectionMonitor.getInstance();

    /**
     * Returns an object for testing.
     *
     * @return the test object
     */
    @Override
    protected ConnectionMonitor getObject() {
        return monitor;
    }

}
