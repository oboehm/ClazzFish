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
 * (c)reated 14.04.2009 by oliver (ob@aosd.de)
 */
package clazzfish.monitor.internal;

import clazzfish.monitor.util.Converter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * The Class ClassWalker.
 * <p>
 * This class is for internal use only. Originally it was part of the
 * PatternTesting project.
 * </p>
 *
 * @author <a href="boehm@javatux.de">oliver</a>
 */
public class ClassWalker extends ResourceWalker {

	/**
	 * Instantiates a new class walker.
	 *
	 * @param dir the dir to walk
	 */
	public ClassWalker(final File dir) {
		super(dir, ".class");
	}

	/**
	 * Walk thru the directories and return all class files as classname, e.g. a
	 * file java/lang/String.class should be returned as "java.lang.String".
	 * <p>
	 * See also <a href=
	 * "http://commons.apache.org/io/api-release/org/apache/commons/io/DirectoryWalker.html">
	 * DirectoryWalker</a>.
	 * </p>
	 *
	 * @return a collection of classnames
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public Collection<String> getClasses() throws IOException {
		Collection<String> resources = this.getResources();
		Collection<String> classes = new ArrayList<>(resources.size());
		for (String res : resources) {
			if (isClassName(res)) {
				classes.add(Converter.resourceToClass(res));
			}
		}
		return classes;
	}

	public static boolean isClassName(String res) {
		return !"module-info.class".equals(res);
	}

}
