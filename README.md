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
* [spi/git](spi/git/README.md)


## Upcoming Events

<img src="https://www.java-forum-stuttgart.de/static/61b0bbc7e54c0a0363d7445042598e64/Banner_JFS2025_Speaker_e_468x60.jpg" alt="JFS 2025 logo">

On July 10, 2025, the 28th [Java Forum Stuttgart](https://www.java-forum-stuttgart.de/) will take place in southern Germany with over 1000 participants.
On the talk about "[Dead Classes](https://www.java-forum-stuttgart.de/vortraege/dead-classes/)" you can learn 

* the technique behind ClazzFish,
* how you use it and
* what are the pitfalls to detect dead classes.


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
