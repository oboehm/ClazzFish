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

import clazzfish.core.spi.CsvXPorter;
import clazzfish.core.spi.FileXPorter;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Die Klasse GitCsvXPorter ...
 *
 * @author oboehm
 * @since 2.6 (15.03.25)
 */
public class GitCsvXPorter implements CsvXPorter {

    private static final Logger log = LoggerFactory.getLogger(GitCsvXPorter.class);
    private final SshConfig sshConfig;
    private final URI uri;

    public GitCsvXPorter(URI uri) {
        this(SshConfig.DEFAULT, uri);
    }

    public GitCsvXPorter(SshConfig sshConfig, URI uri) {
        this.sshConfig = sshConfig;
        this.uri = uri;
    }

    @Override
    public URI getURI() {
        return uri;
    }

    @Override
    public GitCsvXPorter withURI(URI csvURI) {
        if (csvURI.equals(getURI())) {
            return this;
        } else {
            log.trace("A new GitCsvXPorter for URI {} with {} will be created.", csvURI, sshConfig);
            return new GitCsvXPorter(sshConfig, csvURI);
        }
    }

    @Override
    public List<String> importCSV(URI uri) throws IOException {
        if (uri.getScheme().equalsIgnoreCase("file")) {
            return importCSV(new File(uri));
        }
        Path context = Repo.getContextPath(uri);
        try (Repo repo = Repo.of(uri, sshConfig)) {
            return importCSV(new File(repo.getDir(), context.toString()));
        } catch (GitAPIException ex) {
            log.info("Cannot import {} from {} ({}).", context, uri, ex.getMessage());
            log.debug("Details: ", ex);
            return Collections.emptyList();
        }
    }

    private List<String> importCSV(File file) throws IOException {
        log.debug("Importing file {}...", file);
        FileXPorter fileXPorter = new FileXPorter(file.toURI());
        return fileXPorter.importCSV();
    }

    @Override
    public void exportCSV(URI uri, String csvHeadLine, List<String> csvLines) throws IOException {
        try (Repo repo = Repo.of(uri, sshConfig)) {
            writeCSV(repo, csvHeadLine, csvLines);
            log.debug("Statistic exported with {} lines to '{}'.", csvLines.size(), uri);
        } catch (GitAPIException ex) {
            log.info("Cannot export statistic to {} ({}).", uri, ex.getMessage());
            log.debug("Details: ", ex);
        }
    }

    private void writeCSV(Repo repo, String csvHeadLine, List<String> csvLines) throws IOException, GitAPIException {
        File outputFile = new File(repo.getDir(), Repo.getContextPath(uri).toString());
        if (!outputFile.exists()) {
            if (!outputFile.createNewFile()) {
                throw new IOException("cannot create file " + outputFile.getAbsolutePath());
            }
        }
        FileXPorter fileXPorter = new FileXPorter(outputFile.toURI());
        fileXPorter.exportCSV(csvHeadLine, csvLines);
        repo.add(outputFile);
        String comment = String.format("statistic with %d entries", csvLines.size());
        repo.commit(comment);
        repo.push();
        log.trace("{} lines with headline '{}'", csvLines.size(), csvHeadLine);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "-" + uri;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof GitCsvXPorter)) return false;
        GitCsvXPorter that = (GitCsvXPorter) o;
        return Objects.equals(uri, that.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uri);
    }

}
