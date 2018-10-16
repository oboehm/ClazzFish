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
 * (c)reated 07.03.2018 by oboehm (ob@oasd.de)
 */
package clazzfish.monitor.exception;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link NotFoundException}.
 */
public class NotFoundExceptionTest {
    
    private static final Logger LOG = LoggerFactory.getLogger(NotFoundExceptionTest.class);

    /**
     * Test method for {@link NotFoundException#NotFoundException(String, Throwable)}.
     */
    @Test
    public void testNotFoundExceptionStringThrowable() {
        Throwable cause = new IllegalArgumentException("huhu");
        try {
            throw new NotFoundException("searching...", cause);
        } catch (NotFoundException expected) {
            LOG.debug("Excpected exception happenend:", expected);
            assertEquals(cause, expected.getCause());
        }
    }

    /**
     * Test method for {@link NotFoundException#NotFoundException(String)}.
     */
    @Test
    public void testNotFoundExceptionString() {
        NotFoundException ex = new NotFoundException("test");
        assertEquals("test", ex.getMessage());
    }

    /**
     * Test method for {@link NotFoundException#NotFoundException(Object)}.
     */
    @Test
    public void testValueNotFoundExceptionObject() {
        Pattern pattern = Pattern.compile("world");
        NotFoundException ex = new NotFoundException(pattern);
        String msg = ex.getMessage();
        assertTrue(msg.contains("world"), msg);
    }

    /**
     * Test method for {@link NotFoundException#NotFoundException(Object)}.
     */
    @Test
    public void testValueNotFoundExceptionDate() {
        Date now = new Date();
        NotFoundException ex = new NotFoundException(now);
        String msg = ex.getMessage();
        assertTrue(msg.contains("value"), msg);
    }

}
