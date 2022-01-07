# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).


## [Unreleased]

### Security
- update to Log4J 2.17.1
  ([CVE-2021-44832](https://github.com/advisories/GHSA-8489-44mv-ggj8))

### Added
- support for Java 9 and higher added


## [1.0] (13-Jul-2019)

### Added
* How to activate SQL logging?
  The answer to this question you can find now in the [documentation](src/doc/SQL-Logging.adoc).
  Also [clazzfish-sample](sample) is added as new module which provides some example and ideas how to use ClazzFish.

### Fixed
* _fixed_: too many classes found by ClasspathMonitor.getConcreteClassList()
  (https://github.com/oboehm/ClazzFish/issues/1[issue #1])


## [0.9 and earlier]

see [src/doc/RELEASES](src/doc/RELEASES.adoc)
