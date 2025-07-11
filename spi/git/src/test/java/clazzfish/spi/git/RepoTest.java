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
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.PushResult;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import patterntesting.runtime.junit.NetworkTester;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
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
    private static final SshConfig sshConfig = SshConfig.DEFAULT;
    private static GitServer SERVER;

    @BeforeAll
    static void startGitServer() throws Exception {
        SERVER = new GitServer(8080);
        SERVER.start();
        log.info("{} was started.", SERVER);
    }

    @Test
    void ofHttps() throws GitAPIException, IOException {
        URI uri = URI.create("https://github.com/oboehm/ClazzFish.git");
        prepareRepo(uri);
        try (Repo repo = Repo.of(uri, sshConfig)) {
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
        try (Repo repo = Repo.of(uri,  sshConfig)) {
            assertNotNull(repo);
            assertTrue(repo.getDir().isDirectory());
        }
    }

    @Test
    void ofSshWithContextPath() throws GitAPIException, IOException {
        assumeTrue(Files.exists(Paths.get(System.getProperty("user.home"), ".ssh/id_rsa")),
                "no private ssh key available");
        URI uri = URI.create("ssh://git@github.com/oboehm/ClazzFishTest.git/ClazzStatistic.csv");
        try (Repo repo = Repo.of(uri, sshConfig)) {
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

    @Test
    void pull() throws GitAPIException, IOException {
        try (Repo repo = Repo.of(TEST_URI, sshConfig)) {
            assertNotNull(repo);
        }
        try (Repo repo = Repo.of(TEST_URI, sshConfig)) {
            assertNotNull(repo);
        }
    }

    /**
     * This test is more an integration test. If you want to test a special
     * GIT repos call e.g.
     * <p>
     *     java -Dtest.repo.uri=ssh://git@bitbucket.example.com:7999/demo/dead-classes.git/ClazzStatistic.csv
     *     -Dclazzfish.git.ssh.keyfile=...
     * </p>
     * @throws GitAPIException in case of GIT problems
     * @throws IOException.    in case of I/O problems
     */
    @Test
    void ofTestRepo() throws GitAPIException, IOException {
        String testRepoProp = System.getProperty("test.repo.uri");
        assumeFalse(testRepoProp == null, "system property 'test.repo.uri' not set");
        URI uri = URI.create(testRepoProp);
        assumeTrue(NetworkTester.isOnline(uri.getHost(), uri.getPort()), uri + " is offline");
        try (Repo repo = Repo.of(uri, sshConfig)) {
            assertNotNull(repo);
        }
    }

    @Test
    void getStatus() throws GitAPIException, IOException {
        deleteRepoPath(TEST_URI);
        try (Repo repo = Repo.of(TEST_URI, sshConfig)) {
            Status status = repo.getStatus();
            assertNotNull(status);
            assertTrue(status.isClean());
        }
    }

    public static void deleteRepoPath(URI uri) throws IOException {
        Path repoPath = Repo.getRepoPathOf(uri);
        if (Files.exists(repoPath)) {
            FileUtils.deleteDirectory(repoPath.toFile());
            log.info("'{}' wurde entfernt.", repoPath);
        }
    }

    @Test
    void add() throws GitAPIException, IOException {
        try (Repo repo = Repo.of(TEST_URI, sshConfig)) {
            addTo(repo, "hello.world");
            assertFalse(repo.getStatus().isClean());
        }
    }

    private static void addTo(Repo repo, String filename) throws IOException, GitAPIException {
        Path file = Paths.get(repo.getDir().getAbsolutePath(), filename);
        Files.createFile(file);
        repo.add(file.toFile());
    }

    @Test
    void commit() throws GitAPIException, IOException {
        try (Repo repo = Repo.of(TEST_URI, sshConfig)) {
            addTo(repo, "crash.com");
            repo.commit("bumm");
            assertTrue(repo.getStatus().isClean());
        }
    }

    @Test
    void push() throws GitAPIException, IOException {
        try (Repo repo = Repo.of(TEST_URI, sshConfig)) {
            addTo(repo, "push.it");
            repo.commit("go");
            Iterable<PushResult> results = repo.push();
            assertNotNull(results);
        }
        assertFilePushed(TEST_URI, "push.it");
    }

    private void assertFilePushed(URI testUri, String filename) throws IOException, GitAPIException {
        deleteRepoPath(testUri);
        try (Repo repo = Repo.of(testUri, sshConfig)) {
            File pushed = new File(repo.getDir(), filename);
            assertTrue(pushed.isFile());
        }
    }

    @AfterAll
    static void stopServer() throws Exception {
        SERVER.stop();
        log.info("{} was stopped.", SERVER);
        deleteRepoPath(TEST_URI);
    }

}
