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
 * (c)reated 17.11.25 by oboehm
 */
package clazzfish.monitor.jmx;

import clazzfish.core.jmx.MBeanFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.Attribute;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.util.Arrays;

/**
 * The class AgentFinder was introduced to simplify the communication with
 * the ClasspathAgent.
 *
 * @author oboehm
 * @since 3.0 (17.11.25)
 */
public final class AgentFinder {

    private static final Logger log = LoggerFactory.getLogger(AgentFinder.class);
    private static final String[] AGENT_MBEAN_NAMES = new String[]{
            "clazzfish:type=agent,agent=ClasspathAgent",
            "clazzfish.agent:type=ClasspathAgent"
    };
    private static final MBeanServer MBEAN_SERVER = ManagementFactory.getPlatformMBeanServer();

    private final ObjectInstance agentMBean;

    public AgentFinder() {
        this(MBeanFinder.findMBean(AGENT_MBEAN_NAMES));
        if ((agentMBean == null)) {
            log.debug("No MBean \"{}\" is found.", Arrays.toString(AGENT_MBEAN_NAMES));
        }
    }

    public AgentFinder(ObjectInstance agentMBean) {
        this.agentMBean = agentMBean;
    }

    /**
     * Checks if the ClasspathAgent is available as MBean. The ClasspathAgent is
     * needed for classloaders which are not directly supported (e.g. IBM's
     * classloader of their JDK).
     *
     * @return true, if is agent available
     */
    public boolean isAgentAvailable() {
        return agentMBean != null;
    }

    public boolean isDumping() {
        try {
            return getAttribute("Dumping", Boolean.class);
        } catch (JMException ex) {
            log.info("Agent is probably not dumping a statistic ({}).",  ex.getMessage());
            log.debug("Details:", ex);
            return false;
        }
    }

    public URI getDumpURI() throws JMException {
        return getAttribute("DumpURI", URI.class);
    }

    public void setDumpURI(URI uri) {
        Attribute attribute = new Attribute("DumpURI", uri);
        try {
            MBEAN_SERVER.setAttribute(agentMBean.getObjectName(), attribute);
        } catch (JMException ex) {
            log.warn("Could not set {} to {} ({}).", attribute, agentMBean, ex.getMessage());
            log.debug("Details:", ex);
        }
    }

    public <T> T getAttribute(String name, Class<T> type) throws JMException {
        log.trace("Getting attribute '{}' of {}.", name, type);
        return (T) MBEAN_SERVER.getAttribute(agentMBean.getObjectName(), name);
    }

    public Class<?>[] getLoadedClasses() throws JMException {
        return (Class<?>[]) MBEAN_SERVER.invoke(agentMBean.getObjectName(), "getLoadedClasses",
                new Object[] { this.getClass().getClassLoader() }, new String[] { ClassLoader.class.getName() });
    }

}
