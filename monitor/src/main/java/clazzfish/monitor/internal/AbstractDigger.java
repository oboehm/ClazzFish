/*
 * Copyright (c) 2009-2018 by Oliver Boehm
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
package clazzfish.monitor.internal;

import clazzfish.monitor.util.NestedZipFile;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * The Class AbstractDigger was extracted from ClasspathDigger and
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
public abstract class AbstractDigger {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractDigger.class);

    /**
     * Gets the resources of the given name. Normally that would only be one
     * element but some resources (like the MANIFEST.MF file) can appear
     * several times in the classpath.
     *
     * @param name the name of the resource
     * @return all resources with the given name
     */
    public abstract Enumeration<URI> getResources(final String name);

    /**
     * Read elements from nested archive.
     *
     * @param archive a JAR, WAR or EAR archive
     * @return the digged resources
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected static Collection<String> readElementsFromNestedArchive(File archive) throws IOException {
        String path = StringUtils.removeEnd(archive.getPath(), "!");
        String archiveDir = "";
        if (!path.toLowerCase().matches(".*\\.[jwe]ar")) {
            archiveDir = StringUtils.substringAfterLast(path, "!");
            archiveDir = FilenameUtils.separatorsToUnix(archiveDir);
            path = StringUtils.substringBeforeLast(path, "!");
        }
        return readElementsFromArchive(new File(path), archiveDir);
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
            LOG.trace("{} element(s) read from {}.", elements.size(), archive);
            return elements;
        }
    }

}
