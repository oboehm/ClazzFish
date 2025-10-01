/*
 * Copyright (c) 2025 by Oli B.
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
 * (c)reated 26.09.25 by oboehm
 */
package clazzfish.core;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * his class scans a sub directory for a given suffix. It is a re-implentation
 * of the ResourceWalker in the monitor package but without dependencies to
 * commons-io.
 *
 * @author oboehm
 * @since 3.0 (26.09.25)
 */
public class ResourceWalker {

    private final Path startDir;
    private final FileFilter fileFilter;

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
     * @param dir    the start dir
     * @param suffix file suffix, e.g. ".xml"
     */
    public ResourceWalker(File dir, String suffix) {
        this(dir, getFileFilter(suffix));
    }

    private ResourceWalker(File dir, FileFilter filter) {
        this.startDir = dir.toPath();
        this.fileFilter = filter;
    }

    private static FileFilter getFileFilter(String suffix) {
        return file -> file.getName().endsWith(suffix);
    }

    private static FileFilter getAllResourcesFilter() {
        return file -> !file.getName().endsWith(".class");
    }

    /**
     * Walk thru the directories and return all found file resources.
     *
     * @return a collection of resources
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public Collection<String> getResources() throws IOException {
        return getResources(fileFilter);
    }

    /**
     * Walk thru the directories and return all class files as classname, e.g. a
     * file java/lang/String.class is returned as "java.lang.String".
     *
     * @return a collection of classnames
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public Collection<String> getClasses() throws IOException {
        Collection<String> resources = this.getResources(getFileFilter(".class"));
        Collection<String> classes = new ArrayList<>(resources.size());
        for (String res : resources) {
            if (ClassFilter.DEFAULT.isIncluded(res)) {
                classes.add(resourceToClass(res));
            }
        }
        return classes;
    }

    private Collection<String> getResources(FileFilter filter) throws IOException {
        int startDirnameLength = startDir.toString().length();
        try (Stream<Path> stream = Files.walk(startDir)) {
            return stream.filter(Files::isRegularFile)
                    .filter(p -> filter.accept(p.toFile()))
                    .map(p -> p.toString().substring(startDirnameLength))
                    .collect(Collectors.toSet());
        }
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

}
