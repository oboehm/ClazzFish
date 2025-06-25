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

import clazzfish.monitor.Config;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import patterntesting.runtime.junit.CollectionTester;
import patterntesting.runtime.junit.NetworkTester;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Unit tests for {@link GitCsvXPorter}.
 *
 * @author oboehm
 * @since 15.03.25
 */
class GitCsvXPorterTest {

    private static final Logger log = LoggerFactory.getLogger(GitCsvXPorterTest.class);
    private static final File privateKeyFile = new File(System.getProperty("user.home"), ".ssh/id_rsa");
    private static GitCsvXPorter xPorter;

    @BeforeAll
    static void setUpXPorter() {
        Properties props = new Properties();
        props.setProperty("clazzfish.git.ssh.keyfile", privateKeyFile.getAbsolutePath());
        SshConfig sshConfig = SshConfig.of(Config.of(props));
        xPorter = new GitCsvXPorter(sshConfig);
    }

    @Test
    void exportCSV() throws IOException {
        // Given
        assumeTrue(privateKeyFile.exists(), "no SSH key file");
        URI gitURI = URI.create("ssh://git@github.com/oboehm/ClazzFishTest.git");
        assumeTrue(NetworkTester.isOnline(gitURI), gitURI + " is not online");
        String header = "Classpath;Classname;Count";
        List<String> lines = new ArrayList<>();
        lines.add(String.format("%s;%s;%d", new File("target", "classes").toURI(), getClass().getName(), 2));

        // When
        xPorter.exportCSV(gitURI, header, lines);

        // Then
        List<String> imported = xPorter.importCSV(gitURI);
        assertEquals("Classname;Count", imported.get(0));
        assertEquals(getClass().getName() + ";1", imported.get(1));
        RepoTest.deleteRepoPath(gitURI);
        CollectionTester.assertEquals(imported, xPorter.importCSV(gitURI));
    }

    @Test
    void importCSVnotExists() {
        URI gitURI = URI.create("ssh://git@github.com/oboehm/ClazzFish.git");
        try {
            List<String> csvLines = xPorter.importCSV(gitURI);
            assertNotNull(csvLines);
            assertTrue(csvLines.isEmpty(), "ClazzStatistics.csv should not exist direct under " + gitURI);
        } catch (IOException canhappen) {
            log.warn("Cannot import CSV from {}:", gitURI, canhappen);
        }
    }

    @Test
    void importCsvFile() throws IOException {
        File csvFile = new File("src/test/resources/clazzfish/spi/git/test.csv");
        assertTrue(csvFile.exists());
        List<String> csvLines = xPorter.importCSV(csvFile.toURI());
        assertNotNull(csvLines);
        assertFalse(csvLines.isEmpty());
    }

}
