[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.aosd.clazzfish/clazzfish/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.aosd.clazzfish/clazzfish)

# ClazzFish

ClazzFish is a collection of different modules, which help you to dive into the classpath of your application and to monitor it.
Originally it was part of [PatternTesting](http://patterntesting.org) but is now extracted to a separate project.

To start the monitor classes just call the main method in the Starter class:

     clazzfish.monitor.Starter.main()

But before you start your application add clazzfish-agent as Java agent to your VM args, e.g.

     java -javaagent:${user.home}/.m2/repository/de/aosd/clazzfish/clazzfish-agent/2.2/clazzfish-agent-2.2.jar ...

This is needed for Java 9 and later because the internals of the classloader are no longer accessible.
If the agent is started and the library loaded you can use the `jconsole` to examine the loaded classes and resources.
Or you can look for classes and classpathes which are unused.


## History

With v0.5 and v0.8 the first steps were done - the transfer of ClasspathMonitor and ResourcepathMonitor from the [PatternTesting](http://patterntesting.org) project.
V1.x is mainly for Java 8.
V2.x will support Java 9 and beyond and uses now the [fast-classpath-scanner](https://github.com/lukehutch/fast-classpath-scanner) to dive into the classpath.



## More Infos

* Release Notes: [CHANGELOG](CHANGELOG.md)
* [SQL Logging and Montitoring](src/doc/SQL-Logging.adoc)

---
Jan. 2022,
Oli B.
