/*
 * Copyright (c) 2025 by Oli B.
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
 * (c)reated 16.06.25 by oboehm
 */
package clazzfish.monitor.internal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ResourceFilter}.
 *
 * @author oboehm
 * @since 16.06.25
 */
class ResourceFilterTest {

    private final ResourceFilter filter = ResourceFilter.DEFAULT;

    @Test
    void isIncluded() {
        assertTrue(filter.isIncluded("/java/lang/Object "));
    }

    @Test
    void isIncludedModuleInfo() {
        assertFalse(filter.isIncluded("module-info.class"));
        assertFalse(filter.isIncluded("/META-INF/versions/9/module-info.class"));
    }

    @Test
    void isIncludedMetaInfo() {
        assertFalse(filter.isIncluded("/META-INF/versions/9/org/bouncycastle/util/Strings.class"));
    }

}