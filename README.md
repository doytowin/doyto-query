[![License](https://img.shields.io/:license-apache-brightgreen.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Sonar Stats](https://sonarcloud.io/api/project_badges/measure?project=win.doyto%3Adoyto-query&metric=alert_status)](https://sonarcloud.io/dashboard?id=win.doyto%3Adoyto-query)
[![Code Lines](https://sonarcloud.io/api/project_badges/measure?project=win.doyto%3Adoyto-query&metric=ncloc)](https://sonarcloud.io/component_measures?id=win.doyto%3Adoyto-query&metric=ncloc)
[![Coverage Status](https://sonarcloud.io/api/project_badges/measure?project=win.doyto%3Adoyto-query&metric=coverage)](https://sonarcloud.io/component_measures?id=win.doyto%3Adoyto-query&metric=coverage)

DoytoQuery - A Dynamic Query Language Implemented in Java for CRUD
---

## Introduction

DoytoQuery implements a dynamic query language which generates query statements from objects.
Entity/view objects are used to generate table names and columns, 
while query/having objects are used to dynamically generate query conditions.
Each field defined in the query object is used to represent a query condition. 
When executing query, the query condition corresponding to the assigned field will be combined into the query clause, 
thereby completing the dynamic construction of SQL statements with entity/view objects.

## Quick Usage

1. Initialize the project on Spring Initializer with the following 4 dependencies:
* Lombok
* Spring Web
* Validation
* \<A database driver>

2. Add DoytoQuery dependencies in pom.xml:
```xml
<dependency>
    <groupId>win.doyto</groupId>
    <artifactId>doyto-query-jdbc</artifactId>
    <version>2.1.0</version>
</dependency>
<dependency>
    <groupId>win.doyto</groupId>
    <artifactId>doyto-query-web</artifactId>
    <version>2.1.0</version>
</dependency>
<dependency>
    <groupId>win.doyto</groupId>
    <artifactId>doyto-query-dialect</artifactId>
    <version>2.1.0</version>
</dependency>
```

3. Define entity and query objects for a table:
```java
@Getter
@Setter
@Entity(name = "user")
public class UserEntity extends AbstractPersistable<Long> {
    private String username;
    private Integer age;
    private Boolean valid;
}

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserQuery extends PageQuery {
    private String username;
    private Integer ageGe;
    private Integer ageLt;
    private Boolean valid;
}
```

Invoke the [`DataAccess#query(Q)`](https://github.com/doytowin/doyto-query/blob/main/doyto-query-api/src/main/java/win/doyto/query/core/DataAccess.java) method in `UserService`:
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

And a controller to support RESTful API:
```java
@RestController
@RequestMapping("user")
public class UserController extends AbstractEIQController<UserEntity, Integer, UserQuery> {
}
```

Refer to the [demo](https://github.com/doytowin/doyto-query-demo) for more details.

## Architecture for 0.3.x and newer

<img alt="architecture-0.3.x" src="docs/images/architecture-0.3.x.2.png" width="50%">

## Versions

| Module                 | Snapshot                                                                                                                 | Release                                                                                          |
|------------------------|--------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------|
| doyto-query-api        | [![api-snapshots-img]](https://oss.sonatype.org/content/repositories/snapshots/win/doyto/doyto-query-api/)               | [![api-release-img]](https://search.maven.org/artifact/win.doyto/doyto-query-api/)               |
| doyto-query-geo        | [![geo-snapshots-img]](https://oss.sonatype.org/content/repositories/snapshots/win/doyto/doyto-query-geo/)               | [![geo-release-img]](https://search.maven.org/artifact/win.doyto/doyto-query-geo/)               |
| doyto-query-common     | [![common-snapshots-img]](https://oss.sonatype.org/content/repositories/snapshots/win/doyto/doyto-query-common/)         | [![common-release-img]](https://search.maven.org/artifact/win.doyto/doyto-query-common/)         |
| doyto-query-sql        | [![sql-snapshots-img]](https://oss.sonatype.org/content/repositories/snapshots/win/doyto/doyto-query-sql/)               | [![sql-release-img]](https://search.maven.org/artifact/win.doyto/doyto-query-sql/)               |
| doyto-query-jdbc       | [![jdbc-snapshots-img]](https://oss.sonatype.org/content/repositories/snapshots/win/doyto/doyto-query-jdbc/)             | [![jdbc-release-img]](https://search.maven.org/artifact/win.doyto/doyto-query-jdbc/)             |
| doyto-query-web-common | [![web-common-snapshots-img]](https://oss.sonatype.org/content/repositories/snapshots/win/doyto/doyto-query-web-common/) | [![web-common-release-img]](https://search.maven.org/artifact/win.doyto/doyto-query-web-common/) |
| doyto-query-web        | [![web-snapshots-img]](https://oss.sonatype.org/content/repositories/snapshots/win/doyto/doyto-query-web/)               | [![web-release-img]](https://search.maven.org/artifact/win.doyto/doyto-query-web/)               |
| doyto-query-dialect    | [![dialect-snapshots-img]](https://oss.sonatype.org/content/repositories/snapshots/win/doyto/doyto-query-dialect/)       | [![dialect-release-img]](https://search.maven.org/artifact/win.doyto/doyto-query-dialect/)       |

## Supported Databases
- MySQL
- Oracle
- SQL Server
- PostgreSQL
- SQLite
- HSQLDB
- MongoDB

License
-------
This project is under the [Apache Licence v2](https://www.apache.org/licenses/LICENSE-2.0).

[geo-snapshots-img]: https://img.shields.io/nexus/s/win.doyto/doyto-query-geo?color=blue&server=https%3A%2F%2Foss.sonatype.org
[geo-release-img]: https://img.shields.io/maven-central/v/win.doyto/doyto-query-geo?color=brightgreen
[api-snapshots-img]: https://img.shields.io/nexus/s/win.doyto/doyto-query-api?color=blue&server=https%3A%2F%2Foss.sonatype.org
[api-release-img]: https://img.shields.io/maven-central/v/win.doyto/doyto-query-api?color=brightgreen
[common-snapshots-img]: https://img.shields.io/nexus/s/win.doyto/doyto-query-common?color=blue&server=https%3A%2F%2Foss.sonatype.org
[common-release-img]: https://img.shields.io/maven-central/v/win.doyto/doyto-query-common?color=brightgreen
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
