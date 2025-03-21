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

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import org.apache.commons.lang3.SystemUtils;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.ssh.jsch.JschConfigSessionFactory;
import org.eclipse.jgit.util.FS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    public static Repo of(URI gitURI) throws IOException, GitAPIException {
        if (gitURI.getScheme().equalsIgnoreCase("file")) {
            throw new UnsupportedOperationException(gitURI + ": file protocol is not supported");
        }
        Git git = getRepo(gitURI);
        return new Repo(gitURI, git);
    }

    private static Git getRepo(URI gitURI) throws IOException, GitAPIException {
        Path repoDir = getRepoPathOf(gitURI);
        if (Files.exists(repoDir) && Files.list(repoDir).findAny().isPresent()) {
            return pullRepo(repoDir);
        } else {
            return cloneRepo(gitURI, repoDir);
        }
    }

    public static Path getRepoPathOf(URI gitURI) {
        return Paths.get(SystemUtils.getJavaIoTmpDir().toString(), "ClazzFishRepo", gitURI.getPath());
    }

    private static Git pullRepo(Path repoDir) throws IOException, GitAPIException {
        Git git = Git.open(repoDir.toFile());
        PullResult result = git.pull().call();
        log.debug("Pull request into '{}' results in {}.", repoDir, result);
        return git;
    }

    private static Git cloneRepo(URI gitURI, Path repoDir) throws IOException, GitAPIException {
        File dir = Files.createDirectories(repoDir).toFile();
        CloneCommand cmd = Git.cloneRepository()
                .setURI(gitURI.toString())
                .setDirectory(dir);
        if (gitURI.getScheme().equalsIgnoreCase("ssh")) {
            setSshCredentials(cmd);
        }
        Git git = cmd.call();
        log.debug("{} is cloned to dir '{}'.", gitURI, repoDir);
        return git;
    }

    private static void setSshCredentials(CloneCommand cmd) {
        SshSessionFactory sshSessionFactory = getSshSessionFactory();
        cmd.setTransportConfigCallback(transport -> {
            SshTransport sshTransport = (SshTransport) transport;
            sshTransport.setSshSessionFactory(sshSessionFactory);
        });
    }

    private static SshSessionFactory getSshSessionFactory() {
        Path privateKeyFile = Paths.get(System.getProperty("user.home"), ".ssh/id_rsa");
        if (Files.exists(privateKeyFile)) {
            SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
                @Override
                protected JSch createDefaultJSch( FS fs ) throws JSchException {
                    JSch defaultJSch = super.createDefaultJSch( fs );
                    // if you'll get "invalid privatekey" transfrom your file into PEM format
                    defaultJSch.addIdentity(privateKeyFile.toString());
                    return defaultJSch;
                }
            };
            log.debug("Using private key file '{}' as ssh identity.", privateKeyFile);
            return sshSessionFactory;
        } else {
            log.warn("No private key file {} found, using default ssh identity.", privateKeyFile);
            return new JschConfigSessionFactory();
        }
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

}
