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

import clazzfish.monitor.spi.CsvXPorter;
import clazzfish.monitor.spi.FileXPorter;
import clazzfish.monitor.stat.ClazzRecord;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Die Klasse GitCsvXPorter ...
 *
 * @author oboehm
 * @since 2.6 (15.03.25)
 */
public class GitCsvXPorter implements CsvXPorter {

    private static final Logger log = LoggerFactory.getLogger(GitCsvXPorter.class);
    private final SshConfig sshConfig;

    public GitCsvXPorter() {
        this(SshConfig.DEFAULT);
    }

    public GitCsvXPorter(SshConfig sshConfig) {
        this.sshConfig = sshConfig;
    }

    @Override
    public List<String> importCSV(URI uri) throws IOException {
        if (uri.getScheme().equalsIgnoreCase("file")) {
            return importCSV(new File(uri));
        }
        try (Repo repo = Repo.of(uri, sshConfig)) {
            return importCSV(new File(repo.getDir(), "ClazzStatistic.csv"));
        } catch (GitAPIException ex) {
            log.info("Cannot import ClazzStatistic.csv from {} ({}).", uri, ex.getMessage());
            log.debug("Details: ", ex);
            return Collections.emptyList();
        }
    }

    private List<String> importCSV(File file) throws IOException {
        log.debug("Importing file {}...", file);
        FileXPorter fileXPorter = new FileXPorter();
        List<String> lines = fileXPorter.importCSV(file.toURI());
        return lines.stream().map(s -> StringUtils.substringAfter(s, ";")).collect(Collectors.toList());
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
        List<ClazzRecord> clazzRecords = csvLines.stream()
                .map(ClazzRecord::fromCSV)
                .collect(Collectors.toList());
        writeCSV(repo, clazzRecords);
        log.trace("{} lines with headline '{}'", csvLines.size(), csvHeadLine);
    }

    private void writeCSV(Repo repo, List<ClazzRecord> clazzRecords) throws IOException, GitAPIException {
        File outputFile = new File(repo.getDir(), "ClazzStatistic.csv");
        if (!outputFile.exists()) {
            if (!outputFile.createNewFile()) {
                throw new IOException("cannot create file " + outputFile.getAbsolutePath());
            }
        }
        FileXPorter fileXPorter = new FileXPorter();
        List<String> csvLines = clazzRecords.stream()
                .map(cr -> cr.classname() + ";" + (cr.count() > 0 ? "1" : "0"))
                .collect(Collectors.toList());
        fileXPorter.exportCSV(outputFile.toURI(), "Classname;Count", csvLines);
        repo.add(outputFile);
        String statistic = getStatistic(clazzRecords);
        repo.commit(statistic);
        repo.push();
    }

    private String getStatistic(List<ClazzRecord> clazzRecords) {
        long lc = clazzRecords.stream().filter(cr -> cr.count() > 0).count();
        long ac = clazzRecords.size();
        long dc = ac - lc;
        return String.format("%d classes: %d loaded (%d%%), %d dead (%d%%)", ac,
                lc, (lc * 100 + ac/2) / ac, dc, (dc * 100 + dc/2) / ac);
    }

}
