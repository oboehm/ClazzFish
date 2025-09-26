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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * his class scans a sub directory for a given suffix. It is a re-implentation
 * of the ResourceWalker in the monitor package but with dependencies to
 * commons-io.
 *
 * @author oboehm
 * @since 3.0 (26.09.25)
 */
public class ResourceWalker {

    private final Path startDir;
    private final String suffix;

    /**
     * Instantiates a new resource walker for resources with the given suffix.
     *
     * @param dir    the start dir
     * @param suffix file suffix, e.g. ".xml"
     */
    public ResourceWalker(File dir, String suffix) {
        this(dir.toPath(), suffix);
    }

    /**
     * Instantiates a new resource walker for resources with the given suffix.
     *
     * @param dir    the start dir
     * @param suffix file suffix, e.g. ".xml"
     */
    public ResourceWalker(Path dir, String suffix) {
        this.startDir = dir.toAbsolutePath();
        this.suffix = suffix;
    }

    /**
     * Walk thru the directories and return all found file resources.
     *
     * @return a collection of resources
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public Collection<String> getResources() throws IOException {
        int startDirnameLength = startDir.toString().length();
        try (Stream<Path> stream = Files.walk(startDir)) {
            return stream.filter(Files::isRegularFile)
                    .map(p -> p.toString().substring(startDirnameLength))
                    .filter(s -> s.endsWith(suffix))
                    .collect(Collectors.toSet());
        }
    }

}
