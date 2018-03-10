/*
 * Copyright (c) 2015-2018 by Oliver Boehm
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
 * (c)reated 08.12.2015 by oliver (ob@oasd.de)
 */

package clazzfish.monitor.io;

import java.io.*;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

/**
 * The Class FileInputStreamReader is a small wrapper to an
 * {@link InputStreamReader} for files. It was copied from the
 * PatternTesting project.
 *
 * @author oliver
 */
public final class FileInputStreamReader extends Reader {

	private final Reader delegate;
	private final File file;

	/**
	 * Instantiates a new file input stream reader.
	 *
	 * @param file
	 *            the file
	 * @param encoding
	 *            the encoding
	 * @throws FileNotFoundException
	 *             the file not found exception
	 */
	public FileInputStreamReader(final File file, final Charset encoding) throws FileNotFoundException {
		this.file = file;
		ExtendedFile.validate(file);
		this.delegate = new BufferedReader(new InputStreamReader(new FileInputStream(this.file), encoding));
	}

	/**
	 * Hash code.
	 *
	 * @return the int
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return delegate.hashCode();
	}

	/**
	 * Read.
	 *
	 * @param target
	 *            the target
	 * @return the int
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @see Reader#read(CharBuffer)
	 */
	@Override
	public int read(final CharBuffer target) throws IOException {
		return delegate.read(target);
	}

	/**
	 * Read.
	 *
	 * @return the int
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @see Reader#read()
	 */
	@Override
	public int read() throws IOException {
		return delegate.read();
	}

	/**
	 * Equals.
	 *
	 * @param obj
	 *            the obj
	 * @return true, if successful
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		return delegate.equals(obj);
	}

	/**
	 * Read.
	 *
	 * @param cbuf
	 *            the cbuf
	 * @return the int
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @see Reader#read(char[])
	 */
	@Override
	public int read(final char[] cbuf) throws IOException {
		return delegate.read(cbuf);
	}

	/**
	 * Read.
	 *
	 * @param cbuf
	 *            the cbuf
	 * @param off
	 *            the off
	 * @param len
	 *            the len
	 * @return the int
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @see Reader#read(char[], int, int)
	 */
	@Override
	public int read(final char[] cbuf, final int off, final int len) throws IOException {
		return delegate.read(cbuf, off, len);
	}

	/**
	 * Skip.
	 *
	 * @param n
	 *            the n
	 * @return the long
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @see Reader#skip(long)
	 */
	@Override
	public long skip(final long n) throws IOException {
		return delegate.skip(n);
	}

	/**
	 * Ready.
	 *
	 * @return true, if successful
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @see Reader#ready()
	 */
	@Override
	public boolean ready() throws IOException {
		return delegate.ready();
	}

	/**
	 * Mark supported.
	 *
	 * @return true, if successful
	 * @see Reader#markSupported()
	 */
	@Override
	public boolean markSupported() {
		return delegate.markSupported();
	}

	/**
	 * Mark.
	 *
	 * @param readAheadLimit
	 *            the read ahead limit
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @see Reader#mark(int)
	 */
	@Override
	public void mark(final int readAheadLimit) throws IOException {
		delegate.mark(readAheadLimit);
	}

	/**
	 * Reset.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @see Reader#reset()
	 */
	@Override
	public void reset() throws IOException {
		delegate.reset();
	}

	/**
	 * Close.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @see Reader#close()
	 */
	@Override
	public void close() throws IOException {
		delegate.close();
	}

	/**
	 * To string.
	 *
	 * @return the string
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		return this.file.toString();
	}

}
