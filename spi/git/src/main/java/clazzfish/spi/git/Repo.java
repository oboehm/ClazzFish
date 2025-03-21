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

import org.apache.commons.lang3.SystemUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
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
public class Repo implements AutoCloseable{

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
        Git git = cloneRepo(gitURI);
        return new Repo(gitURI, git);
    }

    private static Git cloneRepo(URI gitURI) throws IOException, GitAPIException {
        Path repoDir = Paths.get(SystemUtils.getJavaIoTmpDir().toString(), "ClazzFishRepo", gitURI.getPath());
        repoDir = Files.createDirectories(repoDir);
        Git git = Git.cloneRepository()
                .setURI(gitURI.toString())
                .setDirectory(repoDir.toFile())
                .call();
        log.debug("{} is cloned to dir '{}'.", gitURI, repoDir);
        return git;
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
