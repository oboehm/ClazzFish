This is the dump directory created by ClasspathMonitor with the following
(sorted) files:

BootClasspath.txt
    the boot classpath

ClassloaderInfo.txt
    some infos about the classloader which are used by the ClasspathMonitor
    
Classpath.txt
    the normal classpath
    
ClasspathClasses.txt
    the classpath classes are all classes which can be found in the classpath

Doublets.txt
    the doublets are duplicate classes, e. g. classes which are found in
    different parts of the classpath

DoubletClasspath.txt
    the doublet classpath is a list of directories or JAR file names where
    one of the doublets (or duplicate classes) was found

DoubletClasspathURIs.txt
    same as before but with the complete URI of the doublets

IncompatibleClasses.txt
    the incompatible classes are classes are doublet classes which are found
    in different versions

IncompatibleClasspath.txt
    the incompatible classpath is a list directories or JAR file names where
    on of the incompatible classes was found

IncompatibleClasspathURIs.txt
    same as before but with the complete URI of the incompatible classes

LoadedClasses.txt
    the loaded classes are the classes which are loaded by the classloader

LoadedPackages.txt
    the loaded packages are the packages which are loaded by the classloader

UnusedClasses.txt
    the unsused classes are the classes which are found in the classpath but
    was not loaded by the classloader

UnusedClasspath.txt
    the unused classpath is a list directories or JAR file names where the
    unused classes were found

UsedClasspath.txt
    the used classpath is a list directories or JAR file names where at least
    on of the used classes was found

UsedClasspathURIs.txt
    same as before but with the complete URI for the used classpath
