/*
 * Copyright (c) 2018 by Oliver Boehm
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * (c)reated 06.03.2018 by oboehm (ob@oasd.de)
 */
package clazzfish.monitor.util;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Time;
import java.time.LocalDate;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Converter} class.
 */
public class ConverterTest {
    
    private static final Logger LOG = LoggerFactory.getLogger(ConverterTest.class);

    /**
     * Test method for {@link Converter#getMemoryAsString(long)}.
     */
    @Test
    public void testGetMemoryAsString() {
        assertEquals("5 bytes", Converter.getMemoryAsString(5L));
        assertEquals("5 KB", Converter.getMemoryAsString(5000L));
        assertEquals("5 MB", Converter.getMemoryAsString(0x500000L));
    }

    /**
     * Test method for {@link Converter#getTimeAsString(long)}.
     */
    @Test
    public void testGetTimeAsString() {
        assertEquals("15 seconds", Converter.getTimeAsString(15000));
    }

    /**
     * Test method for {@link Converter#getTimeAsString(double)}.
     */
    @Test
    public void testGetTimeAsStringDouble() {
        assertEquals("0.123 ms", Converter.getTimeAsString(0.123, Locale.ENGLISH));
        assertEquals("12 ms", Converter.getTimeAsString(12.3, Locale.ENGLISH));
    }

    /**
     * Test resource to class.
     */
    @Test
    public void testResourceToClass() {
        String resource = "java/lang/String.class";
        assertEquals("java.lang.String", Converter.resourceToClass(resource));
        resource = "log4j.xml";
        assertEquals("log4j.xml", Converter.resourceToClass(resource));
    }

    /**
     * Test resource to class.
     */
    @Test
    public void testResourceToClassForWindows() {
        String resource = "java\\lang\\String.class";
        assertEquals("java.lang.String", Converter.resourceToClass(resource));
    }

    /**
     * Test class to resource.
     */
    @Test
    public void testClassToResource() {
        assertEquals("java/lang/String.class", Converter
                .toResource(String.class));
    }

    /**
     * Test package to resource.
     */
    @Test
    public void testPackageToResource() {
        Package p = Package.getPackage("java.lang");
        assertEquals("java/lang", Converter.toResource(p));
    }

    /**
     * Test to string object.
     */
    @Test
    public void testToStringObject() {
        Object s = "juhu";
        assertEquals("juhu", Converter.toString(s));
    }

    /**
     * Test to string object array.
     */
    @Test
    public void testToStringObjectArray() {
        Object[] array = { "achja" };
        assertEquals("[ achja ]", Converter.toString(array));
    }

    /**
     * Test to string object array.
     */
    @Test
    public void testToShortStringArray() {
        Object[] array = { "achja", null };
        String expected = Converter.toString(array);
        expected = expected.substring(1, expected.length()-1).trim();
        assertEquals(expected, Converter.toShortString(array).trim());
    }

    /**
     * Test method for {@link Converter#toShortString(Object[])}.
     */
    @Test
    public void testToShortStringByteArray() {
        byte[] bytes = { 1, 2, 3, 4, 5 };
        assertEquals("1, 2, 3, ...", Converter.toShortString(bytes));
    }

    /**
     * Test method for {@link Converter#toShortString(Object[])}.
     */
    @Test
    public void testToShortString() {
        String shortString = Converter.toShortString("a very long string which should be abbreviated");
        assertTrue(shortString.startsWith("a very long"), shortString);
        assertTrue(shortString.endsWith("..."), shortString);
    }

    /**
     * Test method for {@link Converter#toShortString(Number)}.
     */
    @Test
    public void testToShortFloat() {
        String s = Converter.toShortString((Object) 2.718281828459045235);
        assertEquals("2.71828", s);
    }

    /**
     * Test method for {@link Converter#toShortString(Number)}.
     */
    @Test
    public void testToShortInt() {
        String s = Converter.toShortString(1234);
        assertEquals("1234", s);
    }

