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
 * (c)reated 26.05.2017 by oboehm (ob@oasd.de)
 */

package clazzfish.monitor.internal;

import clazzfish.monitor.exception.ClassloaderException;
import clazzfish.monitor.util.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

/**
 * This is the sister of the {@link ClasspathDigger}. It digs into the
 * classpath to find resources and resource problems.
 * 
 * @author oboehm
 * @version $Revision: 1.5 $
 * @since 1.7.2 (26.05.2017)
 */
public class ResourcepathDigger extends ClasspathDigger {
    
    private static final Logger LOG = LoggerFactory.getLogger(ResourcepathDigger.class);

    /**
     * Instantiates a new digger for resources.
     */
    public ResourcepathDigger() {
        super();
    }

    /**
     * Instantiates a new digger for resources.
     * 
     * @param cloader the classloder used for digging
     */
    public ResourcepathDigger(ClassLoader cloader) {
        super(cloader);
    }
    
    /**
     * Digs into the classpath and returns the found resources.
     * 
     * @return resources of the classpath
     */
    public Set<String> getResources() {
        Set<String> resourceSet = new TreeSet<>();
        for (String path : getClasspath()) {
            addResources(resourceSet, new File(path));
        }
        return resourceSet;
    }

    /**
     * Returns the URI of the given resource and the given classloader. If the
     * resource is not found it will be tried again with/without a leading "/"
     * and with the parent classloader.
     *
     * @param name
     *            resource name (e.g. "log4j.properties")
     * @param cloader
     *            class loader
     * @return URI of the given resource (or null if resource was not found)
     */
    public static URI whichResource(final String name, final ClassLoader cloader) {
    	assert cloader != null : "no classloader given";
    	URL url = cloader.getResource(name);
    	if (url == null) {
    		String newName = name.startsWith("/") ? name.substring(1) : "/" + name;
    		try {
    			url = cloader.getResource(newName);
    		} catch (RuntimeException ex) {
    			throw new ClassloaderException(cloader, "cannot get resource \"" + name + "\"", ex);
    		}
    	}
    	if (url == null) {
    		ClassLoader parent = cloader.getParent();
    		if (parent == null) {
    			return null;
    		} else {
    			LOG.trace("{} not found with {}, will ask {}...", name, cloader, parent);
    			return whichResource(name, parent);
    		}
    	}
    	return Converter.toURI(url);
    }

    private static void addResources(final Set<String> resourceSet, final File path) {
        LOG.trace("Adding resources from {}...", path);
        try {
            if (path.isDirectory()) {
                addResourcesFromDir(resourceSet, path);
            } else {
                addElementsFromArchive(resourceSet, path);
            }
        } catch (IOException ioe) {
            LOG.warn("Cannot add classes from " + path.getAbsolutePath() + ":", ioe);
        }
    }

    private static void addResourcesFromDir(final Set<String> resourceSet, final File dir) throws IOException {
        ResourceWalker walker = new ResourceWalker(dir);
        Collection<String> resources = walker.getResources();
        resourceSet.addAll(resources);
    }
    
    private static void addElementsFromArchive(Set<String> resourceSet, File path) throws IOException {
        Collection<String> allElements = readElementsFromNestedArchive(path);
        for(String resource : allElements) {
            if (!resource.endsWith(".class") && !resource.endsWith("/")) {
                resourceSet.add(resource.startsWith("/") ? resource : "/" + resource);
            }
        }
    }

}
