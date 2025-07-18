/*
 * Copyright © 2019-2025 DoytoWin, Inc.
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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import win.doyto.query.core.CompositeId;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.core.IdWrapper;
import win.doyto.query.util.ColumnUtil;
import win.doyto.query.util.CommonUtil;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static win.doyto.query.sql.Constant.*;
import static win.doyto.query.util.CommonUtil.isDynamicTable;
import static win.doyto.query.util.CommonUtil.replaceHolderInString;

/**
 * QueryBuilder
 *
 * @author f0rb
 */
@Slf4j
public class QueryBuilder {

    protected final String tableName;
    protected final String idColumn;
    protected final String wrappedIdColumn;
    protected final String whereId;
    private final BiFunction<IdWrapper<?>, String, String> resolveTableNameFunc;

    public QueryBuilder(String tableName, String[] idColumns) {
        this.tableName = tableName;
        this.idColumn = String.join(SEPARATOR, idColumns);
        this.wrappedIdColumn = idColumns.length > 1 ? OP + idColumn + CP : idColumn;
        this.whereId = WHERE + Arrays.stream(idColumns).map(c -> c + EQUAL_HOLDER).collect(Collectors.joining(AND));
        this.resolveTableNameFunc = isDynamicTable(tableName) ? CommonUtil::replaceHolderInString : (idWrapper, tableName1) -> tableName1;
    }

    public QueryBuilder(Class<?> entityClass) {
        this(BuildHelper.resolveTableName(entityClass), ColumnUtil.resolveIdColumn(entityClass));
    }

    protected String resolveTableName(IdWrapper<?> idWrapper) {
        return resolveTableNameFunc.apply(idWrapper, tableName);
    }

    protected String build(DoytoQuery query, List<Object> argList, String... columns) {
        String sql = BuildHelper.buildStart(columns, resolveTableName(query.toIdWrapper()));
        sql = replaceHolderInString(query, sql);
        if (query instanceof WrappedQuery wq) {
            SqlAndArgs saa = wq.getSaa();
            if (saa != null) {
                sql += saa.getSql();
                argList.addAll(Arrays.asList(saa.getArgs()));
            } else {
                String where = buildWhere(wq.getDeleget(), argList);
                sql += where;
                wq.setSaa(new SqlAndArgs(where, argList));
            }
        } else {
            sql += buildWhere(query, argList);
        }
        if (!(columns.length == 1 && COUNT.equals(columns[0]))) {
            // not SELECT COUNT(*)
            sql += BuildHelper.buildOrderBy(query);
            sql += BuildHelper.buildLock(query);
            sql = BuildHelper.buildPaging(sql, query);
        }
        return sql;
    }

    protected String buildWhere(DoytoQuery query, List<Object> argList) {
        return BuildHelper.buildWhere(query, argList);
    }

    String buildSelectAndArgs(DoytoQuery query, List<Object> argList) {
        return build(query, argList, "*");
    }

    public SqlAndArgs buildCountAndArgs(DoytoQuery query) {
        return SqlAndArgs.buildSqlWithArgs(argList -> build(query, argList, COUNT));
    }

    public SqlAndArgs buildSelectColumnsAndArgs(DoytoQuery query, String... columns) {
        return SqlAndArgs.buildSqlWithArgs(argList -> build(query, argList, columns));
    }

    public SqlAndArgs buildSelectById(IdWrapper<?> idWrapper, String... columns) {
        return SqlAndArgs.buildSqlWithArgs(argList -> {
            appendArgsForId(argList, idWrapper.getId());
            String columnStr = buildColumnStr(idWrapper, columns);
            String table = resolveTableName(idWrapper);
            return SELECT + columnStr + FROM + table + whereId;
        });
    }

    protected void appendArgsForId(List<Object> argList, Object id) {
        if (id instanceof CompositeId compositeId) {
            argList.addAll(compositeId.toKeys());
        } else {
            argList.add(id);
        }
    }

    private String buildColumnStr(IdWrapper<?> idWrapper, String[] columns) {
        return replaceHolderInString(idWrapper, StringUtils.join(columns, SEPARATOR));
    }

    public SqlAndArgs buildSelectIdAndArgs(DoytoQuery query) {
        return buildSelectColumnsAndArgs(query, idColumn);
    }

}
