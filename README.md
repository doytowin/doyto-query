[![License](https://img.shields.io/:license-apache-brightgreen.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Sonar Stats](https://sonarcloud.io/api/project_badges/measure?project=win.doyto%3Adoyto-query&metric=alert_status)](https://sonarcloud.io/dashboard?id=win.doyto%3Adoyto-query)
[![Code Lines](https://sonarcloud.io/api/project_badges/measure?project=win.doyto%3Adoyto-query&metric=ncloc)](https://sonarcloud.io/component_measures?id=win.doyto%3Adoyto-query&metric=ncloc)
[![Coverage Status](https://sonarcloud.io/api/project_badges/measure?project=win.doyto%3Adoyto-query&metric=coverage)](https://sonarcloud.io/component_measures?id=win.doyto%3Adoyto-query&metric=coverage)

DoytoQuery - The First Implementation of Object SQL Mapping for Java Applications Over Relational Databases
---

## Introduction

DoytoQuery is a powerful and easy-to-use Object SQL Mapping (OSM) framework for Java applications over relational databases. 
Unlike object-relational mapping (ORM), which attempts to directly map the object model and the relational model,
OSM introduces SQL as an intermediary between the object model and the relational model 
and concludes a set of solutions to map objects to SQL statements.

## The Mapping Solutions
- [The Query Mapping Solution in DoytoQuery](https://blog.doyto.win/post/the-query-mapping-solution-in-doyto-query/)
- [The Paging and Sorting Solution in DoytoQuery](https://blog.doyto.win/post/paging-and-sorting-en/)
- The Improved CRUD Solution in DoytoQuery
- [The Related Query Solution in DoytoQuery](https://blog.doyto.win/post/the-related-query-solution-in-doyto-query/)
- The Conditional Related Query Solution in DoytoQuery
- [The Aggregation Solution in DoytoQuery](https://blog.doyto.win/post/the-aggregation-query-solution-in-doyto-query/)
- The Natural Joins Mapping Solution
- The Associative Table Solution in DoytoQuery
- The Sharding Solution in DoytoQuery

## Features

- Data Access Layer
  - CRUD operations for single/sharding table.
  - CRD operations for associative table.
  - Query with related entities and views.
- Service Layer
  - CRUD methods.
  - Second-Level Cache.
  - UserId Injection.
  - EntityAspect Extensions.
- Controller Layer
  - Support RESTFul API.
  - ErrorCode Pre-definition.
  - Exception Assertion.
  - Exception Handler.
  - JsonResponse Wrapper.
  - Request/Entity/Response Transition.
  - Group Validation.
- Seamless integration with Spring WebMvc.
- Supported Databases
    - MySQL
    - Oracle
    - SQL Server
    - PostgreSQL
    - SQLite
    - HSQLDB
    - MongoDB

## Quick Usage

For a `UserEntity` defined as follows:
```java
@Getter
@Setter
@Entity(name = "user")
public class UserEntity extends AbstractPersistable<Long> {
    private String username;
    private String email;
    private Boolean valid;
}
```
we can define a query object to query data from database as follows:
```java
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserQuery extends PageQuery {
    private String username;
    private String emailLike;
    private Boolean valid;
}
```
and invoke the [`DataAccess#query(Q)`](https://github.com/doytowin/doyto-query/blob/main/doyto-query-api/src/main/java/win/doyto/query/core/DataAccess.java) method like this:
```java
@Service
public class UserService extends AbstractCrudService<UserEntity, Long, UserQuery> {
    public List<UserEntity> findValidGmailUsers() {
        UserQuery userQuery = UserQuery.builder().emailLike("@gmail.com").valid(true).pageSize(10).build();
        // Executed SQL: SELECT username, email, valid, id FROM t_user WHERE email LIKE ? AND valid = ? LIMIT 10 OFFSET 0
        // Parameters  : %@gmail.com%(java.lang.String), true(java.lang.Boolean)
        return dataAccess.query(userQuery);
    }
}
```

Please refer to the [demo](https://github.com/doytowin/doyto-query-demo) for more details.

## Architecture for 0.3.x and newer

<img alt="architecture-0.3.x" src="docs/images/architecture-0.3.x.2.png" width="50%">

## Versions

| Module                 | Snapshot                                                                                                                 | Release                                                                                          |
|------------------------|--------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------|
| doyto-query-api        | [![api-snapshots-img]](https://oss.sonatype.org/content/repositories/snapshots/win/doyto/doyto-query-api/)               | [![api-release-img]](https://search.maven.org/artifact/win.doyto/doyto-query-api/)               |
| doyto-query-geo        | [![geo-snapshots-img]](https://oss.sonatype.org/content/repositories/snapshots/win/doyto/doyto-query-geo/)               | [![geo-release-img]](https://search.maven.org/artifact/win.doyto/doyto-query-geo/)               |
| doyto-query-common     | [![common-snapshots-img]](https://oss.sonatype.org/content/repositories/snapshots/win/doyto/doyto-query-common/)         | [![common-release-img]](https://search.maven.org/artifact/win.doyto/doyto-query-common/)         |
| doyto-query-memory     | [![memory-snapshots-img]](https://oss.sonatype.org/content/repositories/snapshots/win/doyto/doyto-query-memory/)         | [![memory-release-img]](https://search.maven.org/artifact/win.doyto/doyto-query-memory/)         |
| doyto-query-sql        | [![sql-snapshots-img]](https://oss.sonatype.org/content/repositories/snapshots/win/doyto/doyto-query-sql/)               | [![sql-release-img]](https://search.maven.org/artifact/win.doyto/doyto-query-sql/)               |
| doyto-query-jdbc       | [![jdbc-snapshots-img]](https://oss.sonatype.org/content/repositories/snapshots/win/doyto/doyto-query-jdbc/)             | [![jdbc-release-img]](https://search.maven.org/artifact/win.doyto/doyto-query-jdbc/)             |
| doyto-query-web-common | [![web-common-snapshots-img]](https://oss.sonatype.org/content/repositories/snapshots/win/doyto/doyto-query-web-common/) | [![web-common-release-img]](https://search.maven.org/artifact/win.doyto/doyto-query-web-common/) |
| doyto-query-web        | [![web-snapshots-img]](https://oss.sonatype.org/content/repositories/snapshots/win/doyto/doyto-query-web/)               | [![web-release-img]](https://search.maven.org/artifact/win.doyto/doyto-query-web/)               |
| doyto-query-dialect    | [![dialect-snapshots-img]](https://oss.sonatype.org/content/repositories/snapshots/win/doyto/doyto-query-dialect/)       | [![dialect-release-img]](https://search.maven.org/artifact/win.doyto/doyto-query-dialect/)       |

## Related Resources

- Frameworks
    - [doyto-query](https://github.com/doytowin/doyto-query)
    - [doyto-query-reactive](https://github.com/doytowin/doyto-query-reactive)
    - [doyto-query-mongodb](https://github.com/doytowin/doyto-query-mongodb)
    - [doyto-query-language](https://github.com/doytowin/doyto-query-language)
    - [doyto-query-memorydb](https://github.com/doytowin/doyto-query-memorydb)

- DevOps
    - [OSS](https://github.com/doytowin/doyto-oss-parent)
    - [Workflows](https://github.com/doytowin/doyto-devops)
    - [Templates](https://github.com/doytowin/doyto-query-template)
    - [Images](https://github.com/doytowin/doyto-query-image)

- Projects
    - [Demo](https://github.com/doytowin/doyto-query-demo)
    - [Idea plugin](https://github.com/doytowin/doyto-query-intellij-plugin)
    - [I18n management service](https://github.com/doytowin/doyto-service-i18n)
    - [Code generator service](https://github.com/doytowin/doyto-service-generator)

- Documentation
    - [https://query.doyto.win/](https://query.doyto.win/)

License
-------
This project is under the [Apache Licence v2](https://www.apache.org/licenses/LICENSE-2.0).

[geo-snapshots-img]: https://img.shields.io/nexus/s/win.doyto/doyto-query-geo?color=blue&server=https%3A%2F%2Foss.sonatype.org
[geo-release-img]: https://img.shields.io/maven-central/v/win.doyto/doyto-query-geo?color=brightgreen
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
[web-common-snapshots-img]: https://img.shields.io/nexus/s/win.doyto/doyto-query-web-common?color=blue&server=https%3A%2F%2Foss.sonatype.org
[web-common-release-img]: https://img.shields.io/maven-central/v/win.doyto/doyto-query-web-common?color=brightgreen
[web-snapshots-img]: https://img.shields.io/nexus/s/win.doyto/doyto-query-web?color=blue&server=https%3A%2F%2Foss.sonatype.org
[web-release-img]: https://img.shields.io/maven-central/v/win.doyto/doyto-query-web?color=brightgreen
[dialect-snapshots-img]: https://img.shields.io/nexus/s/win.doyto/doyto-query-dialect?color=blue&server=https%3A%2F%2Foss.sonatype.org
[dialect-release-img]: https://img.shields.io/maven-central/v/win.doyto/doyto-query-dialect?color=brightgreen
