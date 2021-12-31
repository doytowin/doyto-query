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

package win.doyto.query.sql;

import lombok.Getter;

import java.util.List;
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
    private final String tableName;
    private final String k1Column;
    private final String k2Column;

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
    private String deleteIn;
    private String countIn;

    public AssociationSqlBuilder(String tableName, String k1Column, String k2Column) {
        this.tableName = tableName;
        this.k1Column = k1Column;
        this.k2Column = k2Column;

        selectK1ColumnByK2Id = SELECT + k1Column + " FROM " + tableName + WHERE + k2Column + " = ?";
        selectK2ColumnByK1Id = SELECT + k2Column + " FROM " + tableName + WHERE + k1Column + " = ?";
        deleteByK1 = DELETE_FROM + tableName + WHERE + k1Column + " = ?";
        deleteByK2 = DELETE_FROM + tableName + WHERE + k2Column + " = ?";

        insertSql = "INSERT INTO " + tableName + " (" + k1Column + ", " + k2Column + ") VALUES ";
        placeHolders = "(?, ?)";
        deleteIn = DELETE_FROM + tableName + WHERE + "(" + k1Column + ", " + k2Column + ") IN ";
        countIn = SELECT + "COUNT(*)" + FROM + tableName + WHERE + "(" + k1Column + ", " + k2Column + ") IN ";
    }

    private void buildPlaceHolders(StringBuilder sb, int size) {
        sb.append(IntStream.range(0, size).mapToObj(i -> placeHolders).collect(Collectors.joining(SEPARATOR)));
    }

    public SqlAndArgs buildInsert(List<UniqueKey<K1, K2>> keys) {
        return SqlAndArgs.buildSqlWithArgs(argList -> {
            keys.stream().map(UniqueKey::toList).forEach(argList::addAll);
            StringBuilder insertBuilder = new StringBuilder(insertSql);
            buildPlaceHolders(insertBuilder, keys.size());
            return insertBuilder.toString();
        });
    }

    public SqlAndArgs buildDelete(List<UniqueKey<?, ?>> keys) {
        return SqlAndArgs.buildSqlWithArgs(argList -> {
            keys.stream().map(UniqueKey::toList).forEach(argList::addAll);
            StringBuilder deleteBuilder = new StringBuilder(deleteIn).append("(");
            buildPlaceHolders(deleteBuilder, keys.size());
            return deleteBuilder.append(")").toString();
        });
    }

    public SqlAndArgs buildCount(List<UniqueKey<?, ?>> keys) {
        return SqlAndArgs.buildSqlWithArgs(argList -> {
            keys.stream().map(UniqueKey::toList).forEach(argList::addAll);
            StringBuilder stringBuilder = new StringBuilder(countIn).append("(");
            buildPlaceHolders(stringBuilder, keys.size());
            return stringBuilder.append(")").toString();
        });
    }
}
