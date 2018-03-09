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
 * (c)reated 09.03.2018 by oboehm (ob@oasd.de)
 */
package clazzfish.monitor.jmx;

import org.junit.jupiter.api.Test;

import java.util.jar.Manifest;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link MBeanHelper}.
 */
class MBeanHelperTest {

    /**
     * Test method for {@link MBeanHelper#getMBeanName(Class)}.
     */
    @Test
    public final void testGetMBeanNameClass() {
        String mbeanName = MBeanHelper.getMBeanName(String.class);
        assertEquals("java:type=lang,name=String", mbeanName);
    }

    /**
     * Test method for {@link MBeanHelper#getMBeanName(String)}.
     */
    @Test
    public final void testGetMBeanNameStringOne() {
        checkMBeanName(":name=One", "One");
    }

    /**
     * Test method for {@link MBeanHelper#getMBeanName(String)}.
     */
    @Test
    public final void testGetMBeanNameStringThree() {
        checkMBeanName("one:type=two,name=For", "one.two.For");
    }

    /**
     * Test method for {@link MBeanHelper#getMBeanName(String)}.
     */
    @Test
    public final void testGetMBeanNameStringFour() {
        checkMBeanName("one:type=two,two=three,name=For", "one.two.three.For");
    }

    /**
     * Test method for {@link MBeanHelper#getMBeanName(String)}.
     */
    @Test
    public final void testGetMBeanNameStringUnfiltered() {
        checkMBeanName(":name=One", ":name=One");
    }

    private void checkMBeanName(final String expected, final String name) {
        String mbeanName = MBeanHelper.getMBeanName(name);
        assertEquals(expected, mbeanName);
    }

    /**
     * Test method for {@link MBeanHelper#getMBeanName(Class)}.
     */
    @Test
    public final void testGetMBeanNameHierarchical() {
        String mbeanName = MBeanHelper.getMBeanName(Manifest.class, 1);
        assertEquals("java:type=util,util=jar,name=Manifest", mbeanName);
    }

    /**
     * Test method for {@link MBeanHelper#getMBeanName(Class,int)}.
     */
    @Test
    public final void testGetMBeanNameLevel() {
        String mbeanName = MBeanHelper.getMBeanName(Manifest.class, 2);
        assertEquals("java.util:type=jar,name=Manifest", mbeanName);
    }
    
}
