/*
 * Copyright (c) 2018 by Oliver Boehm
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * (c)reated 09.07.2018 by oboehm (ob@oasd.de)
 */
package clazzfish.agent;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import org.apache.maven.shared.invoker.*;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * The class ClasspathAgentLoader loads dynamically the javaagent at runtime.
 * It was inspired from <a href=
 * "https://web.archive.org/web/20141014195801/http://dhruba.name/2010/02/07/creation-dynamic-loading-and-instrumentation-with-javaagents/"
 * >creation-dynamic-loading-and-instrumentation</a>.
 *
 * @since 0.8 (09.07.2018)
 */
public class ClasspathAgentLoader {

    private static final Logger LOG = Logger.getLogger(ClasspathAgentLoader.class.getName());

    /**
     * The pid of the running process is queried and the javaagent is attacted
     * to the running process.
     *
     * @param jarFile the jar file
     */
    public static void loadAgent(Path jarFile) {
        LOG.info("Loading javaagent dynamically from '" +  jarFile + "'...");
        String nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();
        int p = nameOfRunningVM.indexOf('@');
        String pid = nameOfRunningVM.substring(0, p);
        try {
            VirtualMachine vm = VirtualMachine.attach(pid);
            vm.loadAgent(jarFile.toString(), "");
            vm.detach();
            LOG.info("Loading javaagent dynamically from '" + jarFile + "' finished and attached to pid " + pid + ".");
        } catch (IOException | AgentInitializationException | AgentLoadException | AttachNotSupportedException ex) {
            throw new IllegalStateException("cannot load agent from " + jarFile, ex);
        }
    }

    /**
     * Creates the project's jar file using 'maven'.
     */
    public static void createJar() {
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(new File("pom.xml"));
        request.setGoals(Collections.singletonList("package"));
        request.setProperties(createProperties("maven.test.skip", "true"));
        Invoker invoker = new DefaultInvoker();
        try {
            LOG.info("Creating jar using " + invoker + "...");
            invoker.execute(request);
        } catch (MavenInvocationException e) {
            throw new IllegalStateException("cannot create jar with " + request);
        }
    }

    private static Properties createProperties(String key, String value) {
        Properties props = new Properties();
        props.setProperty(key, value);
        return props;
    }

}
