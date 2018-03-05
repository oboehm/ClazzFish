/*
 * Copyright (c) 2017-2018 by Oliver Boehm
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
 * (c)reated 24.01.2017 by oboehm (ob@oasd.de)
 */

package clazzfish.monitor.util;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * This class provides more details about the underlying ZIP file as the
 * original {@link ZipFile} class. E.g. it has a useful toString()
 * implementation which gives you the name of the file. And it allows
 * you to use nested ZIP files, e.g. ZIP files inside ZIP files.
 * <p>
 * Originally this class was part of the PatternTesting project.
 * </p>
 * 
 * @author oboehm
 */
public final class NestedZipFile extends ZipFile {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(NestedZipFile.class);

    /**
     * Instantiates a new detailed zip file.
     *
     * @param name the name
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public NestedZipFile(String name) throws IOException {
        this(new File(name));
    }

    /**
     * Instantiates a new detailed zip file.
     *
     * @param file a normal file or a nested ZIP file 
     *        (e.g. "file:/a.jar!/b.jar")
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public NestedZipFile(File file) throws IOException {
        this(file, Charset.defaultCharset());
    }

    /**
     * Instantiates a new detailed zip file.
     *
     * @param file the file
     * @param mode the mode
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public NestedZipFile(File file, int mode) throws IOException {
        this(file, mode, Charset.defaultCharset());
    }

    /**
     * Instantiates a new detailed zip file.
     *
     * @param name the name
     * @param charset the charset
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public NestedZipFile(String name, Charset charset) throws IOException {
        this(new File(name), charset);
    }

    /**
     * Instantiates a new detailed zip file.
     *
     * @param file the file
     * @param charset the charset
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public NestedZipFile(File file, Charset charset) throws IOException {
        this(file, OPEN_READ, charset);
    }

    /**
     * Instantiates a new detailed zip file.
     *
     * @param file the file
     * @param mode the mode
     * @param charset the charset
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public NestedZipFile(File file, int mode, Charset charset) throws IOException {
        super(getEmbeddedZipFile(file), mode, charset);
    }

    private static File getEmbeddedZipFile(File file) throws IOException {
        String path = file.getPath();
        if (path.contains("!")) {
            String[] pathes = path.split("!");
            return getEmbeddedZipFile(new File(pathes[0]), Arrays.copyOfRange(pathes, 1, pathes.length));
        }
        return file;
    }
    
    private static File getEmbeddedZipFile(File embeddingZip, String[] pathes) throws IOException {
        File embedded = getEmbbededZipFile(embeddingZip, pathes[0]);
        if (pathes.length > 1) {
            return getEmbeddedZipFile(embedded, Arrays.copyOfRange(pathes, 1, pathes.length));
        } else {
            return embedded;
        }
    }

    private static File getEmbbededZipFile(File file, String name) throws IOException {
        String normalizedName = FilenameUtils.separatorsToUnix(name.substring(1));
        try (ZipFile zipFile = new ZipFile(file)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (normalizedName.equals(entry.getName())) {
                    return getEmbeddedZipFile(zipFile, entry);
                }
            }
        }
        throw new FileNotFoundException("entry '" + name + "' not found in " + file);
    }
    
    private static File getEmbeddedZipFile(ZipFile zipFile, ZipEntry zipEntry) throws IOException {
        try (InputStream istream = zipFile.getInputStream(zipEntry)) {
            File tmpZipFile = File.createTempFile(zipEntry.getName(), ".zip");
            tmpZipFile.deleteOnExit();
            OutputStream ostream = new FileOutputStream(tmpZipFile);
            IOUtils.copy(istream, ostream);
            ostream.close();
            LOG.debug("File {} created with '{}' from file '{}'.", tmpZipFile, zipEntry, zipFile.getName());
            return tmpZipFile;
        }
    }

    /**
     * In contrast to the super class this method provides the name of the
     * ZIP file.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return "ZipFile '" + getName() + "'";
    }

}
