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

import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import win.doyto.query.sql.SqlAndArgs;

import java.util.List;
import java.util.Map;

/**
 * DbOperations
 *
 * @author f0rb on 2021-08-29
 */
public interface DatabaseOperations {

    <V> List<V> query(SqlAndArgs sqlAndArgs, RowMapper<V> rowMapper);

    long count(SqlAndArgs sqlAndArgs);

    <I> List<I> insert(SqlAndArgs sqlAndArgs, Class<I> idClass, String idColumn);

    int update(SqlAndArgs sqlAndArgs);

    <I, R> Map<I, List<R>> query(SqlAndArgs sqlAndArgs, ResultSetExtractor<Map<I, List<R>>> resultSetExtractor);
}
