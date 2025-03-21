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

import com.github.sparsick.testcontainers.gitserver.plain.GitServerContainer;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.utility.DockerImageName;

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
    private static GitServerContainer gitServer;

    @BeforeAll
    static void startGitServer() {
        gitServer =
                new GitServerContainer(DockerImageName.parse("rockstorm/git-server:2.47"))
                        .withGitRepo("testRepo")
                        .withGitPassword("topsecret");
        gitServer.start();
    }

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
    void pull() throws GitAPIException, IOException {
        URI uri = URI.create("https://github.com/oboehm/ClazzFish.git");
        try (Repo repo = Repo.of(uri)) {
            assertNotNull(repo);
        }
        try (Repo repo = Repo.of(uri)) {
            assertNotNull(repo);
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

    @AfterAll
    static void stopGitServer() {
        gitServer.stop();
    }

}
