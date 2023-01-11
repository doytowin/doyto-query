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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.FieldUtils;
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.core.IdWrapper;
import win.doyto.query.entity.Persistable;
import win.doyto.query.util.ColumnUtil;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import javax.persistence.Id;

import static win.doyto.query.sql.Constant.*;
import static win.doyto.query.util.CommonUtil.*;

/**
 * CrudBuilder
 *
 * @author f0rb
 */
@Slf4j
final class CrudBuilder<E extends Persistable<?>> extends QueryBuilder implements SqlBuilder<E> {

    private final Class<E> entityClass;
    private final Field idField;
    private final List<Field> fields;
    private final String wildInsertValue;   // ?, ?, ?
    private final String insertColumns;
    private final String wildSetClause;     // column1 = ?, column2 = ?

    public CrudBuilder(Class<E> entityClass) {
        super(entityClass);
        this.entityClass = entityClass;
        idField = FieldUtils.getFieldsWithAnnotation(entityClass, Id.class)[0];

        // init fields
        fields = ColumnUtil.getColumnFieldsFrom(entityClass);

        wildInsertValue = fields.stream().map(f -> PLACE_HOLDER).collect(CLT_COMMA_WITH_PAREN);

        List<String> columnList = fields.stream().map(ColumnUtil::resolveColumn).collect(Collectors.toList());
        insertColumns = columnList.stream().collect(CLT_COMMA_WITH_PAREN);
        wildSetClause = columnList.stream().map(c -> c + EQUAL_HOLDER).collect(Collectors.joining(SEPARATOR));

    }

    /**
     * Build insert statements with table name, columns and fields' placeholders
     * <p>
     * Note: Make this method package private for DoytoQL project.
     * </p>
     *
     * @return insert statement with placeholders
     */
    static String buildInsertSql(String table, String columns, String fields) {
        return new StringBuilder(INSERT_INTO)
                .append(table)
                .append(SPACE)
                .append(columns)
                .append(VALUES)
                .append(fields)
                .toString();
    }

    /**
     * Build update statements with table name, columns and fields' placeholders
     * <p>
     * Note: Make this method package private for DoytoQL project.
     * </p>
     *
     * @return update statement with placeholders
     */
    static String buildUpdateSql(String tableName, String setClauses) {
        return new StringJoiner(SPACE)
                .add("UPDATE")
                .add(tableName)
                .add("SET")
                .add(setClauses)
                .toString();
    }

    private static void readValueToArgList(List<Field> fields, Object entity, List<Object> argList) {
        fields.stream().map(field -> readFieldGetter(field, entity)).forEach(argList::add);
    }

    private static void readValueToArgList(List<Field> fields, Object entity, List<Object> argList, StringJoiner setClauses) {
        for (Field field : fields) {
            Object o = readFieldGetter(field, entity);
            if (o != null) {
                setClauses.add(ColumnUtil.resolveColumn(field) + EQUAL_HOLDER);
                argList.add(o);
            }
        }
    }

    private static void readValueToArgList(Object entity, List<Object> argList, StringJoiner setClauses) {
        List<Field> fieldsOfSubClass = Arrays.stream(entity.getClass().getDeclaredFields())
                                             .filter(ColumnUtil::shouldRetain)
                                             .collect(Collectors.toList());

        for (Field field : fieldsOfSubClass) {
            Object value = readFieldGetter(field, entity);
            if (value != null) {
                String setClause = CompoundOperatorsSuffix.mapField(field.getName());
                setClauses.add(setClause);
                argList.add(value);
            }
        }
    }

    @Override
    public SqlAndArgs buildCreateAndArgs(E testEntity) {
        return SqlAndArgs.buildSqlWithArgs(argList -> {
            String table = resolveTableName(testEntity);
            readValueToArgList(fields, testEntity, argList);
            return buildInsertSql(table, replaceHolderInString(testEntity, insertColumns), wildInsertValue);
        });
    }

