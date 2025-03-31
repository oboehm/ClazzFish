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
 * (c)reated 15.03.25 by oboehm
 */
package clazzfish.spi.git;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link GitCsvXPorter}.
 *
 * @author oboehm
 * @since 15.03.25
 */
class GitCsvXPorterTest {

    private final GitCsvXPorter xPorter = new GitCsvXPorter();

    @Test
    void importCSVnotExists() throws IOException {
        URI gitURI = URI.create("ssh://git@github.com/oboehm/ClazzFish.git");
        List<String> csvLines = xPorter.importCSV(gitURI);
        assertNotNull(csvLines);
        assertTrue(csvLines.isEmpty(), "ClazzStatistics.csv should not exist direct under " + gitURI);
    }

}
