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

package win.doyto.query.sql;

import lombok.Getter;
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.service.UniqueKey;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static win.doyto.query.sql.Constant.*;

/**
 * AssociationSqlBuilder
 *
 * @author f0rb on 2021-12-17
 */
@SuppressWarnings("java:S1068")
public class AssociationSqlBuilder<K1, K2> {
    @Getter
    private String selectK1ColumnByK2Id;
    @Getter
    private String selectK2ColumnByK1Id;
    @Getter
    private String deleteByK1;
    @Getter
    private String deleteByK2;
    private String insertSql;
    private String placeHolders;
    private String placeHolderFormat;
    private String deleteIn;
    private String countIn;

    public AssociationSqlBuilder(String tableName, String k1Column, String k2Column) {
        selectK1ColumnByK2Id = SELECT + k1Column + FROM + tableName + WHERE + k2Column + EQUAL_HOLDER;
        selectK2ColumnByK1Id = SELECT + k2Column + FROM + tableName + WHERE + k1Column + EQUAL_HOLDER;
        deleteByK1 = DELETE_FROM + tableName + WHERE + k1Column + EQUAL_HOLDER;
        deleteByK2 = DELETE_FROM + tableName + WHERE + k2Column + EQUAL_HOLDER;

        insertSql = INSERT_INTO + tableName + SPACE + OP + k1Column + SEPARATOR + k2Column + CP + VALUES;
        placeHolders = "(?, ?)";
        placeHolderFormat = "(?, ?)";
        deleteIn = DELETE_FROM + tableName + WHERE + OP + k1Column + SEPARATOR + k2Column + CP + IN;
        countIn = SELECT + COUNT + Constant.FROM + tableName + WHERE + OP + k1Column + SEPARATOR + k2Column + CP + IN;
    }

    public AssociationSqlBuilder(String tableName, String k1Column, String k2Column, String createUserColumn) {
        this(tableName, k1Column, k2Column);
        insertSql = INSERT_INTO + tableName + SPACE + OP + k1Column + SEPARATOR + k2Column + SEPARATOR + createUserColumn + CP + VALUES;
        placeHolderFormat = "(?, ?, %s)";
    }

    private void buildPlaceHolders(StringBuilder sb, int size, String placeHolders) {
        sb.append(IntStream.range(0, size).mapToObj(i -> placeHolders).collect(Collectors.joining(SEPARATOR)));
    }

    public SqlAndArgs buildInsert(Set<UniqueKey<K1, K2>> keys) {
        return buildInsert(keys, null);
    }

    public SqlAndArgs buildInsert(Set<UniqueKey<K1, K2>> keys, Object userId) {
        return SqlAndArgs.buildSqlWithArgs(argList -> {
            keys.stream().map(UniqueKey::toList).forEach(argList::addAll);
            StringBuilder insertBuilder = new StringBuilder(insertSql);
            String ph = String.format(placeHolderFormat, userId);
            buildPlaceHolders(insertBuilder, keys.size(), ph);
            return GlobalConfiguration.dialect().buildInsertIgnore(insertBuilder);
        });
    }

    public SqlAndArgs buildDelete(Set<UniqueKey<K1, K2>> keys) {
        return SqlAndArgs.buildSqlWithArgs(argList -> {
            keys.stream().map(UniqueKey::toList).forEach(argList::addAll);
            StringBuilder deleteBuilder = new StringBuilder(deleteIn).append(OP);
            buildPlaceHolders(deleteBuilder, keys.size(), placeHolders);
            return deleteBuilder.append(CP).toString();
        });
    }

    public SqlAndArgs buildCount(Set<UniqueKey<K1, K2>> keys) {
        return SqlAndArgs.buildSqlWithArgs(argList -> {
            keys.stream().map(UniqueKey::toList).forEach(argList::addAll);
            StringBuilder countBuilder = new StringBuilder(countIn).append(OP);
            buildPlaceHolders(countBuilder, keys.size(), placeHolders);
            return countBuilder.append(CP).toString();
        });
    }

    public SqlAndArgs buildSelectK1ColumnByK2Id(K2 k2) {
        return SqlAndArgs.buildSqlWithArgs(argList -> {
            argList.add(k2);
            return selectK1ColumnByK2Id;
        });
    }

    public SqlAndArgs buildSelectK2ColumnByK1Id(K1 k1) {
        return SqlAndArgs.buildSqlWithArgs(argList -> {
            argList.add(k1);
            return selectK2ColumnByK1Id;
        });
    }

    public SqlAndArgs buildDeleteByK1(K1 k1) {
        return SqlAndArgs.buildSqlWithArgs(argList -> {
            argList.add(k1);
            return deleteByK1;
        });
    }

    public SqlAndArgs buildDeleteByK2(K2 k2) {
        return SqlAndArgs.buildSqlWithArgs(argList -> {
            argList.add(k2);
            return deleteByK2;
        });
    }
}
