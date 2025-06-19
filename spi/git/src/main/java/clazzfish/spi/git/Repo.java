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
 * (c)reated 19.03.25 by oboehm
 */
package clazzfish.spi.git;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * The class Repo is responsible for the access to a GIT repository.
 *
 * @author oboehm
 * @since 2.6 (19.03.25)
 */
public class Repo implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(Repo.class);
    private final URI uri;
    private final Git git;

    private Repo(URI uri, Git git) {
        this.uri = uri;
        this.git = git;
    }

    public static Repo of(URI gitURI, SshConfig sshCfg) throws IOException, GitAPIException {
        if (gitURI.getScheme().equalsIgnoreCase("file")) {
            throw new UnsupportedOperationException(gitURI + ": file protocol is not supported");
        }
        URI baseURI = getBaseURI(gitURI);
        Git git = getRepo(baseURI, sshCfg);
        return new Repo(baseURI, git);
    }

    private static URI getBaseURI(URI gitURI) {
        String uri = gitURI.toString();
        if (uri.contains(".git/")) {
            return URI.create(StringUtils.substringBefore(uri, ".git/") + ".git");
        } else if (uri.endsWith("/ClazzStatistic.csv")) {
            return URI.create(StringUtils.substringBefore(uri, "/ClazzStatistic.csv") + ".csv");
        }
        return gitURI;
    }

    private static Git getRepo(URI gitURI, SshConfig sshCfg) throws IOException, GitAPIException {
        final Path repoDir = getRepoPathOf(gitURI);
        if (Files.exists(repoDir)) {
            try (Stream<Path> list = Files.list(repoDir)) {
                if (list.findAny().isPresent()) {
                    return pullRepo(repoDir);
                }
            }
        }
        return cloneRepo(gitURI, repoDir, sshCfg);
    }

    public static Path getRepoPathOf(URI gitURI) {
        return Paths.get(SystemUtils.getJavaIoTmpDir().toString(), "ClazzFishRepo", gitURI.getPath());
    }

    private static Git pullRepo(Path repoDir) throws IOException, GitAPIException {
        Git git = Git.open(repoDir.toFile());
        PullCommand pull = git.pull();
        PullResult result = pull.call();
        log.debug("Pull request into '{}' results in {}.", repoDir, result);
        return git;
    }

    private static Git cloneRepo(URI gitURI, Path repoDir, SshConfig sshCfg) throws IOException, GitAPIException {
        File dir = Files.createDirectories(repoDir).toFile();
        CloneCommand cmd = Git.cloneRepository()
                .setURI(gitURI.toString())
                .setDirectory(dir);
        if (gitURI.getScheme().equalsIgnoreCase("ssh")) {
            setSshCredentials(cmd, sshCfg);
        }
        Git git = cmd.call();
        log.debug("{} is cloned to dir '{}'.", gitURI, repoDir);
        return git;
    }

    private static void setSshCredentials(CloneCommand cmd, SshConfig sshCfg) {
        SshSessionFactory sshSessionFactory = sshCfg.getSshSessionFactory();
        cmd.setTransportConfigCallback(transport -> {
            SshTransport sshTransport = (SshTransport) transport;
            sshTransport.setSshSessionFactory(sshSessionFactory);
        });
    }

    public File getDir() {
        return git.getRepository().getWorkTree();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " " + uri;
    }

    @Override
    public void close() {
        git.close();
        log.debug("{} is closed.", git);
    }

    public Status getStatus() throws GitAPIException {
        return git.status().call();
    }

    public void add(File... files) throws GitAPIException {
        Repository repository = git.getRepository();
        AddCommand command = new AddCommand(repository);
        for (File f : files) {
            String filename = asFilename(f);
            command.addFilepattern(filename);
            log.debug("File '{}' is added to {}.", filename, repository);
        }
        command.call();
    }

    private String asFilename(File file) {
        String filename = StringUtils.substringAfter(file.getAbsolutePath(), getDir().getAbsolutePath());
        filename = FilenameUtils.separatorsToUnix(filename);
        if (filename.startsWith("/")) {
            filename = filename.substring(1);
        } else if (filename.isEmpty()) {
            filename = file.toString();
        }
        return filename;
    }

    public void commit(String message) throws GitAPIException {
        git.commit().setMessage(message).call();
        log.debug("Changes were commited with '{}'.", message);
    }

    public Iterable<PushResult> push() throws GitAPIException {
        Iterable<PushResult> results = git.push().call();
        log.debug("Changes are pushed to {}.", uri);
        return results;
    }

}
