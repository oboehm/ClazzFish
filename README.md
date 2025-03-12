[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.aosd.clazzfish/clazzfish/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.aosd.clazzfish/clazzfish)

# ClazzFish <img src="src/doc/images/clazzfish-128x64.png" width="64">

ClazzFish is a collection of different modules, which help you to dive into the classpath of your application and to monitor it.
Originally it was part of [PatternTesting](http://patterntesting.org) but is now extracted to a separate project.

Part of ClassFish are several modules:

* agent
* [monitor](monitor/README.md)
* jdbc
* [sample](sample/README.md)



## History

With v0.5 and v0.8 the first steps were done - the transfer of ClasspathMonitor and ResourcepathMonitor from the [PatternTesting](http://patterntesting.org) project.
V1.x is mainly for Java 8.
V2.x supports Java 17 and beyond but still works with Java 11.
The use of [fast-classpath-scanner](https://github.com/lukehutch/fast-classpath-scanner) to dive into the classpath was discarded (no speedup was measured).



## More Infos

* Release Notes: [CHANGELOG](CHANGELOG.md)
* [JavaDoc](http://aosd.de/ClazzFish/)
* [How to Find Dead Classes](src/doc/DeadClasses.adoc)
* [SQL Logging and Montitoring](src/doc/SQL-Logging.adoc)

---
March 2022,
Oli B.
