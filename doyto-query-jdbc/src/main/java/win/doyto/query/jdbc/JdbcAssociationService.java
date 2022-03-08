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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.transaction.annotation.Transactional;
import win.doyto.query.entity.UserIdProvider;
import win.doyto.query.service.AssociationService;
import win.doyto.query.service.UniqueKey;
import win.doyto.query.sql.AssociationSqlBuilder;
import win.doyto.query.sql.SqlAndArgs;

import java.util.List;
import java.util.Set;

/**
 * JdbcAssociationService
 *
 * @author f0rb on 2021-12-31
 */
public class JdbcAssociationService<K1, K2> implements AssociationService<K1, K2> {

    private DatabaseOperations databaseOperations;
    private final AssociationSqlBuilder<K1, K2> sqlBuilder;
    private final SingleColumnRowMapper<K1> k1RowMapper = new SingleColumnRowMapper<>();
    private final SingleColumnRowMapper<K2> k2RowMapper = new SingleColumnRowMapper<>();

    @Autowired(required = false)
    private UserIdProvider<?> userIdProvider = () -> null;

    public JdbcAssociationService(String tableName, String k1Column, String k2Columns) {
        this.sqlBuilder = new AssociationSqlBuilder<>(tableName, k1Column, k2Columns);
    }

    public JdbcAssociationService(String tableName, String k1Column, String k2Column, String createUserColumn) {
        this.sqlBuilder = new AssociationSqlBuilder<>(tableName, k1Column, k2Column, createUserColumn);
    }

    @Autowired
    public void setJdbcOperations(JdbcOperations jdbcOperations) {
        this.databaseOperations = new DatabaseTemplate(jdbcOperations);
    }

    @Override
    public int associate(Set<UniqueKey<K1, K2>> uniqueKeys) {
        SqlAndArgs sqlAndArgs = sqlBuilder.buildInsert(uniqueKeys, userIdProvider.getUserId());
        return databaseOperations.update(sqlAndArgs);
    }

    @Override
    public int dissociate(Set<UniqueKey<K1, K2>> uniqueKeys) {
        SqlAndArgs sqlAndArgs = sqlBuilder.buildDelete(uniqueKeys);
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
    @Transactional
    public int reassociateForK1(K1 k1, List<K2> list) {
        deleteByK1(k1);
        if (list.isEmpty()) {
            return 0;
        }
        return associate(k1, list);
    }

    private int associate(K1 k1, List<K2> k2List) {
        return associate(buildUniqueKeys(k1, k2List));
    }

    @Override
    @Transactional
    public int reassociateForK2(K2 k2, List<K1> list) {
        deleteByK2(k2);
        if (list.isEmpty()) {
            return 0;
        }
        return associate(list, k2);
    }

    private int associate(List<K1> k1List, K2 k2) {
        return associate(buildUniqueKeys(k1List, k2));
    }

    @Override
    public long count(Set<UniqueKey<K1, K2>> list) {
        SqlAndArgs sqlAndArgs = sqlBuilder.buildCount(list);
        return databaseOperations.count(sqlAndArgs);
    }

}
