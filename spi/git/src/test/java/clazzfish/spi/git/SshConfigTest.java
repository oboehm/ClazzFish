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
 * (c)reated 18.06.25 by oboehm
 */
package clazzfish.spi.git;

import clazzfish.monitor.Config;
import org.junit.jupiter.api.Test;
import patterntesting.runtime.junit.ObjectTester;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link SshConfig}.
 *
 * @author oboehm
 * @since 18.06.25
 */
class SshConfigTest {

    @Test
    void getPrivateKeyFile() {
        Config config = Config.of("config/clazzfish-test.properties");
        SshConfig sshConfig = SshConfig.of(config);
        File keyFile = sshConfig.getPrivateKeyFile();
        assertTrue(keyFile.exists());
        assertEquals("id_ed25519", keyFile.getName());
    }

    @Test
    void testEquals() {
        ObjectTester.assertEquals(SshConfig.DEFAULT, SshConfig.DEFAULT);
        ObjectTester.assertEquals(SshConfig.of(Config.of("config/clazzfish-test.properties")),
                SshConfig.of(Config.of("config/clazzfish-test.properties")));
    }

}