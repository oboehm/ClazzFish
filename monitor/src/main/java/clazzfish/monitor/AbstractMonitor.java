/*
 * Copyright (c) 2016-2018 by Oliver Boehm
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express orimplied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * (c)reated 25.02.2016 by oliver (ob@oasd.de)
 */

package clazzfish.monitor;

import clazzfish.monitor.io.ExtendedFile;
import clazzfish.monitor.jmx.MBeanHelper;
import clazzfish.monitor.util.ReflectionHelper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.ObjectName;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;

/**
 * This abstract base class has some support to for registration and
 * unregistration of a monitor class as shutdown hook. Originally this
 * class was part of the PatternTesting project.
 *
 * @author oliver
 */
public abstract class AbstractMonitor extends Thread implements AbstractMonitorMBean {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractMonitor.class);
    private boolean shutdownHook = false;
    private ObjectName mbeanName;

    /**
     * Here you can ask if the ClasspathMonitor was already registeres ad
     * shutdown hook.
     *
     * @return true if it is registered as shutdown hook.
     */
    @Override
    public synchronized boolean isShutdownHook() {
        return shutdownHook;
    }

    /**
     * To be able to register the instance as shutdown hook via JMX we can't use
     * a static method - this is the reason why this additional method was
     * added.
     * 
     * If this monitor class is registered as shutdown hook (see
     * {@link #addMeAsShutdownHook()} from a web application a dump to a
     * directory may not be possible because the application server (e.g.
     * Tomcat) has been stopped already the web application instance. This was
     * the problem as decribed in
     * <a href="https://sourceforge.net/p/patterntesting/bugs/37/">bugs/37</a>.
     * 
     * In this situation it is not possible to add monitor as shutdown hook.
     */
    @Override
    public void addMeAsShutdownHook() {
        ClassloaderType type = ClassloaderType.getCurrentClassloaderType();
        if (type.isWeb()) {
            LOG.info("Registration as shutdown hook is ignored inside {}.", type);
        } else {
            Runtime.getRuntime().addShutdownHook(this);
            this.shutdownHook = true;
            LOG.debug("{} is registered as shutdown hook", this);
        }
    }

    /**
     * If you want to unregister the instance as shutdown hook you can use this
     * (not static) method.
     */
    @Override
    public void removeMeAsShutdownHook() {
        Runtime.getRuntime().removeShutdownHook(this);
        this.shutdownHook = false;
        LOG.debug("{} is de-registered as shutdown hook", this);
    }

    /**
     * This method is called when the ClasspathMonitor is registered as shutdown
     * hook.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        try {
            File file = dumpMe();
            LOG.info("{} was dumped to '{}'.", this, file);
        } catch (IOException ioe) {
            LOG.error("Cannot dump {}:", this, ioe);
            logMe();
        } catch (NoClassDefFoundError error) {
            System.err.println("Cannot dump " + this + ": " + error);
        }
    }

    /**
     * With this method you can register the monitor with the default name.
     * <p>
     * You can only register the monitor only once. If you want to register it
     * with another name you have to first unregister it.
     * </p>
     */
    public void registerMeAsMBean() {
        this.registerMeAsMBean(MBeanHelper.getAsObjectName(this.getClass()));
    }

    /**
     * With this method you can register the monitor with your own name. This is
     * e.g. useful if you have an application server with several applications.
     * <p>
     * You can only register the monitor only once. If you want to register it
     * with another name you have to first unregister it.
     * </p>
     *
     * @param name the name
     */
    public void registerMeAsMBean(ObjectName name) {
        MBeanHelper.registerMBean(name, this);
        this.mbeanName = name;
    }

    /**
     * Unregister monitor as MBean.
     */
    public void unregisterMeAsMBean() {
        MBeanHelper.unregisterMBean(this.mbeanName);
    }

    /**
     * If you want to ask JMX if bean is already registered you can ask the
     * MBeanHelper or you can ask this method.
     *
     * @return true if MBean is already registered.
     */
    public boolean isMBean() {
        return MBeanHelper.isRegistered(this.mbeanName);
    }

    /**
     * Prints the different MBean attributes to the log output.
     */
    @Override
    public abstract void logMe();

    /**
     * This operation dumps the different MBean attributes to a temporary
     * directory with classname as prefix. The name of the created directory is
     * returned so that you can see it in the 'jconsole' (if you have triggered
     * it from there).
     *
     * @return the file
     * @throws IOException Signals that an I/O exception has occurred.
     * @see ClasspathMonitorMBean#dumpMe()
     */
    @Override
    public File dumpMe() throws IOException {
        File dumpDir = ExtendedFile.createTmpdir(this.getClass().getSimpleName(), "");
        this.dumpMe(dumpDir);
        return dumpDir;
    }

    /**
     * This operation dumps the different MBean attributes. Use this method if
     * you want to have all relevant infos in <em>one</em> stream. This is e.g.
     * used by the {@link #logMe()} method.
     *
     * @param writer a buffered writer
     * @param getterMethodNames the names of the getter methods which returns
     *        an array
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected void dump(final BufferedWriter writer, final String... getterMethodNames) throws IOException {
        for (String getter : getterMethodNames) {
            this.dumpArray(writer, getter);
        }
        writer.flush();
    }

    /**
     * Dumps different arrays to the given dumpDir directory, each array in its
     * own file. For each array a method name must be provided as argument. Each
     * given method must return an array as result.
     *
     * @param dumpDir the dump dir
     * @param getterMethodNames the name of the getter methods which return an
     *        array
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected void dump(final File dumpDir, final String... getterMethodNames) throws IOException {
        LOG.info("Attributes will be dumped to dir '{}'.", dumpDir);
        if (!dumpDir.exists()) {
            if (dumpDir.mkdir()) {
                LOG.debug("Directory '{}' successful created.", dumpDir);
            } else {
                LOG.error("Cannot create dir '{}' and will give up.", dumpDir);
                return;
            }
        }
        for (String name : getterMethodNames) {
            this.dumpArray(dumpDir, name);
        }
    }

    private void dumpArray(final File dir, final String title) throws IOException {
        File dumpFile = new File(dir, title + ".txt");
        LOG.debug("Dumping '{}' to {}...", title, dumpFile);
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(dumpFile), "UTF-8")) {
            dumpArray(new BufferedWriter(writer), title);
        }
    }

    private void dumpArray(BufferedWriter writer, final String title) throws IOException {
        try {
            Method getter = ReflectionHelper.getMethod(this.getClass(), "get" + title);
            Object[] array = (Object[]) getter.invoke(this);
            dumpArray(array, writer, title);
        } catch (NoSuchMethodException | IllegalAccessException ex) {
            throw new IllegalArgumentException("no accessible getter for '" + title + "' found", ex);
        } catch (InvocationTargetException ex) {
            LOG.error("Error happens in getter for '" + title + "':", ex);
            dumpException(writer, ex.getTargetException());
        } catch (RuntimeException ex) {
            LOG.error("Cannot get '" + title + "':", ex);
            dumpException(writer, ex);
        }
    }

    private static void dumpException(final BufferedWriter writer, final Throwable ex) throws IOException {
        writer.write("*** " + ex);
        writer.newLine();
        ex.printStackTrace(new PrintWriter(writer));
        writer.newLine();
        writer.flush();
    }

    /**
     * This operation dumps the different MBean attributes to the given
     * directory.
     *
     * @param dirname the directory name
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public void dumpMe(final String dirname) throws IOException {
        this.dumpMe(new File(dirname));
    }

    /**
     * This operation dumps the different MBean attributes to the given
     * directory.
     *
     * @param dumpDir the directory where the attributes are dumped to.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void dumpMe(final File dumpDir) throws IOException {
        LOG.info("Attributes will be dumped to dir '{}'.", dumpDir);
        if (!dumpDir.exists()) {
            if (dumpDir.mkdir()) {
                LOG.debug("Directory '{}' successful created.", dumpDir);
            } else {
                LOG.error("Cannot create dir '{}' and will give up.", dumpDir);
                return;
            }
        }
    }
    
    /**
     * Copy a resource to the given file.
     *
     * @param name the name
     * @param file the dir
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected void copyResource(final String name, final File file) throws IOException {
        URL readmeURL = ClasspathMonitor.class.getResource(name);
        if (readmeURL == null) {
            throw new IOException("'" + name + "' not found in classpath");
        }
        FileUtils.copyURLToFile(readmeURL, file);
    }

    /**
     * Dumps an array to the given directory.
     *
     * @param array the array
     * @param dir the dir
     * @param title the title (or name of the generated file)
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected static void dumpArray(Object[] array, File dir, String title) throws IOException {
        File dumpFile = new File(dir, title + ".txt");
        LOG.debug("Dumping '{}' to {}...", title, dumpFile);
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(dumpFile), "UTF-8")) {
            dumpArray(array, new BufferedWriter(writer), title);
        }
    }

    /**
     * Dump array.
     *
     * @param array the array
     * @param writer the writer
     * @param title the title
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected static void dumpArray(Object[] array, BufferedWriter writer, String title) throws IOException {
        dumpHeadline(writer, title + " (" + array.length + " entries)");
        for (Object a : array) {
            writer.write(a.toString());
            writer.newLine();
        }
        dumpHeadline(writer, title + " (end)");
        writer.flush();
    }

    /**
     * Dump headline.
     *
     * @param writer the writer
     * @param headline the headline
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected static void dumpHeadline(final BufferedWriter writer, final String headline) throws IOException {
        writer.write("----- " + headline + " -----");
        writer.newLine();
    }

    /**
     * We remove the URI prefix from the result to be in-line with the other
     * classpath methods.
     *
     * @param uris a collection with URIs
     * @return the string array
     */
    protected static String[] toStringArray(Collection<URI> uris) {
        return toStringArray(uris.toArray(new URI[uris.size()]));
    }

    /**
     * We remove the URI prefix from the result to be in-line with the other
     * classpath methods.
     *
     * @param uris an array with URIs
     * @return the string array
     */
    protected static String[] toStringArray(final URI[] uris) {
        String[] classpath = new String[uris.length];
        for (int i = 0; i < classpath.length; i++) {
            URI uri = uris[i];
            classpath[i] = uri.getPath();
            if (StringUtils.isEmpty(classpath[i])) {
                classpath[i] = "/" + StringUtils.substringAfter(uri.toString(), ":/");
            }
            classpath[i] = new File(classpath[i]).getAbsolutePath();
        }
        Arrays.sort(classpath);
        return classpath;
    }

}
