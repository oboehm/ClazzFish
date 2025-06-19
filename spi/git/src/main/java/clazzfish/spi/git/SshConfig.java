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

/**
 * The class SshConfig ...
 *
 * @author oboehm
 * @since 2.7 (18.06.25)
 */
public final class SshConfig {

    private static final Logger log = LoggerFactory.getLogger(SshConfig.class);
    private final Config config;

    public SshConfig() {
        this(Config.DEFAULT);
    }

    public SshConfig(Config config) {
        this.config = config;
        setUpSshSessionFactory(config);
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

    private static void setUpSshSessionFactory(Config cfg) {
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
                defaultJSch.addIdentity(keyFile.getAbsolutePath());
                return defaultJSch;
            }
        };
        SshSessionFactory.setInstance(sshSessionFactory);
        log.debug("SSH sessions are set up with non-strict host checking and '{}' as private key.", keyFile);
    }

    public SshSessionFactory getSshSessionFactory() {
        File privateKeyFile = getPrivateKeyFile();
        if (privateKeyFile.exists()) {
            SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
                @Override
                protected JSch createDefaultJSch( FS fs ) throws JSchException {
                    JSch defaultJSch = super.createDefaultJSch(fs);
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

}
