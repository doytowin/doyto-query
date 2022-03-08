Architecture Migration for `DoytoQuery`
---

0.0.1
---
`DoytoQuery` acts as an SQL generation framework，using `SpringDataJPA` and `MyBatis` as SQL executor.

![](./images/architecture-0.0.x.png)

0.1.x
---
Replace `SpringDataJPA` and `MyBatis` with `spring-jdbc` due to table sharding.

![](./images/architecture-0.1.x.png)

0.2.x
---
Extract Controller implementation from tests to `doyto-query-web`.

![](./images/architecture-0.2.x.png)

0.3.x
---
Try to support both webmvc and webflux.

![](./images/architecture-0.3.x.png)

Support SQL and NoSQL databases.

![](./images/architecture-0.3.x.2.png)

Architecture for reactive version.

![](./images/architecture-0.3.x.3.png)
