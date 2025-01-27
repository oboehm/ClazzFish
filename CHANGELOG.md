# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).


## [Unreleased]

### Changed
- ClazzStatistic writes the statistic not direct but to a tempory file to be more robust for parallel exports

## [2.4.2] (10-Jan-2025)

### Fixed
- exception loop at shutdown is avoided
  ([issue #22](https://github.com/oboehm/ClazzFish/issues/22))


## [2.4.1] (09-Jan-2025)

### Fixed
- corrupt lines in exported CSV files were ignored


## [2.4] (08-Jan-2025)

### Fixed
- no double entries for updated entries
  ([issue #21](https://github.com/oboehm/ClazzFish/issues/21))
- tested with Java 21 and PatternTesting


## [2.3] (02-Jan-2025)

### Changed
- **v2.3.11**: backport to Java 11
- **v2.3.11**: documentation updated
- MBean name of ClasspathAgent changed to "clazzfish:type=agent,agent=ClasspathAgent"
- MBeanHelper class replaced by MBeanFinder class
- update to Java 17

### Added
- ClazzStatistic collects class statistic over several runs of a program
- Starter class to automatically register the monitor classes to JMX
  ([issue #15](https://github.com/oboehm/ClazzFish/issues/15))
- dump directory can now be set via system property `clazzfish.dump.dir` or environment variable `CLAZZFISH_DUMP_DIR`
- Bank class to sample module


## [2.2] (02-Feb-2024)

### Added
- clazzfish-jdbc: now with PostgreSQL support

### Changed
- update to Java 17 as build environment
- using testcontainers for integration tests


## [2.1] (25-Apr-2023)

### Changed
- clazzfish-jdbc: passwords are suppressed during SQL logging
  ([issue #14](https://github.com/oboehm/ClazzFish/issues/14))
- documentation of [SQL-Logging](src/doc/SQL-Logging.adoc) continued


## [2.0] (19-Jan-2023)

### Changed
- support for Java 9+
- based on [fast-classpath-scanner](https://github.com/lukehutch/fast-classpath-scanner)

## [1.1] (07-Jan-2022)

### Security
- update to Log4J 2.17.1
  ([CVE-2021-44832](https://github.com/advisories/GHSA-8489-44mv-ggj8))


## [1.0] (13-Jul-2019)

### Added
* How to activate SQL logging?
  The answer to this question you can find now in the [documentation](src/doc/SQL-Logging.adoc).
  Also [clazzfish-sample](sample) is added as new module which provides some example and ideas how to use ClazzFish.

### Fixed
* _fixed_: too many classes found by ClasspathMonitor.getConcreteClassList()
  ([issue #1](https://github.com/oboehm/ClazzFish/issues/1))


## [0.9] (01-Jan-2019)

### Added

* clazzfish-jdbc is added as new module to support JDBC monitoring.
  It provides a ProxyDriver as JDBC driver which allows you to log and monitor the SQL statements.
  I.e. you can ask the monitor how often an SQL statement is called and how long does it takes.


## [0.8] (07-Nov-2018)

### Added

* Java agent for IBM classloader provided (clazzfish-agent)


## [0.5] (10-Mar-2018)

* The first version contains mainly a ClasspathMonitor and ResourcepathMonitor as JMX bean.
  Originally these classes were part of the [PatternTesting](http://patterntesting.org) project.
  With [OSSRH-38331](https://issues.sonatype.org/browse/OSSRH-38331) ClassFish is sync'd with Central Maven.
