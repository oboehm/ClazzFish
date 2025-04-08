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

import clazzfish.spi.git.test.GitServer;
import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import patterntesting.runtime.junit.NetworkTester;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Unit tests for {@link Repo}.
 *
 * @author oboehm
 * @since 21.03.25
 */
class RepoTest {

    private static final Logger log = LoggerFactory.getLogger(RepoTest.class);
    private static final URI TEST_URI = URI.create("http://localhost:8080/TestRepo");
    private static Server SERVER;

    @BeforeAll
    static void startGitServer() throws Exception {
        SERVER = GitServer.startServer();
        log.info("{} was started.", SERVER);
    }

    @Test
    void ofHttps() throws GitAPIException, IOException {
        URI uri = URI.create("https://github.com/oboehm/ClazzFish.git");
        prepareRepo(uri);
        try (Repo repo = Repo.of(uri)) {
            assertNotNull(repo);
            assertTrue(repo.getDir().isDirectory());
        }
    }

    @Test
    void ofSsh() throws GitAPIException, IOException {
        assumeTrue(Files.exists(Paths.get(System.getProperty("user.home"), ".ssh/id_rsa")),
                "no private ssh key available");
        URI uri = URI.create("ssh://git@github.com/oboehm/ClazzFish.git");
        prepareRepo(uri);
        try (Repo repo = Repo.of(uri)) {
            assertNotNull(repo);
            assertTrue(repo.getDir().isDirectory());
        }
    }

    private static void prepareRepo(URI uri) throws IOException {
        deleteRepoPath(uri);
        try {
            URL url = uri.toURL();
            assumeTrue(NetworkTester.exists(url), url + " is offline");
        } catch (MalformedURLException ex) {
            if (uri.getScheme().equals("ssh")) {
                log.warn("URI {} is not a known URI ({}).", uri, ex.getMessage());
                log.debug("Details: ", ex);
                assumeTrue(NetworkTester.isOnline(uri.getHost(), 22), uri + " is offline");
            } else {
                throw ex;
            }
        }
    }

    private static void deleteRepoPath(URI uri) throws IOException {
        Path repoPath = Repo.getRepoPathOf(uri);
        if (Files.exists(repoPath)) {
            FileUtils.deleteDirectory(repoPath.toFile());
            log.info("'{}' wurde entfernt.", repoPath);
        }
    }

    @Test
    void pull() throws GitAPIException, IOException {
        try (Repo repo = Repo.of(TEST_URI)) {
            assertNotNull(repo);
        }
        try (Repo repo = Repo.of(TEST_URI)) {
            assertNotNull(repo);
        }
    }

    @Test
    void ofTestRepo() throws GitAPIException, IOException {
        String testRepoProp = System.getProperty("test.repo.uri");
        assumeFalse(testRepoProp == null, "system property 'test.repo.uri' not set");
        URI uri = URI.create(testRepoProp);
        assumeTrue(NetworkTester.isOnline(uri.getHost(), uri.getPort()), uri + " is offline");
        try (Repo repo = Repo.of(uri)) {
            assertNotNull(repo);
        }
    }

    @Test
    void getStatus() throws GitAPIException, IOException {
        URI TEST_URI = URI.create("http://localhost:8080/TestRepo");
        try (Repo repo = Repo.of(TEST_URI)) {
            Status status = repo.getStatus();
            assertNotNull(status);
            assertTrue(status.isClean());
        }
    }

    @AfterAll
    static void stopServer() throws Exception {
        SERVER.stop();
        log.info("{} was stopped.", SERVER);
    }

}
