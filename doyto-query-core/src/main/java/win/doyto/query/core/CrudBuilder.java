package win.doyto.query.core;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import win.doyto.query.entity.Persistable;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.persistence.Id;

import static win.doyto.query.core.CommonUtil.*;
import static win.doyto.query.core.Constant.*;

/**
 * CrudBuilder
 *
 * @author f0rb
 */
@Slf4j
final class CrudBuilder<E extends Persistable<?>> extends QueryBuilder implements SqlBuilder<E> {

    private final Field idField;
    private final List<Field> fields;
    private final int fieldsSize;
    private final String wildInsertValue;   // ?, ?, ?
    private final String insertColumns;
    private final String wildSetClause;     // column1 = ?, column2 = ?

    public CrudBuilder(Class<E> entityClass) {
        super(entityClass);
        idField = FieldUtils.getFieldsWithAnnotation(entityClass, Id.class)[0];

        // init fields
        Field[] allFields = FieldUtils.getAllFields(entityClass);
        List<Field> tempFields = new ArrayList<>(allFields.length);
        Arrays.stream(allFields).filter(CommonUtil::fieldFilter).forEachOrdered(tempFields::add);
        fields = Collections.unmodifiableList(tempFields);
        fieldsSize = fields.size();

        wildInsertValue = wrapWithParenthesis(StringUtils.join(IntStream.range(0, fieldsSize).mapToObj(i -> PLACE_HOLDER).collect(Collectors.toList()), SEPARATOR));

        List<String> columnList = fields.stream().map(CommonUtil::resolveColumn).collect(Collectors.toList());
        insertColumns = wrapWithParenthesis(StringUtils.join(columnList, SEPARATOR));
        wildSetClause = StringUtils.join(columnList.stream().map(c -> c + EQUALS_PLACE_HOLDER).collect(Collectors.toList()), SEPARATOR);

    }

    private static String buildInsertSql(String table, String columns, String fields) {
        StringJoiner insertSql = new StringJoiner(SPACE, 5);
        insertSql.append("INSERT INTO");
        insertSql.append(table);
        insertSql.append(columns);
        insertSql.append("VALUES");
        insertSql.append(fields);
        return insertSql.toString();
    }

    private static String buildUpdateSql(String tableName, String setClauses) {
        StringJoiner updateSql = new StringJoiner(SPACE, 4);
        updateSql.append("UPDATE");
        updateSql.append(tableName);
        updateSql.append("SET");
        updateSql.append(setClauses);
        return updateSql.toString();
    }

    private static void readValueToArgList(List<Field> fields, Object entity, List<Object> argList) {
        fields.stream().map(field -> readFieldGetter(field, entity)).forEach(argList::add);
    }

    private static void readValueToArgList(List<Field> fields, Object entity, List<Object> argList, StringJoiner setClauses) {
        for (Field field : fields) {
            Object o = readFieldGetter(field, entity);
            if (o != null) {
                setClauses.append(resolveColumn(field) + EQUALS_PLACE_HOLDER);
                argList.add(o);
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
                insertSqlBuilder.append(" ON DUPLICATE KEY UPDATE ");
                StringJoiner stringJoiner = new StringJoiner(SEPARATOR, columns.length);
                for (String column : columns) {
                    stringJoiner.append(column + EQUAL + "VALUES (" + column + ")");
                }
                insertSqlBuilder.append(stringJoiner.toString());
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
        StringJoiner setClauses = new StringJoiner(SEPARATOR, fieldsSize);
        readValueToArgList(fields, entity, argList, setClauses);
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
    public SqlAndArgs buildPatchAndArgsWithQuery(E entity, PageQuery query) {
        return SqlAndArgs.buildSqlWithArgs(argList -> {
            String sql = buildPatchAndArgs(entity, argList)
                    + BuildHelper.buildWhere(query, argList);
            return BuildHelper.buildPaging(sql, query);
        });
    }

    @Override
    public SqlAndArgs buildDeleteAndArgs(PageQuery query) {
        return SqlAndArgs.buildSqlWithArgs(argList -> {
            String sql = buildDeleteFromTable(query.toIdWrapper())
                    +  BuildHelper.buildWhere(query, argList);
            return BuildHelper.buildPaging(sql, query);
        });
    }

    @Override
    public SqlAndArgs buildDeleteById(IdWrapper<?> w) {
        return SqlAndArgs.buildSqlWithArgs(argList -> {
            argList.add(w.getId());
            return buildDeleteFromTable(w) + whereId;
        });
    }

    private String buildDeleteFromTable(IdWrapper<?> idWrapper) {
        return "DELETE FROM " + resolveTableName(idWrapper);
    }

    protected String resolveTableName(E e) {
        return resolveTableName(e.toIdWrapper());
    }
}
