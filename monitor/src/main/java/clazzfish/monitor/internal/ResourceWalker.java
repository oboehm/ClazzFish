/*
 * $Id: ResourceWalker.java,v 1.11 2016/12/10 20:55:22 oboehm Exp $
 *
 * Copyright (c) 2016-2018 by Oliver Boehm
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
 * (c)reated 24.04.16 by oliver (ob@oasd.de)
 */

package clazzfish.monitor.internal;

import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;

/**
 * This class scans a sub directory for a given suffix. Originally it was
 * part of the PatternTesting project.
 *
 * @author <a href="ob@aosd.de">oliver</a>
 */
public class ResourceWalker extends DirectoryWalker<String> {

	private final File startDir;
	private final int startDirnameLength;
	private final Collection<String> dirnames = new ArrayList<>();

	/**
	 * Instantiates a new Resource walker for all resources.
	 *
	 * @param dir the start dir
	 */
	public ResourceWalker(File dir) {
		this(dir, getAllResourcesFilter());
	}

	/**
	 * Instantiates a new resource walker for resources with the given suffix.
	 *
	 * @param dir
	 *            the start dir
	 * @param suffix
	 *            file suffix, e.g. ".xml"
	 */
	public ResourceWalker(File dir, String suffix) {
		this(dir, getFileFilter(suffix));
	}

	/**
	 * Instantiates a new Resource walker.
	 *
	 * @param dir
	 *            the dir
	 * @param filter
	 *            the filter
	 */
	public ResourceWalker(File dir, FileFilter filter) {
		super(filter, -1);
		this.startDir = dir;
		this.startDirnameLength = FilenameUtils.normalizeNoEndSeparator(dir.getAbsolutePath()).length();
	}

	private static FileFilter getFileFilter(String suffix) {
		IOFileFilter dirFilter = FileFilterUtils.and(FileFilterUtils.directoryFileFilter(), HiddenFileFilter.VISIBLE);
		IOFileFilter fileFilter = FileFilterUtils.and(FileFilterUtils.fileFileFilter(),
				FileFilterUtils.suffixFileFilter(suffix, IOCase.INSENSITIVE));
		return FileFilterUtils.or(dirFilter, fileFilter);
	}

	private static FileFilter getAllResourcesFilter() {
		IOFileFilter dirFilter = FileFilterUtils.and(FileFilterUtils.directoryFileFilter(), HiddenFileFilter.VISIBLE);
		IOFileFilter fileFilter = FileFilterUtils.and(FileFilterUtils.fileFileFilter(),
				FileFilterUtils.notFileFilter(new RegexFileFilter("^.*\\.class$")));
		return FileFilterUtils.or(dirFilter, fileFilter);
	}

	/**
	 * Walk thru the directories and return all found file resources.
	 *
	 * @return a collection of resources
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public Collection<String> getResources() throws IOException {
		Collection<String> resources = new TreeSet<>();
		walk(startDir, resources);
		return resources;
	}

	/**
	 * Walk thru the directories and return it as packages.
	 *
	 * @return the packages
	 * @throws IOException
	 *             the io exception
	 */
	public Collection<String> getPackages() throws IOException {
		dirnames.clear();
		Collection<String> dirs = new TreeSet<>();
		walk(startDir, dirs);
		Collection<String> packages = new ArrayList<>();
		for (String dirname : dirnames) {
			packages.add(dirname.substring(1) + "/");
		}
		return packages;
	}

	/**
	 * Handle file.
	 *
	 * @param file
	 *            the file
	 * @param depth
	 *            the depth
	 * @param results
	 *            the results
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @see DirectoryWalker#handleFile(File, int, Collection)
	 */
	@Override
	protected void handleFile(final File file, final int depth, final Collection<String> results) throws IOException {
		String absFilename = FilenameUtils.normalize(file.getAbsolutePath());
		String resource = absFilename.substring(startDirnameLength);
		if (!resource.isEmpty()) {
			String normalized = FilenameUtils.separatorsToUnix(resource);
			if (file.isDirectory()) {
				dirnames.add(normalized);
			} else {
				results.add(normalized);
			}
		}
	}

	/**
	 * Overridable callback method invoked to determine if a directory should be
	 * processed.
	 *
	 * @param directory
	 *            the current directory being processed
	 * @param depth
	 *            the current directory level (starting directory = 0)
	 * @param results
	 *            the collection of result objects, may be updated
	 * @return true to process this directory
	 * @throws IOException
	 *             if an I/O Error occurs
	 */
	@Override
	protected boolean handleDirectory(File directory, int depth, Collection<String> results) throws IOException {
		super.handleDirectory(directory, depth, results);
		handleFile(directory, depth, results);
		return true;
	}

}
