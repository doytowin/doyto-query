[![License](https://img.shields.io/:license-apache-brightgreen.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Sonar Stats](https://sonarcloud.io/api/project_badges/measure?project=win.doyto%3Adoyto-query&metric=alert_status)](https://sonarcloud.io/dashboard?id=win.doyto%3Adoyto-query)
[![Code Lines](https://sonarcloud.io/api/project_badges/measure?project=win.doyto%3Adoyto-query&metric=ncloc)](https://sonarcloud.io/component_measures?id=win.doyto%3Adoyto-query&metric=ncloc)
[![Coverage Status](https://sonarcloud.io/api/project_badges/measure?project=win.doyto%3Adoyto-query&metric=coverage)](https://sonarcloud.io/component_measures?id=win.doyto%3Adoyto-query&metric=coverage)

DoytoQuery - A Java implementation of the 2nd generation ORM Framework
---

## Concepts about the 2nd generation ORM Framework

**Mapping SQL statements to objects for database access operations**

## Features
- Mapping query fields to query conditions in four ways:
  - @QueryField 
  - Suffix deduction
  - @NestedQueries
  - `Or` interface
  
- CRUD SQL building from Query object and Entity object.
  
- Complex Query SQL building from Query object and View object.

## Architecture for 0.3.x

<img alt="architecture-0.3.x" src="docs/images/architecture-0.3.x.2.png" width="50%">

## Versions

| Module | Snapshot | Release |
| --- | --- | --- |
| doyto-query-api | [![api-snapshots-img]](https://oss.sonatype.org/content/repositories/snapshots/win/doyto/doyto-query-api/) | [![api-release-img]](https://search.maven.org/artifact/win.doyto/doyto-query-api/) |
| doyto-query-common | [![common-snapshots-img]](https://oss.sonatype.org/content/repositories/snapshots/win/doyto/doyto-query-common/) | [![common-release-img]](https://search.maven.org/artifact/win.doyto/doyto-query-common/) |
| doyto-query-memory | [![memory-snapshots-img]](https://oss.sonatype.org/content/repositories/snapshots/win/doyto/doyto-query-memory/) | [![memory-release-img]](https://search.maven.org/artifact/win.doyto/doyto-query-memory/) |
| doyto-query-sql | [![sql-snapshots-img]](https://oss.sonatype.org/content/repositories/snapshots/win/doyto/doyto-query-sql/) | [![sql-release-img]](https://search.maven.org/artifact/win.doyto/doyto-query-sql/) |
| doyto-query-jdbc | [![jdbc-snapshots-img]](https://oss.sonatype.org/content/repositories/snapshots/win/doyto/doyto-query-jdbc/) | [![jdbc-release-img]](https://search.maven.org/artifact/win.doyto/doyto-query-jdbc/) |
| doyto-query-mongodb | [![mongodb-snapshots-img]](https://oss.sonatype.org/content/repositories/snapshots/win/doyto/doyto-query-mongodb/) | [![mongodb-release-img]](https://search.maven.org/artifact/win.doyto/doyto-query-mongodb/) |
| doyto-query-web | [![web-snapshots-img]](https://oss.sonatype.org/content/repositories/snapshots/win/doyto/doyto-query-web/) | [![web-release-img]](https://search.maven.org/artifact/win.doyto/doyto-query-web/) |
| doyto-query-dialect | [![dialect-snapshots-img]](https://oss.sonatype.org/content/repositories/snapshots/win/doyto/doyto-query-dialect/) | [![dialect-release-img]](https://search.maven.org/artifact/win.doyto/doyto-query-dialect/) |

## Related resources

- Github
  - [doyto-query](https://github.com/doytowin/doyto-query)
  - [doyto-query-reactive](https://github.com/doytowin/doyto-query-reactive)
  - [doyto-query-dialect](https://github.com/doytowin/doyto-query-dialect)

- Projects
  - [Demo](https://github.com/f0rb/doyto-query-demo)
  - [Idea plugin](https://github.com/doytowin/doyto-query-intellij-plugin)
  - [I18n management service](https://github.com/f0rb/doyto-service-i18n)
  - [Code generator service](https://gitee.com/doyto/doyto-service-generator)

- Documentation
  -  [https://query.doyto.win/](https://query.doyto.win/)

License
-------
This project is under the [Apache Licence v2](https://www.apache.org/licenses/LICENSE-2.0).

[api-snapshots-img]: https://img.shields.io/nexus/s/win.doyto/doyto-query-api?color=blue&server=https%3A%2F%2Foss.sonatype.org
[api-release-img]: https://img.shields.io/maven-central/v/win.doyto/doyto-query-api?color=brightgreen
[common-snapshots-img]: https://img.shields.io/nexus/s/win.doyto/doyto-query-common?color=blue&server=https%3A%2F%2Foss.sonatype.org
[common-release-img]: https://img.shields.io/maven-central/v/win.doyto/doyto-query-common?color=brightgreen
[memory-snapshots-img]: https://img.shields.io/nexus/s/win.doyto/doyto-query-memory?color=blue&server=https%3A%2F%2Foss.sonatype.org
[memory-release-img]: https://img.shields.io/maven-central/v/win.doyto/doyto-query-memory?color=brightgreen
[sql-snapshots-img]: https://img.shields.io/nexus/s/win.doyto/doyto-query-sql?color=blue&server=https%3A%2F%2Foss.sonatype.org
[sql-release-img]: https://img.shields.io/maven-central/v/win.doyto/doyto-query-sql?color=brightgreen
[jdbc-snapshots-img]: https://img.shields.io/nexus/s/win.doyto/doyto-query-jdbc?color=blue&server=https%3A%2F%2Foss.sonatype.org
[jdbc-release-img]: https://img.shields.io/maven-central/v/win.doyto/doyto-query-jdbc?color=brightgreen
[mongodb-snapshots-img]: https://img.shields.io/nexus/s/win.doyto/doyto-query-mongodb?color=blue&server=https%3A%2F%2Foss.sonatype.org
[mongodb-release-img]: https://img.shields.io/maven-central/v/win.doyto/doyto-query-mongodb?color=brightgreen
[web-snapshots-img]: https://img.shields.io/nexus/s/win.doyto/doyto-query-web?color=blue&server=https%3A%2F%2Foss.sonatype.org
[web-release-img]: https://img.shields.io/maven-central/v/win.doyto/doyto-query-web?color=brightgreen
[dialect-snapshots-img]: https://img.shields.io/nexus/s/win.doyto/doyto-query-dialect?color=blue&server=https%3A%2F%2Foss.sonatype.org
[dialect-release-img]: https://img.shields.io/maven-central/v/win.doyto/doyto-query-dialect?color=brightgreen
