/*
 * Copyright (c) 2009-2025 by Oliver Boehm
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
 * (c)reated 31.05.2017 by oliver (ob@aosd.de)
 */
package clazzfish.core;

import clazzfish.core.util.NestedZipFile;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * The Class Digger was extracted from ClasspathDigger and
 * ResourcepathDigger. It is intended to pull up common code of both
 * classes into this class to avoid code duplicates.
 * <p>
 * For later it is planned to derive ClasspathDigger from ResourepathDigger
 * (the other way around at now), because a class can be considered as
 * specialized resource (resource with suffix ".class").
 * </p>
 * <p>
 * Originally this class was part of the PatternTesting project.
 * </p>
 *
 * @author oboehm
 */
public class Digger {

    private static final Logger log = Logger.getLogger(Digger.class.getName());
    private static final FutureTask<String[]> allClasspathClasses;

    static {
        allClasspathClasses = new FutureTask<>(Digger::getClasspathClassArray);
        Executors.newCachedThreadPool().execute(allClasspathClasses);
    }

    private static String[] getClasspathClassArray() {
        Set<String> classSet = getAllClasses();
        return classSet.toArray(new String[0]);
    }

    /**
     * Converts a resource (e.g. "/java/lang/String.class") into its classname
     * ("java.lang.String").
     *
     * @param name e.g. "/java/lang/String.class"
     *
     * @return e.g. "java.lang.String"
     */
    public static String resourceToClass(String name) {
        if (name == null) {
            return null;
        }
        if (name.endsWith(".class")) {
            int lastdot = name.lastIndexOf('.');
            String classname = name.substring(0, lastdot).replaceAll("[/\\\\]", "\\.");
            if (classname.startsWith(".")) {
                classname = classname.substring(1);
            }
            return classname;
        } else {
            return name;
        }
    }

    /**
     * Gets the classpath using the {@link ClasspathInspector}.
     *
     * @return the classpath
     */
    public String[] getClasspath() {
        return ClasspathInspector.getClasspath();
    }

    /**
     * Digs into the classpath and returns the found classes.
     * <p>
     * NOTE: This logic was formerly part of ClasspathDigger in the
     * monitor module.
     * </p>
     *
     * @return classes of the classpath
     */
    public String[] getClasses() {
        try {
            return allClasspathClasses.get();
        } catch (InterruptedException e) {
            log.log(Level.WARNING, String.format("Was interrupted before got result from %s:", allClasspathClasses), e);
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            log.log(Level.WARNING, String.format("Cannot execute get of %s:", allClasspathClasses), e);
        }
        return getClasspathClassArray();
    }

    private static Set<String> getAllClasses() {
        Set<String> classSet = new TreeSet<>();
        for (String path : ClasspathInspector.getClasspath()) {
            addClasses(classSet, new File(path));
        }
        return classSet;
    }

    private static void addClasses(final Set<String> classSet, final File path) {
        log.finer(String.format("Adding classes from %s...", path));
        try {
            if (path.isDirectory()) {
                addClassesFromDir(classSet, path);
            } else {
                addElementsFromArchive(classSet, path, ".class");
            }
        } catch (IOException ioe) {
            log.log(Level.FINER, String.format("Cannot add classes from %s:", path.getAbsolutePath()), ioe);
        }
    }

    private static void addClassesFromDir(final Set<String> classSet, final File dir) throws IOException {
        ResourceWalker classWalker = new ResourceWalker(dir);
        Collection<String> classes = classWalker.getClasses();
        classSet.addAll(classes);
    }

    private static void addElementsFromArchive(Collection<String> elements, File archive, String suffix)
            throws IOException {
        Collection<String> allElements = readElementsFromNestedArchive(archive);
        for(String resource : allElements) {
            if (resource.endsWith(suffix)) {
                String classname = resourceToClass(resource);
                if (ClassFilter.DEFAULT.isIncluded(classname)) {
                    elements.add(resourceToClass(classname));
                }
            }
        }
    }

    /**
     * Read elements from nested archive.
     *
     * @param archive a JAR, WAR or EAR archive
     * @return the digged resources
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static Collection<String> readElementsFromNestedArchive(File archive) throws IOException {
        String path = stripPath(archive);
        String archiveDir = "";
        if (!path.toLowerCase().matches(".*\\.[jwe]ar")) {
            archiveDir = getArchiveDir(path);
            path = getArchivePath(path);
        }
        return readElementsFromArchive(new File(path), archiveDir);
    }

    private static String stripPath(File archive) {
        String filename = archive.getPath();
        if (filename.endsWith("!")) {
            return filename.substring(0, filename.length() - 1);
        }
        return filename;
    }

    private static String getArchiveDir(String path) {
        int i = path.lastIndexOf('!');
        String dir = path.substring(i+1);
        return dir.replace('\\', '/');
    }

    private static String getArchivePath(String path) {
        int i = path.lastIndexOf('!');
        return path.substring(0,i);
    }

    private static Collection<String> readElementsFromArchive(File archive, String archiveDir)
            throws IOException {
        Collection<String> elements = new ArrayList<>();
        try (ZipFile zipFile = new NestedZipFile(archive)) {
            String relPath = archiveDir.startsWith("/") ? archiveDir.substring(1) : archiveDir;
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.startsWith(relPath)) {
                    elements.add(name.substring(relPath.length()));
                }
            }
        } catch (RuntimeException ex) {
            log.warning(String.format("Could not read all entries in %s (%s).", archive, ex.getMessage()));
            log.log(Level.FINE, "Details:", ex);
        }
        log.finer(String.format("%d element(s) read from %s.", elements.size(), archive));
        return elements;
    }

}
