/*
 * $Id: ArchivEntry.java,v 1.26 2017/11/09 20:34:51 oboehm Exp $
 *
 * Copyright (c) 2008 by Oliver Boehm
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
 * (c)reated 11-May-2009 by oliver (ob@oasd.de)
 */
package clazzfish.monitor.util;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * Unfortunately we can't extends URI because this is a final class. So now it
 * is more or less implemented as URI wrapper and is intended for the use with
 * zip and jar files to describe an entry inside an archive.
 * <p>
 * Historically some parts of this class were developed for a log browser for
 * Log4J. Next it was part of the PatternTesting project. The facility to read 
 * (compressed) tar files (using org.apache.commons.compress.tar.*) was removed
 * because we use it here only for zip and jar files.
 * </p>
 * <p>
 * This class is final because of performance reasons. If you need to derive it
 * write a feature request.
 * </p>
 *
 * @author oliver (ob@aosd.de)
 */
public final class ArchivEntry {

	private static final Logger LOG = LoggerFactory.getLogger(ArchivEntry.class);
	private final URI uri;
	private Long size = null;

	/**
	 * Instantiates a new archiv entry.
	 *
	 * @param file the file
	 */
	protected ArchivEntry(final File file) {
		this(file.toURI());
	}

	/**
	 * Instantiates a new archiv entry.
	 *
	 * @param uri
	 *            the uri
	 */
	public ArchivEntry(final URI uri) {
		this.uri = uri;
	}

	/**
	 * Instantiates a new archiv entry.
	 *
	 * @param url
	 *            the url
	 */
	public ArchivEntry(final URL url) {
		this(Converter.toURI(url));
	}

	/**
	 * Instantiates a new archiv entry.
	 *
	 * @param scheme
	 *            the scheme
	 * @param archive
	 *            the archive
	 * @param entry
	 *            the entry
	 *
	 * @throws URISyntaxException
	 *             the URI syntax exception
	 */
	public ArchivEntry(final String scheme, final File archive, final String entry) throws URISyntaxException {
		this(toURI(scheme, archive, entry));
	}

	private static URI toURI(final String scheme, final File archiv, final String name) throws URISyntaxException {
		String path = FilenameUtils.separatorsToUnix(archiv.getPath());
		String schemeURI = scheme + ":" + path + "!/" + name;
		return new URI(schemeURI).normalize();
	}

	/**
	 * To uri.
	 *
	 * @return the uRI
	 */
	public URI toURI() {
		return this.uri;
	}

	/**
	 * Checks if is file.
	 *
	 * @return true, if is file
	 */
	public boolean isFile() {
		return StringUtils.isEmpty(this.getEntry());
	}

	/**
	 * Gets the file archiv. The works of course only for JAR and ZIP files.
	 *
	 * @return the file
	 */
	public File getFileArchiv() {
		if (this.hasEntry()) {
			String prefix = StringUtils.substringBeforeLast(this.uri.toString(), "!");
			return Converter.toFile(prefix);
		}
		return Converter.toFile(this.uri);
	}

	/**
	 * Gets the zip file.
	 *
	 * @return the zip file
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public ZipFile getZipFile() throws IOException {
		try {
			return new NestedZipFile(this.getFileArchiv());
		} catch (IOException ioe) {
			throw new IOException("can't get zip file \"" + this.getFileArchiv() + '"', ioe);
		}
	}

	/**
	 * Gets the entry. If no entry can be found an empty string is returned now.
	 * This behaviour has changed with 1.5.
	 *
	 * @return the entry
	 */
	public String getEntry() {
		if (this.hasEntry()) {
			String path = this.uri.getRawSchemeSpecificPart();
			if (this.isBundleresource()) {
				return StringUtils.substringAfter(path.substring(2), "/");
			} else {
				String entry = StringUtils.substringAfterLast(path, "!");
				return (entry.charAt(0) == '/') ? entry.substring(1) : entry;
			}
		} else {
			return "";
		}
	}

	/**
	 * Checks for entry.
	 *
	 * @return true, if successful
	 */
	public boolean hasEntry() {
		String path = this.uri.getRawSchemeSpecificPart();
		if (this.isBundleresource()) {
			return path.substring(2).contains("/");
		}
		return path.contains("!");
	}

	private boolean isBundleresource() {
		return "bundleresource".equals(this.uri.getScheme());
	}

	/**
	 * Gets the zip entry.
	 *
	 * @return the zip entry
	 */
	public ZipEntry getZipEntry() {
		return new ZipEntry(this.getEntry());
	}

	/**
	 * Gets the size.
	 *
	 * @return the size
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public long getSize() throws IOException {
		if (size == null) {
			if (isFile()) {
				size = this.getFileArchiv().length();
			} else {
				ZipFile zipFile = getZipFile();
				String entry = this.getEntry();
				ZipEntry zipEntry = zipFile.getEntry(entry);
				if (zipEntry == null) {
				    throw new ZipException("entry '" + entry + "' not found in " + zipFile);
				}
				size = zipEntry.getSize();
			}
		}
		return size;
	}

	/**
	 * Gets the bytes.
	 *
	 * @return the bytes
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public byte[] getBytes() throws IOException {
		InputStream istream = this.uri.toURL().openStream();
		assert istream != null;
		try {
			return IOUtils.toByteArray(istream);
		} finally {
			istream.close();
		}
	}

	/**
	 * If two entries with the same resource or class name have a different size
	 * or not the same byte code they are considered as not equal.
	 *
	 * @param other
	 *            the other
	 * @return true if they have the same size and the same byte code.
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(final Object other) {
		if (other instanceof ArchivEntry) {
			return isEqualsWith((ArchivEntry) other);
		} else {
			return false;
		}
	}

	private boolean isEqualsWith(final ArchivEntry other) {
		if (!StringUtils.equals(this.getEntry(), other.getEntry())) {
			return false;
		}
		try {
			if (this.getSize() != other.getSize()) {
				return false;
			}
			return Arrays.equals(this.getBytes(), other.getBytes());
		} catch (IOException ioe) {
            LOG.info("Will use URI for comparison because cannot compare content of {} with {}.", this, other);
		    LOG.debug("Details:", ioe);
			return this.uri.equals(other.uri);
		}
	}

	/**
	 * Hash code.
	 *
	 * @return the hash code
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if (this.size == null) {
			return 0;
		} else {
			return this.size.hashCode();
		}
	}

	/**
	 * To string.
	 *
	 * @return the string
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		return "ArchivEntry " + this.uri;
	}

}