    /**
     * Test method for {@link Converter#toShortString(Object)}. If the result of
     * the toString method is the result from {@link Object#toString()} and was
     * not overwritten we want to see only the classname, not the whole
     * packagename.
     */
    @Test
    public void testToShortObject() {
        String s = Converter.toShortString(this);
        assertTrue(s.startsWith(this.getClass().getSimpleName()), s + " should not start with classname only");
    }

    /**
     * Test method for {@link Converter#toShortString(Object)}. Here we only
     * want to see if this method works also with inner classes.
     */
    @Test
    public void testToShortInnerClass() {
        InnerConverterTest ict = new InnerConverterTest();
        String s = Converter.toShortString(ict);
        LOG.info("s = \"{}\"", s);
    }

    /**
     * As short representation for a class we expect only the name of the
     * class.
     */
    @Test
    public void testToShortClass() {
        String s = Converter.toShortString(String.class);
        assertEquals("String", s);
    }

    /**
     * Test to string null array.
     */
    @Test
    public void testToStringEmptyArray() {
        Object[] array = {};
        assertEquals("[]", Converter.toString(array));
    }

    /**
     * Tests the toString() implementation for a Dictionary.
     */
    @Test
    public void testToLongStringDictionary() {
        Properties props = new Properties();
        props.put("a", "10");
        props.put("c", "12");
        props.put("b", "11");
        String expected = "a=10\nb=11\nc=12\n";
        assertEquals(expected, Converter.toLongString(props));
        assertEquals(expected, Converter.toLongString((Object) props));
    }

    /**
     * Test method for {@link Converter#toLongString(Object)}.
     */
    @Test
    public void testToLongStringStackTrace() {
        StackTraceElement[] stacktrace = new StackTraceElement[2];
        stacktrace[0] = new StackTraceElement("ClassA", "methodA", "ClassA.java", 12);
        stacktrace[1] = new StackTraceElement("B", "b", "B.java", 34);
        String s = Converter.toLongString(stacktrace);
        assertTrue(s.startsWith("\tat " + stacktrace[0]), s);
        assertTrue(s.trim().endsWith("\n\tat " + stacktrace[1]), s);
    }

    /**
     * Test for toURI().
     *
     * @throws MalformedURLException should not happen
     */
    @Test
    public void testToURI() throws MalformedURLException {
        URL url = new URL("file:/with blank/");
        URI uri = Converter.toURI(url);
        assertNotNull(uri);
        LOG.info(url + " was converted to " + uri);
    }

    /**
     * For a file URL which represents a directory the {@link File#toURI()}
     * method generates an URI with a trailing slash ("/"). So should be
     * done also by {@link Converter#toURI(URL)}.
     *
     * @throws MalformedURLException should not happen
     */
    @Test
    public void testToURIwithDirURL() throws MalformedURLException {
        File dir = SystemUtils.getJavaHome();
        URL url = new URL("file:/" + FilenameUtils.separatorsToUnix(dir.getPath()));
        LOG.info("Checking '{}' as URI ({}).", dir, url);
        URI converted = Converter.toURI(url);
        checkURI(dir, converted);
    }

    /**
     * For a file URL which represents a directory the {@link File#toURI()}
     * method generates an URI with a trailing slash ("/"). So should be
     * done also by {@link Converter#toURI(String)}.
     */
    @Test
    public void testToURIwithDirname() {
        File dir = SystemUtils.getJavaIoTmpDir();
        String url = "file:/" + FilenameUtils.separatorsToUnix(dir.getPath());
        LOG.info("Checking '{}' as URI ({}).", dir, url);
        URI converted = Converter.toURI(url);
        checkURI(dir, converted);
    }

    private static void checkURI(File dir, URI converted) {
        LOG.info("Checking dir '{}' if it has '{}' as URI.", dir, converted);
        URI expected = dir.toURI();
        assertEquals(expected, converted);
    }

