/*
 * Copyright © 2019-2022 Forb Yuan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package win.doyto.query.jdbc;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.RowMapper;
import win.doyto.query.core.DataQuery;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.sql.JoinQueryBuilder;
import win.doyto.query.sql.SqlAndArgs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JoinQueryService
 *
 * @author f0rb on 2019-06-09
 */
public class JdbcDataQuery implements DataQuery {

    private Map<Class<?>, RowMapper<?>> holder = new HashMap<>();
    private DatabaseOperations databaseOperations;

    public JdbcDataQuery(JdbcOperations jdbcOperations) {
        this.databaseOperations = new DatabaseTemplate(jdbcOperations);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E, Q extends DoytoQuery> List<E> query(Q query, Class<E> entityClass) {
        RowMapper<E> rowMapper = (RowMapper<E>) holder.computeIfAbsent(entityClass, BeanPropertyRowMapper::new);
        SqlAndArgs sqlAndArgs = JoinQueryBuilder.buildSelectAndArgs(query, entityClass);
        return databaseOperations.query(sqlAndArgs, rowMapper);
    }

    @Override
    public <E, Q extends DoytoQuery> Long count(Q query, Class<E> entityClass) {
        SqlAndArgs sqlAndArgs = JoinQueryBuilder.buildCountAndArgs(query, entityClass);
        return databaseOperations.count(sqlAndArgs);
    }

}
