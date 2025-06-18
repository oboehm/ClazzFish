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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * The class SshConfig ...
 *
 * @author oboehm
 * @since 2.7 (18.06.25)
 */
public final class SshConfig {

    private static final Logger log = LoggerFactory.getLogger(SshConfig.class);
    private final Config config;

    public SshConfig(Config config) {
        this.config = config;
    }

    public File getPrivateKeyFile() {
        String propname = "clazzfish.git.ssh.keyfile";
        String filename = config.getProperty(propname);
        if (filename == null) {
            File defaultFile = new File(System.getProperty("user.home"), ".ssh/id_rsa");
            log.debug("Using '{}' for SSH key because property '{}' is not set.", defaultFile, propname);
            return defaultFile;
        } else {
            log.debug("Using '{}' for SSH key.", filename);
            return new File(filename);
        }
    }

}
