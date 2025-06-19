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
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.ssh.jsch.JschConfigSessionFactory;
import org.eclipse.jgit.transport.ssh.jsch.OpenSshConfig;
import org.eclipse.jgit.util.FS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * The class SshConfig ...
 *
 * @author oboehm
 * @since 2.7 (18.06.25)
 */
public final class SshConfig {

    private static final Logger log = LoggerFactory.getLogger(SshConfig.class);
    private static final Map<Config, SshConfig> CACHE = new HashMap<>();
    private final Config config;
    private final SshSessionFactory sshSessionFactory;

    public static final SshConfig DEFAULT = SshConfig.of(Config.DEFAULT);

    public SshConfig(Config config) {
        this.config = config;
        sshSessionFactory = createSshSessionFactory(config);
    }

    public static SshConfig of(Config cfg) {
        SshConfig sshCfg = CACHE.get(cfg);
        if (sshCfg == null) {
            sshCfg = new SshConfig(cfg);
            CACHE.put(cfg, sshCfg);
        }
        return sshCfg;
    }

    private static File getPrivateKeyFile(Config cfg) {
        String propname = "clazzfish.git.ssh.keyfile";
        String filename = cfg.getProperty(propname);
        if (filename == null) {
            File defaultFile = new File(System.getProperty("user.home"), ".ssh/id_rsa");
            log.debug("Using '{}' for SSH key because property '{}' is not set.", defaultFile, propname);
            return defaultFile;
        } else {
            log.debug("Using '{}' for SSH key.", filename);
            return new File(filename);
        }
    }

    public File getPrivateKeyFile() {
        return getPrivateKeyFile(config);
    }

    private static SshSessionFactory createSshSessionFactory(Config cfg) {
        File keyFile = getPrivateKeyFile(cfg);
        SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
            @Override
            protected void configure(OpenSshConfig.Host host, Session session) {
                session.setConfig("StrictHostKeyChecking", "no");
                log.debug("Strict host checking of {} is disabled for {}.", host, session);
            }
            @Override
            protected JSch createDefaultJSch(FS fs) throws JSchException {
                JSch defaultJSch = super.createDefaultJSch(fs);
                // if you'll get "invalid privatekey" transfrom your file into PEM format
                defaultJSch.addIdentity(keyFile.getAbsolutePath());
                return defaultJSch;
            }
        };
        SshSessionFactory.setInstance(sshSessionFactory);
        log.debug("SSH sessions are set up with non-strict host checking and '{}' as private key.", keyFile);
        return sshSessionFactory;
    }

    public SshSessionFactory getSshSessionFactory() {
        return sshSessionFactory;
    }

    @Override
    public String toString() {
        return "SSH-" + config;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SshConfig)) return false;
        SshConfig sshConfig = (SshConfig) o;
        return Objects.equals(config, sshConfig.config);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(config);
    }

}
