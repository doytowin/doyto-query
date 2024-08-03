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

package win.doyto.query.sql;

import lombok.extern.slf4j.Slf4j;
import win.doyto.query.annotation.Clause;
import win.doyto.query.annotation.Column;
import win.doyto.query.annotation.DomainPath;
import win.doyto.query.annotation.Id;
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.core.IdWrapper;
import win.doyto.query.core.OptimisticLock;
import win.doyto.query.entity.Persistable;
import win.doyto.query.sql.field.SqlQuerySuffix;
import win.doyto.query.util.ColumnUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static win.doyto.query.sql.Constant.*;
import static win.doyto.query.util.ColumnUtil.filterFields;
import static win.doyto.query.util.CommonUtil.*;

/**
 * CrudBuilder
 *
 * @author f0rb
 */
@Slf4j
public class CrudBuilder<E extends Persistable<?>> extends QueryBuilder implements SqlBuilder<E> {

    public static final String DEFAULT_VERSION_COLUMN = "entity_version";

    private final Class<E> entityClass;
    private final List<Field> insertFields;
    private final List<Field> updateFields;
    private final String wildInsertValue;   // ?, ?, ?
    private final String insertColumns;
    private final String wildSetClause;     // column1 = ?, column2 = ?
    private final String versionColumn;

    public CrudBuilder(Class<E> entityClass) {
        super(entityClass);
        this.entityClass = entityClass;

        // init fields
        insertFields = ColumnUtil.getColumnFieldsFrom(entityClass);
        wildInsertValue = insertFields.stream().map(f -> PLACE_HOLDER).collect(CLT_COMMA_WITH_PAREN);
        insertColumns = insertFields.stream().map(ColumnUtil::resolveColumn).collect(CLT_COMMA_WITH_PAREN);

        versionColumn = resolveVersionColumn(entityClass);
        updateFields = buildUpdateFields(entityClass, versionColumn);
        wildSetClause = buildSetClause(entityClass, updateFields, versionColumn);
    }

    private static String resolveVersionColumn(Class<?> entityClass) {
        String column = null;
        if (OptimisticLock.class.isAssignableFrom(entityClass)) {
            try {
                Method currentVersionMethod = entityClass.getMethod("currentVersion");
                Column columnAnno = currentVersionMethod.getAnnotation(Column.class);
                if (columnAnno == null || columnAnno.name().equals("")) {
                    column = DEFAULT_VERSION_COLUMN;
                } else {
                    column = columnAnno.name();
                }
            } catch (NoSuchMethodException e) {
                throw new InternalError();
            }
        }
        return column;
    }

    private static List<Field> buildUpdateFields(Class<?> entityClass, String versionColumn) {
        return filterFields(entityClass, field -> ColumnUtil.shouldRetain(field)
                && !field.isAnnotationPresent(Id.class)
                && !field.isAnnotationPresent(DomainPath.class)
                && !ColumnUtil.convertColumn(field.getName()).equals(versionColumn)
        ).toList();
    }

    private static String buildSetClause(Class<?> entityClass, List<Field> updateFields, String versionColumn) {
        String tempSetClause = updateFields.stream()
                                           .map(ColumnUtil::resolveColumn)
                                           .map(c -> c + EQUAL_HOLDER)
                                           .collect(Collectors.joining(SEPARATOR));

        if (OptimisticLock.class.isAssignableFrom(entityClass)) {
            tempSetClause += SEPARATOR + versionColumn + EQUAL + versionColumn + " + 1";
        }
        return tempSetClause;
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
        return INSERT_INTO + table + SPACE + columns + VALUES + fields;
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
        return "UPDATE " + tableName + " SET " + setClauses;
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
                                             .filter(ColumnUtil::shouldRetain).toList();

        for (Field field : fieldsOfSubClass) {
            Object value = readFieldGetter(field, entity);
            if (value != null) {
                String setClause;
                if (field.isAnnotationPresent(Clause.class)) {
                    setClause = field.getAnnotation(Clause.class).value();
                } else {
                    setClause = CompoundOperatorsSuffix.mapField(field.getName());
                }
                setClauses.add(setClause);
                argList.add(value);
            }
        }
    }

    @Override
    public SqlAndArgs buildCreateAndArgs(E testEntity) {
        return SqlAndArgs.buildSqlWithArgs(argList -> {
            String table = resolveTableName(testEntity);
            readValueToArgList(insertFields, testEntity, argList);
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
            readValueToArgList(insertFields, next, argList);
            while (iterator.hasNext()) {
                E entity = iterator.next();
                readValueToArgList(insertFields, entity, argList);
                insertSqlBuilder.append(SEPARATOR).append(wildInsertValue);
            }
            if (columns.length > 0) {
                GlobalConfiguration.dialect().buildInsertUpdate(insertSqlBuilder, columns);
            }

            String insert = replaceHolderInString(next, insertSqlBuilder.toString());
            return GlobalConfiguration.dialect().alterBatchInsert(insert);
        });
    }

    private String addVersion(E entity, String sql, List<Object> argList) {
        if (entity instanceof OptimisticLock lock &&lock.currentVersion() != null) {
            sql += AND + versionColumn + EQUAL_HOLDER;
            appendArgsForId(argList, lock.currentVersion());
        }
        return sql;
    }

    @Override
    public SqlAndArgs buildUpdateAndArgs(E entity) {
        return SqlAndArgs.buildSqlWithArgs(argList -> {
            String table = resolveTableName(entity);
            readValueToArgList(updateFields, entity, argList);
            appendArgsForId(argList, entity.getId());
            String sql = buildUpdateSql(table, replaceHolderInString(entity, wildSetClause)) + whereId;
            return addVersion(entity, sql, argList);
        });
    }

    private String buildPatchAndArgs(E entity, List<Object> argList) {
        String table = resolveTableName(entity);
        StringJoiner setClauses = new StringJoiner(SEPARATOR);
        if (entity instanceof OptimisticLock) {
            setClauses.add(versionColumn + EQUAL + versionColumn + " + 1");
        }
        readValueToArgList(updateFields, entity, argList, setClauses);
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
            appendArgsForId(argList, entity.getId());
            return addVersion(entity, sql, argList);
        });
    }

    @Override
    public SqlAndArgs buildDeleteById(IdWrapper<?> w) {
        return SqlAndArgs.buildSqlWithArgs(argList -> {
            appendArgsForId(argList, w.getId());
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
                + WHERE + wrappedIdColumn + IN + OP
                + GlobalConfiguration.dialect().wrapSelectForUpdate(build(query, argList, idColumn), wrappedIdColumn)
                + CP);
    }

    @Override
    public SqlAndArgs buildPatchAndArgs(E entity, DoytoQuery query) {
        return SqlAndArgs.buildSqlWithArgs(argList -> buildPatchAndArgs(entity, argList)
                + WHERE + wrappedIdColumn + IN + OP
                + GlobalConfiguration.dialect().wrapSelectForUpdate(build(query, argList, idColumn), wrappedIdColumn)
                + CP);
    }

    private String buildDeleteFromTable(IdWrapper<?> idWrapper) {
        return DELETE_FROM + resolveTableName(idWrapper);
    }

    private String resolveTableName(E e) {
        return resolveTableName(e.toIdWrapper());
    }
}
