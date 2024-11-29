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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ServiceLoader;

/**
 * The starter class loads automatical the Monitor classes.
 *
 * @author oboehm
 * @since 2.3 (22.11.24)
 */
public final class Starter implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(Starter.class);

    static {
        ServiceLoader.load(Serializable.class).forEach(r -> log.debug("Loading {}...", r));
        main();
    }

    public static void main(String... args) {
        ClasspathMonitor.getInstance().registerMeAsMBean();
        ResourcepathMonitor.getInstance().registerMeAsMBean();
        log.debug("ClazzFish library is started and ready.");
    }

}
