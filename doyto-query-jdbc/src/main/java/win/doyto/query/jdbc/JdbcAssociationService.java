/*
 * Copyright © 2019-2021 Forb Yuan
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

import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import win.doyto.query.sql.AssociationSqlBuilder;
import win.doyto.query.sql.SqlAndArgs;
import win.doyto.query.sql.UniqueKey;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JdbcAssociationService
 *
 * @author f0rb on 2021-12-31
 */
public class JdbcAssociationService<K1, K2> implements AssociationService<K1, K2> {

    private DatabaseOperations databaseOperations;
    private AssociationSqlBuilder<K1, K2> sqlBuilder;
    private final SingleColumnRowMapper<K1> k1RowMapper = new SingleColumnRowMapper<>();
    private final SingleColumnRowMapper<K2> k2RowMapper = new SingleColumnRowMapper<>();

    public JdbcAssociationService(JdbcOperations jdbcOperations, String tableName, String k1Column, String k2Column) {
        this.databaseOperations = new DatabaseTemplate(jdbcOperations);
        this.sqlBuilder = new AssociationSqlBuilder<>(tableName, k1Column, k2Column);
    }

    @Override
    public int associate(K1 k1, K2 k2) {
        SqlAndArgs sqlAndArgs = buildInsert(k1, k2);
        return databaseOperations.update(sqlAndArgs);
    }

    @Override
    public List<K1> queryK1ByK2(K2 k2) {
        SqlAndArgs sqlAndArgs = sqlBuilder.buildSelectK1ColumnByK2Id(k2);
        return databaseOperations.query(sqlAndArgs, k1RowMapper);
    }

    @Override
    public List<K2> queryK2ByK1(K1 k1) {
        SqlAndArgs sqlAndArgs = sqlBuilder.buildSelectK2ColumnByK1Id(k1);
        return databaseOperations.query(sqlAndArgs, k2RowMapper);
    }

    @Override
    public int deleteByK1(K1 k1) {
        SqlAndArgs sqlAndArgs = sqlBuilder.buildDeleteByK1(k1);
        return databaseOperations.update(sqlAndArgs);
    }

    @Override
    public int deleteByK2(K2 k2) {
        SqlAndArgs sqlAndArgs = sqlBuilder.buildDeleteByK2(k2);
        return databaseOperations.update(sqlAndArgs);
    }

    @Override
    public int reassociateForK1(K1 k1, List<K2> list) {
        deleteByK1(k1);
        if (list.isEmpty()) {
            return 0;
        }
        return associate(k1, list);
    }

    private int associate(K1 k1, List<K2> list) {
        return associate(list.stream().map(k2 -> new UniqueKey<>(k1, k2)).collect(Collectors.toList()));
    }

    private int associate(List<UniqueKey<K1, K2>> ukList) {
        SqlAndArgs sqlAndArgs = sqlBuilder.buildInsert(ukList);
        return databaseOperations.update(sqlAndArgs);
    }

    @Override
    public int reassociateForK2(K2 k2, List<K1> list) {
        deleteByK2(k2);
        return associate(list, k2);
    }

    private int associate(List<K1> list, K2 k2) {
        return associate(list.stream().map(k1 -> new UniqueKey<>(k1, k2)).collect(Collectors.toList()));
    }

    private SqlAndArgs buildInsert(K1 k1, K2 k2) {
        return sqlBuilder.buildInsert(Arrays.asList(new UniqueKey<>(k1, k2)));
    }
}
