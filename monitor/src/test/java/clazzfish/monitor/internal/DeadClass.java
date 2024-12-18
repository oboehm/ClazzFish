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
 * (c)reated 15.12.24 by oboehm
 */
package clazzfish.monitor.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is an example of a class which is never loaeded. It is used
 * for testing.
 *
 * @author oboehm
 * @since 2.3 (15.12.24)
 */
public class DeadClass {

    private static final Logger log = LoggerFactory.getLogger(DeadClass.class);

    static {
        log.error("This class should be never loaded!");
    }

}
