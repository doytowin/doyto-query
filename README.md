DoytoQuery -- A Java implementation for the 2nd generation ORM Framework
---
[![License](https://img.shields.io/:license-apache-brightgreen.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Maven Central](https://img.shields.io/maven-central/v/win.doyto/doyto-query?color=brightgreen)](https://search.maven.org/artifact/win.doyto/doyto-query/)
[![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/win.doyto/doyto-query?color=blue&server=https%3A%2F%2Foss.sonatype.org)](https://oss.sonatype.org/content/repositories/snapshots/win/doyto/doyto-query/)
[![Sonar Stats](https://sonarcloud.io/api/project_badges/measure?project=win.doyto%3Adoyto-query&metric=alert_status)](https://sonarcloud.io/dashboard?id=win.doyto%3Adoyto-query)
[![Code Lines](https://sonarcloud.io/api/project_badges/measure?project=win.doyto%3Adoyto-query&metric=ncloc)](https://sonarcloud.io/component_measures?id=win.doyto%3Adoyto-query&metric=ncloc)
[![Coverage Status](https://sonarcloud.io/api/project_badges/measure?project=win.doyto%3Adoyto-query&metric=coverage)](https://sonarcloud.io/component_measures?id=win.doyto%3Adoyto-query&metric=coverage)


## Concepts about the 2nd generation ORM Framework

**Mapping SQL statements to objects for database access operations**

## Features
- Data access layer
  - CRUD interface `DataAccess` 
  - Dialect extension
  - Mock implementation for `DataAccess`
  - Associative Service
  - Join/GroupBy Query
  
- Service layer
  - CRUD methods
  - Second level cache
  - UserId 
  - EntityAspect extension
  
- Controller layer
  - RESTFul API
  - ErrorCode
  - Exception assertion
  - Exception handler
  - Json response wrapper
  - Request/Entity/Response transition
  - Group validation

## Related resources

- Github
  - [https://github.com/f0rb/doyto-query](https://github.com/f0rb/doyto-query)
  - [https://github.com/f0rb/doyto-query-web](https://github.com/f0rb/doyto-query-web)
  - [https://github.com/doytowin/doyto-query-dialect](https://github.com/doytowin/doyto-query-dialect)

- Projects
  - [Demo](https://github.com/f0rb/doyto-query-demo)
  - [Idea plugin](https://github.com/doytowin/doyto-query-intellij-plugin)
  - [I18n management service](https://github.com/f0rb/doyto-service-i18n)
  - [Code generator service](https://gitee.com/doyto/doyto-service-generator)

- Documentation
  -  [https://query.doyto.win/](https://query.doyto.win/)

- Architecture for 0.3.x
  ![architecture-0.3.x](./doc/images/architecture-0.3.x.png)
