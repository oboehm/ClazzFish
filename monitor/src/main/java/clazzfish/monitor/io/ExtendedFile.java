/*
 * Copyright (c) 2014-2018 by Oliver Boehm
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
 * (c)reated 27.08.2014 by oliver (ob@oasd.de)
 */

package clazzfish.monitor.io;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;

/**
 * This is the extended version of the {@link File} class. It provides some
 * additional methods which are missing in this class.
 * <p>
 * Originally this class was part of the PatternTesting project but is now
 * transfered to here.
 * </p>
 *
 * @author oliver
 */
public final class ExtendedFile extends File {

	private static final long serialVersionUID = 20180307L;
	private static final Logger LOG = LoggerFactory.getLogger(ExtendedFile.class);

	/**
	 * Instantiates a new extended file.
	 *
	 * @param file the wrapped file
	 */
	public ExtendedFile(final File file) {
		super(file.getPath());
	}

	/**
	 * Instantiates a new extended file.
	 *
	 * @param filename
	 *            the filename
	 */
	public ExtendedFile(final String filename) {
		super(filename);
	}

	/**
	 * Instantiates a new extended file.
	 *
	 * @param parent
	 *            the parent
	 * @param filename
	 *            the filename
	 */
	public ExtendedFile(final File parent, final String filename) {
		super(parent, filename);
	}

	/**
	 * Instantiates a new extended file.
	 *
	 * @param parent
	 *            the parent
	 * @param filename
	 *            the filename
	 */
	public ExtendedFile(final String parent, final String filename) {
		super(parent, filename);
	}

	/**
	 * Instantiates a new extended file.
	 *
	 * @param fileURI
	 *            the file uri
	 */
	public ExtendedFile(final URI fileURI) {
		super(fileURI);
	}

	/**
	 * Instantiates a new extended file.
	 *
	 * @param parent
	 *            the parent
	 * @param file
	 *            the file
	 */
	public ExtendedFile(final File parent, final File file) {
		super(parent, file.getPath());
	}

	/**
	 * Validates the given file and throws an {@link FileNotFoundException} if
	 * it is not a valid file. This is the case if
	 * <ol>
	 * <li>the file does not exist or</li>
	 * <li>the file exists, but is not a file but a directory.</li>
	 * </ol>
	 * <p>
	 * If the file does not exist it tr tries guess the real filename. E.g. one
	 * of the common platform problems is that Windows does not support case
	 * sensitive filenames whereas Unix platforms distinguish between
	 * upper/lowercase in filenames. "readme.txt" and "README.TXT" are two
	 * different files under Unix but not on Windows.
	 * </p>
	 *
	 * @param file the file
	 * @throws FileNotFoundException the file not found exception
	 * @since 1.7
	 */
	public static void validate(File file) throws FileNotFoundException {
		if (!file.exists()) {
			throw new FileNotFoundException(getMessageForMissing(file));
		}
	}

    private static String getMessageForMissing(File file) {
        String msg = "file '" + file + "' does not exist";
        File similar = findSimilarFile(file);
        if (!similar.getPath().equals(file.getPath())) {
            msg += " - do you mean '" + similar + "'?";
        }
        return msg;
    }

    private static File findSimilarFile(File file) {
        File dir = file.getParentFile();
        if (dir == null) {
            File currentDir = new File(".");
            return new File(findSimilarFilename(currentDir, file.getName()));
        } else {
            File similarDir = findSimilarFile(dir);
            String similarFilename = findSimilarFilename(similarDir, file.getName());
            return new File(similarDir, similarFilename);
        }
    }

    private static String findSimilarFilename(File currentDir, String filename) {
        String[] currentFiles = currentDir.list();
        if (currentFiles != null) {
            for (String name : currentFiles) {
                if (filename.equalsIgnoreCase(name)) {
                    LOG.debug("Similar file '{}' for missing '{}' found.", name, filename);
                    return name;
                }
            }
        }
        LOG.trace("No similar '{}' found.", filename);
        return filename;
    }

    /**
     * Creates a directory in the temp directory.
     *
     * @param prefix the prefix
     * @param suffix the suffix
     * @return the directory
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static File createTmpdir(final String prefix, final String suffix) throws IOException {
    	long n = System.currentTimeMillis();
        File tmpdir = getTmpdir(prefix + "@" + Long.toString(n, Character.MAX_RADIX) + suffix);
		if (tmpdir.exists()) {
			LOG.debug("{} exists already, will increase timestamp.", tmpdir);
			tmpdir = getTmpdir(prefix + "@" + Long.toString(n+1, Character.MAX_RADIX) + suffix);
		}
        if (!tmpdir.mkdirs()) {
            throw new IOException("can't create dir " + tmpdir);
        }
        LOG.trace("Dir {} is created.", tmpdir);
        return tmpdir;
    }

    /**
     * Gets the tmpdir.
     *
     * @param filename the filename
     * @return the tmpdir
     */
    public static File getTmpdir(final String filename) {
        File tmpdir = FileUtils.getTempDirectory();
        return new File(tmpdir, filename);
    }

