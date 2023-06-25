/*
 * Copyright © 2019-2023 Forb Yuan
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
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.sql.SqlAndArgs;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * DatabaseTemplate
 *
 * @author f0rb on 2021-08-30
 */
@RequiredArgsConstructor
public class DatabaseTemplate implements DatabaseOperations {

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

    @SuppressWarnings({"java:S2259"})
    @Override
    public long count(SqlAndArgs sqlAndArgs) {
        return jdbcOperations.queryForObject(sqlAndArgs.getSql(), Long.class, sqlAndArgs.getArgs());
    }

    @Override
    public <I> List<I> insert(SqlAndArgs sqlAndArgs, Class<I> keyClass, String keyColumn) {
        ArgumentPreparedStatementSetter pss = new ArgumentPreparedStatementSetter(sqlAndArgs.getArgs());
        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcOperations.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sqlAndArgs.getSql(), Statement.RETURN_GENERATED_KEYS);
                pss.setValues(ps);
                return ps;
            }, keyHolder);
            return keyHolder.getKeyList().stream()
                    .map(map -> GlobalConfiguration.dialect().resolveKey((Number) map.get(keyColumn), keyClass))
                    .collect(Collectors.toList());
        } finally {
            pss.cleanupParameters();
        }
    }

}
