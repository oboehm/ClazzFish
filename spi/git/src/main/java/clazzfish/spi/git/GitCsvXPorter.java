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

    @Override
    public List<String> importCSV(URI uri) throws IOException {
        try (Repo repo = Repo.of(uri)) {
            return importCSV(new File(repo.getDir(), "ClazzStatistic.csv"));
        } catch (GitAPIException ex) {
            log.info("Cannot import ClazzStatistic.csv from {} ({}).", uri, ex.getMessage());
            log.debug("Details: ", ex);
            return Collections.emptyList();
        }
    }

    private List<String> importCSV(File file) throws IOException {
        FileXPorter fileXPorter = new FileXPorter();
        return fileXPorter.importCSV(file.toURI());
    }

    @Override
    public void exportCSV(URI uri, String csvHeadLine, List<String> csvLines) throws IOException {
        try (Repo repo = Repo.of(uri)) {
            writeCSV(repo, csvHeadLine, csvLines);
            log.debug("Statistic exported with {} lines to '{}'.", csvLines.size(), uri);
        } catch (GitAPIException ex) {
            log.info("Cannot export statistic to {} ({}).", uri, ex.getMessage());
            log.debug("Details: ", ex);
        }
    }

    private void writeCSV(Repo repo, String csvHeadLine, List<String> csvLines) throws IOException, GitAPIException {
        File outputFile = new File(repo.getDir(), "ClazzStatistic.csv");
        if (!outputFile.exists()) {
            if (!outputFile.createNewFile()) {
                throw new IOException("cannot create file " + outputFile.getAbsolutePath());
            }
        }
        csvHeadLine = StringUtils.substringAfter(csvHeadLine, ";");
        List<String> lines = csvLines.stream()
                .map(l -> StringUtils.substringAfter(l, ";"))
                .collect(Collectors.toList());
        FileXPorter fileXPorter = new FileXPorter();
        fileXPorter.exportCSV(outputFile.toURI(), csvHeadLine, lines);
        repo.add(outputFile);
        repo.commit(csvHeadLine + " - " + lines.size() + " lines");
        repo.push();
    }

}
