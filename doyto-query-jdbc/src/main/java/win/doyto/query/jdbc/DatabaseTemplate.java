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

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import win.doyto.query.sql.SqlAndArgs;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

/**
 * DbTemplate
 *
 * @author f0rb on 2021-08-30
 */
@RequiredArgsConstructor
public class DatabaseTemplate implements DatabaseOperations {

    private static final RowMapper<Long> countRowMapper = new SingleColumnRowMapper<>(Long.class);
    private final JdbcOperations jdbcOperations;

    @Override
    public <V> List<V> query(SqlAndArgs sqlAndArgs, RowMapper<V> rowMapper) {
        return jdbcOperations.query(sqlAndArgs.getSql(), rowMapper, sqlAndArgs.getArgs());
    }

    @Override
    public <I, R> Map<I, List<R>> query(SqlAndArgs sqlAndArgs, ResultSetExtractor<Map<I, List<R>>> resultSetExtractor) {
        return jdbcOperations.query(sqlAndArgs.getSql(), resultSetExtractor, sqlAndArgs.getArgs());
    }

    @Override
    public int update(SqlAndArgs sqlAndArgs) {
        return jdbcOperations.update(sqlAndArgs.getSql(), sqlAndArgs.getArgs());
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public long count(SqlAndArgs sqlAndArgs) {
        return jdbcOperations.queryForObject(sqlAndArgs.getSql(), countRowMapper, sqlAndArgs.getArgs());
    }

    @Override
    public <I> I insert(SqlAndArgs sqlAndArgs, Class<I> idClass) {
        ArgumentPreparedStatementSetter pss = new ArgumentPreparedStatementSetter(sqlAndArgs.getArgs());
        try {
            GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcOperations.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sqlAndArgs.getSql(), Statement.RETURN_GENERATED_KEYS);
                pss.setValues(ps);
                return ps;
            }, keyHolder);
            return keyHolder.getKeyAs(idClass);
        } finally {
            pss.cleanupParameters();
        }
    }
}
