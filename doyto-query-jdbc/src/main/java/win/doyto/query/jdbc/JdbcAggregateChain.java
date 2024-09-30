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

import win.doyto.query.core.AggregateChain;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.jdbc.rowmapper.BeanPropertyRowMapper;
import win.doyto.query.jdbc.rowmapper.RowMapper;
import win.doyto.query.sql.AggregateQueryBuilder;
import win.doyto.query.sql.EntityMetadata;
import win.doyto.query.sql.SqlAndArgs;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JdbcAggregateChain
 *
 * @author f0rb on 2024/9/12
 */
public class JdbcAggregateChain<V> implements AggregateChain<V> {

    private static final Map<Class<?>, RowMapper<?>> holder = new ConcurrentHashMap<>();

    private final DatabaseOperations databaseOperations;
    private RowMapper<V> rowMapper;
    private final EntityMetadata entityMetadata;
    private DoytoQuery query;

    @SuppressWarnings("unchecked")
    public JdbcAggregateChain(DatabaseOperations databaseOperations, Class<V> viewClass) {
        this.databaseOperations = databaseOperations;
        this.entityMetadata = EntityMetadata.build(viewClass);
        this.rowMapper = (RowMapper<V>) holder.computeIfAbsent(viewClass, BeanPropertyRowMapper::new);
    }

    @Override
    public AggregateChain<V> filter(DoytoQuery query) {
        this.query = query;
        return this;
    }

    /**
     * Set custom mapper for view
     *
     * @param mapper {@link RowMapper}
     * @return {@link AggregateChain}
     */
    @SuppressWarnings("unchecked")
    @Override
    public AggregateChain<V> mapper(Object mapper) {
        this.rowMapper = (RowMapper<V>) mapper;
        return this;
    }

    @Override
    public List<V> query() {
        SqlAndArgs sqlAndArgs = AggregateQueryBuilder.buildSelectAndArgs(entityMetadata, query);
        return databaseOperations.query(sqlAndArgs, rowMapper);
    }

    @Override
    public long count() {
        SqlAndArgs sqlAndArgs = AggregateQueryBuilder.buildCountAndArgs(entityMetadata, query);
        return databaseOperations.count(sqlAndArgs);
    }

    @Override
    public void print() {
        AggregateQueryBuilder.buildSelectAndArgs(entityMetadata, query);
        AggregateQueryBuilder.buildCountAndArgs(entityMetadata, query);
    }

}