    @Override
    public SqlAndArgs buildCreateAndArgs(Iterable<E> entities, String... columns) {
        return SqlAndArgs.buildSqlWithArgs(argList -> {
            Iterator<E> iterator = entities.iterator();
            E next = iterator.next();

            String insertSql = buildInsertSql(resolveTableName(next), insertColumns, wildInsertValue);
            StringBuilder insertSqlBuilder = new StringBuilder(insertSql);
            readValueToArgList(fields, next, argList);
            while (iterator.hasNext()) {
                E entity = iterator.next();
                readValueToArgList(fields, entity, argList);
                insertSqlBuilder.append(SEPARATOR).append(wildInsertValue);
            }
            if (columns.length > 0) {
                GlobalConfiguration.dialect().buildInsertUpdate(insertSqlBuilder, columns);
            }

            return replaceHolderInString(next, insertSqlBuilder.toString());
        });
    }

    @Override
    public SqlAndArgs buildUpdateAndArgs(E entity) {
        return SqlAndArgs.buildSqlWithArgs(argList -> {
            String table = resolveTableName(entity);
            readValueToArgList(fields, entity, argList);
            argList.add(readField(idField, entity));
            return buildUpdateSql(table, replaceHolderInString(entity, wildSetClause)) + whereId;
        });
    }

    private String buildPatchAndArgs(E entity, List<Object> argList) {
        String table = resolveTableName(entity);
        StringJoiner setClauses = new StringJoiner(SEPARATOR);
        readValueToArgList(fields, entity, argList, setClauses);
        if (entity.getClass().getSuperclass().equals(entityClass)) {
            readValueToArgList(entity, argList, setClauses);
        }
        String setClausesText = replaceHolderInString(entity, setClauses.toString());
        return buildUpdateSql(table, setClausesText);
    }

    @Override
    public SqlAndArgs buildPatchAndArgsWithId(E entity) {
        return SqlAndArgs.buildSqlWithArgs(argList -> {
            String sql = buildPatchAndArgs(entity, argList) + whereId;
            argList.add(readField(idField, entity));
            return sql;
        });
    }

    @Override
    public SqlAndArgs buildDeleteById(IdWrapper<?> w) {
        return SqlAndArgs.buildSqlWithArgs(argList -> {
            argList.add(w.getId());
            return buildDeleteFromTable(w) + whereId;
        });
    }

    @Override
    public SqlAndArgs buildDeleteByIdIn(IdWrapper<?> w, List<?> ids) {
        return SqlAndArgs.buildSqlWithArgs(argList -> buildDeleteFromTable(w)
                + WHERE + SqlQuerySuffix.In.buildColumnCondition(idColumn, argList, ids));
    }

    @Override
    public SqlAndArgs buildPatchAndArgsWithIds(E entity, List<?> ids) {
        return SqlAndArgs.buildSqlWithArgs(argList -> buildPatchAndArgs(entity, argList)
                + WHERE + SqlQuerySuffix.In.buildColumnCondition(idColumn, argList, ids));
    }

    @Override
    public SqlAndArgs buildDeleteAndArgs(DoytoQuery query) {
        return SqlAndArgs.buildSqlWithArgs(argList -> buildDeleteFromTable(query.toIdWrapper())
                + WHERE + idColumn + IN
                + OP + build(query, argList, idColumn) + CP);
    }

    @Override
    public SqlAndArgs buildPatchAndArgs(E entity, DoytoQuery query) {
        return SqlAndArgs.buildSqlWithArgs(argList -> buildPatchAndArgs(entity, argList)
                + WHERE + idColumn + IN
                + OP + build(query, argList, idColumn) + CP);
    }

    private String buildDeleteFromTable(IdWrapper<?> idWrapper) {
        return DELETE_FROM + resolveTableName(idWrapper);
    }

    private String resolveTableName(E e) {
        return resolveTableName(e.toIdWrapper());
    }
}
