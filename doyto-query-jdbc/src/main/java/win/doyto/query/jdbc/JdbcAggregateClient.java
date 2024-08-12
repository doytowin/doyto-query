/*
 * Copyright © 2019-2024 Forb Yuan
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

import lombok.AllArgsConstructor;
import win.doyto.query.core.AggregateClient;
import win.doyto.query.core.AggregatedQuery;
import win.doyto.query.jdbc.rowmapper.BeanPropertyRowMapper;
import win.doyto.query.jdbc.rowmapper.RowMapper;
import win.doyto.query.sql.AggregateQueryBuilder;
import win.doyto.query.sql.SqlAndArgs;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * JdbcAggregateClient
 *
 * @author f0rb on 2024/8/11
 */
@AllArgsConstructor
public class JdbcAggregateClient implements AggregateClient {
    private static final Map<Class<?>, RowMapper<?>> holder = new ConcurrentHashMap<>();
    private final DatabaseOperations databaseOperations;

    @SuppressWarnings("unchecked")
    @Override
    public <V, A extends AggregatedQuery> List<V> aggregate(Class<V> viewClass, A aggregatedQuery) {
        SqlAndArgs sqlAndArgs = AggregateQueryBuilder.buildSelectAndArgs(viewClass, aggregatedQuery);

        RowMapper<V> rowMapper = (RowMapper<V>) holder.computeIfAbsent(viewClass, BeanPropertyRowMapper::new);
        return databaseOperations.query(sqlAndArgs, rowMapper);
    }
}
