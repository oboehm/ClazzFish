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
 * (c)reated 21.03.25 by oboehm
 */
package clazzfish.spi.git;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link Repo}.
 *
 * @author oboehm
 * @since 21.03.25
 */
class RepoTest {

    private static final Logger log = LoggerFactory.getLogger(RepoTest.class);

    @Test
    void ofHttps() throws GitAPIException, IOException {
        URI uri = URI.create("https://github.com/oboehm/ClazzFish.git");
        try (Repo repo = Repo.of(uri)) {
            assertNotNull(repo);
            assertTrue(repo.getDir().isDirectory());
            FileUtils.deleteDirectory(repo.getDir());
            log.info("'{}' wurde wieder entfernt.", repo.getDir());
        }
    }

    @Test
    @Disabled("not yet supported")
    void ofSsh() throws GitAPIException, IOException {
        URI uri = URI.create("ssh://git@github.com/oboehm/ClazzFish.git");
        try (Repo repo = Repo.of(uri)) {
            assertNotNull(repo);
        }
    }

}