    /**
     * Converts a list with files into a string array.
     * <p>
     * Since 1.7 this method was moved from the (now deprecated) FileHelper to
     * this class here.
     * </p>
     *
     * @param files the files
     * @return the string[]
     * @since 1.5 (27.08.2014)
     */
    public static String[] toStringArray(final List<File> files) {
        String[] array = new String[files.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = files.get(i).getPath();
        }
        return array;
    }

	/**
	 * Gets the parent as {@link ExtendedFile}. This method was introduced
	 * because {@link #getParent()} and {@link #getParentFile()} also exists.
	 *
	 * @return the parent extended file
	 */
	public ExtendedFile getParentExtendedFile() {
		return new ExtendedFile(this.getParentFile());
	}

	/**
	 * Normalizes a file, analogous to {@link FilenameUtils#normalize(String)}.
	 * But before we normalize it we try to ask to OS for the canonical path for
	 * it.
	 *
	 * @return the normalized file
	 */
	public File normalize() {
		return normalize(this);
	}
	
	/**
	 * Normalizes a file, analogous to {@link FilenameUtils#normalize(String)}.
	 * But before we normalize it we try to ask to OS for the canonical path for
	 * it.
	 *
	 * @param file the file
	 * @return the file
	 */
	public static File normalize(File file) {
		String filename = file.getAbsolutePath();
		try {
			filename = file.getCanonicalPath();
		} catch (IOException ex) {
			LOG.debug("Cannot get " + file + " as cannocial path:", ex);
		}
		filename = FilenameUtils.normalize(filename);
		return new File(filename);
	}

	/**
	 * With this method you can ask if a file ends with the given extension.
	 *
	 * @param extension
	 *            the extension - must be a relative path e.g. "WEB-INF/classes"
	 * @return true, if successful
	 */
	public boolean endsWith(final File extension) {
		assert !extension.isAbsolute() : "not relative: " + extension;
		String extensionName = FilenameUtils.normalize(extension.getPath());
		String filename = FilenameUtils.normalize(this.getAbsolutePath());
		return filename.endsWith(extensionName);
	}

	/**
	 * Returns the base of the given file. The 'base' is the given file without
	 * the extension. Remember, the extension parameter is of type {@link File},
	 * so it must be a file (not a file extension like ".txt").
	 *
	 * @param extension
	 *            the extension - must be a relative path e.g. "WEB-INF/classes"
	 * @return the file, e.g. "/tmp/web"
	 */
	public File getBaseDir(final File extension) {
		assert !extension.isAbsolute() : "not relative: " + extension;
		if (extension.getName().equals(this.getName())) {
			File extensionParent = extension.getParentFile();
			if (extensionParent == null) {
				return this.getParentExtendedFile();
			} else {
				return this.getParentExtendedFile().getBaseDir(extensionParent);
			}
		}
		LOG.debug("Base dir is '{}' because '{}' does not end with '{}'.", this, extension, this.getName());
		return this;
	}
	
    /**
     * Creates the output stream of the file. As fallback <em>stdout</em> will
     * be used if the output stream cannot be created.
     *
     * @param file the file
     * @return the output stream
     * @since 1.7
     */
    public static OutputStream createOutputStreamFor(File file) {
        if (file.exists()) {
            if (file.delete()) {
                LOG.info("Old file \"{}\" was deleted.", file);
            } else {
                LOG.warn("Old log file \"{}\" could not be deleted!", file);
            }
        }
        try {
            return new BetterFileOutputStream(file, true);
        } catch (FileNotFoundException fnfe) {
            LOG.debug("Cannot log to {}:", file, fnfe);
            LOG.info("Will log to stdout because cannot log to \"{}\" ({}).", file, fnfe.getMessage());
            return System.out;
        }
    }

    /**
     * Creates the output stream of the file. As fallback <em>stdout</em> will
     * be used if the output stream cannot be created.
     * 
     * @return the output stream
     * @since 1.7
     */
    public OutputStream createOutputStream() {
        return createOutputStreamFor(this);
    }

	/**
     * Comparing two files is not so easy as it seems to be. On unix systems or
     * Mac OS-X you can see the same file via two different mount points or as
     * two different hard links. If you are on *nix you can use the inode to
     * compare but how to get the inode?
     *
     * @param obj the other file
     * @return true, if successful
     * @see File#equals(Object)
     */
	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof File)) {
			return false;
		}
		File otherFile = normalize((File) obj);
		File thisFile = this.normalize();
		return thisFile.getPath().equals(otherFile.getPath());
	}

	/**
	 * Hash code.
	 *
	 * @return the int
	 * @see File#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.normalize().hashCode();
	}

}
