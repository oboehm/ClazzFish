package clazzfish.monitor.internal;

import clazzfish.monitor.util.Converter;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The Class AbstractDiggerTest provides some common constants and setup
 * methodes which are used by the different derived unit tests.
 */
public abstract class AbstractDiggerTest {

    /** The Constant WORLD_WAR. */
    protected static final File WORLD_WAR = new File("src/test/resources/clazzfish/util/world.war");

    /** The Constant WORLD_EAR. */
    protected static final File WORLD_EAR = new File("src/test/resources/clazzfish/util/world.ear");

    /**
     * An {@link URLClassLoader} is mocked to return only one classpath which
     * is constructed by the arguments.
     *
     * @param jar the JAR part of the classpath
     * @param path e.g. "!/WEB-INF/classes"
     * @return an mocked {@link URLClassLoader}
     * @throws MalformedURLException the malformed URL exception
     */
    protected static URLClassLoader mockURLClassLoader(File jar, String path) throws MalformedURLException {
        URLClassLoader mockedClassLoader = mock(URLClassLoader.class);
        URL[] webclasspath = { new URL(jar.toURI() + path) };
        when(mockedClassLoader.getURLs()).thenReturn(webclasspath);
        return mockedClassLoader;
    }

    /**
     * Should return the digger for testing.
     *
     * @return digger
     */
    protected abstract AbstractDigger getDigger();

    /**
     * We use the String class as resource for testing. But with this class it
     * happened that it appeared 2 times in the classpath, e.g. if you call the
     * test inside your favorite IDE. In most cases this was the same classpath
     * where the doublet appears. Since 2.0 doublets in the same classpath are
     * not regarded as doublet.
     */
    @Test
    public void testGetResources() {
        AbstractDigger digger = getDigger();
        String rsc = Converter.toResource(String.class);
        Enumeration<URL> resources = digger.getResources(rsc);
        URL r1 = resources.nextElement();
        if (resources.hasMoreElements()) {
            URL r2 = resources.nextElement();
            assertThat(r1, not(r2));
        }
    }


}
