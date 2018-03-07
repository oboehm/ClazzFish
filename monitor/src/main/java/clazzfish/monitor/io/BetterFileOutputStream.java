/*
 * Copyright (c) 2013-2018 by Oli B.
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
 * (c)reated 14.09.2013 by Oli B. (boehm@javatux.de)
 */

package clazzfish.monitor.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Whenever you need a {@link FileOutputStream} and want a better toString
 * implementation you can use the class BetterFileOutputStream. "Better" means
 * that you see the name if the input file in the log which is much more helpful
 * than the default implementation of {@link Object#toString()}, especially for
 * logging and debugging.
 * <p>
 * Originally this class was part of the PatternTesting project.
 * </p>
 *
 * @author oliver (boehm@javatux.de)
 * @see FileOutputStream
 */
public final class BetterFileOutputStream extends FileOutputStream {

	private final File file;

	/**
	 * Instantiates a new better file output stream.
	 *
	 * @param name
	 *            the file name
	 * @throws FileNotFoundException
	 *             the file not found exception
	 * @see FileOutputStream#FileOutputStream(String)
	 */
	public BetterFileOutputStream(final String name) throws FileNotFoundException {
		super(name);
		this.file = new File(name);
	}

	/**
	 * Instantiates a new better file output stream.
	 *
	 * @param file
	 *            the file
	 * @throws FileNotFoundException
	 *             the file not found exception
	 * @see FileOutputStream#FileOutputStream(File)
	 */
	public BetterFileOutputStream(final File file) throws FileNotFoundException {
		super(file);
		this.file = file;
	}

	/**
	 * Instantiates a new better file output stream.
	 *
	 * @param name
	 *            the file name
	 * @param append
	 *            if it should be appended
	 * @throws FileNotFoundException
	 *             the file not found exception
	 * @see FileOutputStream#FileOutputStream(String,boolean)
	 */
	public BetterFileOutputStream(final String name, final boolean append) throws FileNotFoundException {
		super(name, append);
		this.file = new File(name);
	}

	/**
	 * Instantiates a new better file output stream.
	 *
	 * @param file
	 *            the file
	 * @param append
	 *            if it should be appended
	 * @throws FileNotFoundException
	 *             the file not found exception
	 * @see FileOutputStream#FileOutputStream(File,boolean)
	 */
	public BetterFileOutputStream(final File file, final boolean append) throws FileNotFoundException {
		super(file, append);
		this.file = file;
	}

	/**
	 * This toString implementation reports the name of the output file which is
	 * much more helpful than the default implementation of
	 * {@link Object#toString()}, especially for logging and debugging.
	 *
	 * @return the string
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		return "output stream for file \"" + this.file + '"';
	}

}
