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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.core.AssociationService;
import win.doyto.query.core.UniqueKey;
import win.doyto.query.entity.UserIdProvider;
import win.doyto.query.jdbc.rowmapper.SingleColumnRowMapper;
import win.doyto.query.sql.AssociationSqlBuilder;
import win.doyto.query.sql.SqlAndArgs;
import win.doyto.query.util.BeanUtil;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

/**
 * JdbcAssociationService
 *
 * @author f0rb on 2021-12-31
 */
@SuppressWarnings({"unchecked", "java:S6813"})
public abstract class JdbcAssociationService<K1, K2> implements AssociationService<K1, K2> {

    private static final GlobalConfiguration instance = GlobalConfiguration.instance();
    @Autowired
    private DatabaseOperations databaseOperations;
    private final AssociationSqlBuilder<K1, K2> sqlBuilder;
    private SingleColumnRowMapper<K1> k1RowMapper;
    private SingleColumnRowMapper<K2> k2RowMapper;

    @Autowired(required = false)
    private UserIdProvider<?> userIdProvider = () -> null;

    protected JdbcAssociationService(String domain1, String domain2) {
        this.sqlBuilder = new AssociationSqlBuilder<>(
                instance.formatJoinTable(domain1, domain2), instance.formatJoinId(domain1), instance.formatJoinId(domain2)
        );
        setRequiredType();
    }

    protected JdbcAssociationService(String domain1, String domain2, String createUserColumn) {
        this.sqlBuilder = new AssociationSqlBuilder<>(
                instance.formatJoinTable(domain1, domain2),
                instance.formatJoinId(domain1),
                instance.formatJoinId(domain2),
                createUserColumn
        );
        setRequiredType();
    }

    private void setRequiredType() {
        Type[] actualTypes = BeanUtil.getActualTypeArguments(getClass());
        if (actualTypes.length != 2) {
            throw new IllegalArgumentException("The number of generic parameters should be 2.");
        }
        k1RowMapper = new SingleColumnRowMapper<>((Class<K1>) actualTypes[0]);
        k2RowMapper = new SingleColumnRowMapper<>((Class<K2>) actualTypes[1]);
    }


    @Override
    public int associate(Set<UniqueKey<K1, K2>> uniqueKeys) {
        if (uniqueKeys.isEmpty()) {
            return 0;
        }
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
        return AssociationService.super.reassociateForK1(k1, list);
    }

    @Override
    @Transactional
    public int reassociateForK2(K2 k2, List<K1> list) {
        return AssociationService.super.reassociateForK2(k2, list);
    }

    @Override
    public long count(Set<UniqueKey<K1, K2>> list) {
        SqlAndArgs sqlAndArgs = sqlBuilder.buildCount(list);
        return databaseOperations.count(sqlAndArgs);
    }

}
