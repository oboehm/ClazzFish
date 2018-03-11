[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=de.aosd.clazzfish%3Aclazzfish&metric=coverage)](https://sonarcloud.io/dashboard?id=de.aosd.clazzfish%3Aclazzfish)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.aosd.clazzfish/clazzfish/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.aosd.clazzfish/clazzfish)

# ClazzFish

ClazzFish is a collection of different modules, which help you to dive into the classpath of your application and to monitor it.
Originally it was part of [PatternTesting](http://patterntesting.org) but is now extracted to a separate project.



## Roadmap

With v0.5 the first step is done - the transfer of ClasspathMonitor and ResourcpathMonitor from the [PatternTesting](http://patterntesting.org) project.
The next steps will be:

* Java agent for IBM classloader (clazzfish-agent)
* provide basic documentation, code cleanup, polishing
* use of [fast-classpath-scanner](https://github.com/lukehutch/fast-classpath-scanner) to dive into the classpath
* support of JDBC monitoring
* extend documentation



## More Infos

* Release Notes: [src/doc/RELEASES](src/doc/RELEASES.adoc)

---
March 2018,
Oli B.
