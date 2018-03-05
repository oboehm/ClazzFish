/*
 * Copyright (c) 2009-2018 by Oliver Boehm
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
 * (c)reated 14.04.2009 by oliver (ob@aosd.de)
 */
package clazzfish.monitor.internal;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The Class ClassWalkerTest.
 *
 * @author <a href="boehm@javatux.de">oliver</a>
 */
class ClassWalkerTest {
    
    private static final Logger LOG = LoggerFactory.getLogger(ClassWalkerTest.class);
    private final File startDir = new File("target/test-classes");
    private final ClassWalker classWalker = new ClassWalker(startDir);

    /**
     * Test method for {@link ClassWalker#getClasses()}.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public final void testGetClasses() throws IOException {
        Collection<String> classes = classWalker.getClasses();
        assertTrue(classes.size() > 0, "no classes found");
        LOG.info("{} classes found in {}.", classes.size(), startDir.getAbsolutePath());
        LOG.debug("{}", classes);
        String firstClass = classes.iterator().next();
        assertFalse(firstClass.startsWith("."), firstClass + " is not a classname");
    }

}