    /**
     * Test to file.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testToFile() throws IOException {
        checkToFile("/tmp/blubb.jar");
        checkToFile("/tmp/blubb.jar!/hello/world");
    }

    private void checkToFile(final String filename) throws IOException {
        File file = new File(filename).getCanonicalFile();
        URI uri = file.toURI();
        LOG.info(file + " as URI is " + uri);
        assertEquals(file, Converter.toFile(uri));
    }

    /**
     * Window platforms does not have normal path names. So we do an extra
     * check here for them.
     * @throws URISyntaxException if the jar URI is not recogniced
     */
    @Test
    public void testToJarFile() throws URISyntaxException {
        URI uri = new URI("jar:file:/C:/Dokumente%20und%20Einstellungen/.m2/hugo.jar");
        File file = Converter.toFile(uri);
        LOG.info("file=" + file);
        String filename = file.toString();
        assertTrue(filename.startsWith("C:")
                                || filename.startsWith("/C:"), file + " does not start with 'C:'");
    }

    /**
     * Tests the toDate() implementation.
     */
    @Test
    public void testToDate() {
        checkDate("26-Nov-2009", "dd-MMM-yyyy");
    }

    /**
     * Now we want to be able to set date <em>and</em> time.
     */
    @Test
    public void testToDateTime() {
        checkDate("01-Apr-2011", "dd-MMM-yyyy");
        checkDate("01-Apr-2011 17:55", "dd-MMM-yyyy H:m");
    }

    /**
     * Here we want to test the date with time and seconds.
     */
    @Test
    public void testToDateSeconds() {
        checkDate("08-12-2015 22:44:55", "dd-MM-yyyy H:m:s");
    }

    /**
     * Tests the toDate() implementation.
     */
    @Test
    public void testToDateUS() {
        checkDate("2009-11-26", "yyyy-MM-dd");
    }

    /**
     * Tests the toDate() implementation.
     */
    @Test
    public void testToDateWithBlanks() {
        checkDate("Nov 26 2009", "MMM dd yyyy");
    }

    /**
     * There was a problem with "30-May-2010" as date string.
     * It seems that "30-Mai-2010" is expected on a Mac with German (de) set as
     * default.
     */
    @Test
    public void testToDateMay() {
        if (Locale.getDefault().getLanguage().equals("de")) {
            checkDate("30-Mai-2010", "dd-MMM-yyyy", new Locale("de"));
        }
        checkDate("30-May-2010", "dd-MMM-yyyy", new Locale("en"));
    }

    private static void checkDate(final String s, final String pattern) {
        Date date = Converter.toDate(s);
        assertEquals(s, Converter.toString(date, pattern));
    }

    private static void checkDate(final String s, final String pattern, final Locale locale) {
        Date date = Converter.toDate(s);
        assertEquals(s, Converter.toString(date, pattern, locale));
    }

    /**
     * There is a problem with the default formatting of a {@link Date}. It
     * seems that it is not correct recognized (see also
     * http://stackoverflow.com/questions/999172/how-to-parse-a-date)
     */
    @Test
    public void testToDateDefaultFormat() {
        Date now = new Date();
        Date converted = Converter.toDate(now.toString());
        assertEquals(now.toString(), converted.toString());
    }

    /**
     * Test method for {@link Converter#toTime(String)}.
     */
    @Test
    public void testToTime() {
        String s = "11:55";
        Time time = Converter.toTime(s);
        assertEquals(s, Converter.toShortString(time));
    }

    @Test
    public void testToStringLocalDate() {
        LocalDate date = LocalDate.of(2022, 1, 7);
        assertEquals("07-Jan-2022", Converter.toString(date, "dd-MMM-yyyy"));
    }

    /**
     * Test method for {@link Converter#serialize(Serializable)} and
     * {@link Converter#deserialize(byte[])}.
     *
     * @throws NotSerializableException the not serializable exception
     * @throws ClassNotFoundException the class not found exception
     */
    @Test
    public void testSerialize() throws NotSerializableException, ClassNotFoundException {
        Serializable obj = new Date();
        byte[] serialized = Converter.serialize(obj);
        assertEquals(obj, Converter.deserialize(serialized));
    }

    private static class InnerConverterTest {
    }

}
