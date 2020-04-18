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
final class CrudBuilder<E extends Persistable<?>> extends QueryBuilder {

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

        wildInsertValue = wrapWithParenthesis(StringUtils.join(IntStream.range(0, fieldsSize).mapToObj(i -> REPLACE_HOLDER).collect(Collectors.toList()), SEPARATOR));

        List<String> columnList = fields.stream().map(CommonUtil::resolveColumn).collect(Collectors.toList());
        insertColumns = wrapWithParenthesis(StringUtils.join(columnList, SEPARATOR));
        wildSetClause = StringUtils.join(columnList.stream().map(c -> c + EQUALS_REPLACE_HOLDER).collect(Collectors.toList()), SEPARATOR);

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
                setClauses.append(resolveColumn(field) + EQUALS_REPLACE_HOLDER);
                argList.add(o);
            }
        }
    }

    public String buildCreateAndArgs(E entity, List<Object> argList) {
        String table = resolveTableName(entity);
        readValueToArgList(fields, entity, argList);
        return buildInsertSql(table, insertColumns, wildInsertValue);
    }

    public SqlAndArgs buildCreateAndArgs(Iterable<E> entities, String... columns) {

        ArrayList<Object> argList = new ArrayList<>();
        Iterator<E> iterator = entities.iterator();
        E next = iterator.next();

        StringBuilder insertSql = new StringBuilder(buildInsertSql(resolveTableName(next.toIdWrapper()), insertColumns, wildInsertValue));
        readValueToArgList(fields, next, argList);
        while (iterator.hasNext()) {
            E entity = iterator.next();
            readValueToArgList(fields, entity, argList);
            insertSql.append(SEPARATOR).append(wildInsertValue);
        }
        if (columns.length > 0) {
            insertSql.append(" ON DUPLICATE KEY UPDATE ");
            StringJoiner stringJoiner = new StringJoiner(SEPARATOR, columns.length);
            for (String column : columns) {
                stringJoiner.append(column + EQUAL + "VALUES (" + column + ")");
            }
            insertSql.append(stringJoiner.toString());
        }

        return new SqlAndArgs(insertSql.toString(), argList);
    }

    public String buildUpdateAndArgs(E entity, List<Object> argList) {
        String table = resolveTableName(entity);
        readValueToArgList(fields, entity, argList);
        argList.add(readField(idField, entity));
        return buildUpdateSql(table, wildSetClause) + whereId;
    }

    public SqlAndArgs buildUpdateAndArgs(E entity) {
        ArrayList<Object> argList = new ArrayList<>();
        return new SqlAndArgs(buildUpdateAndArgs(entity, argList), argList);
    }

    public String buildPatchAndArgs(E entity, List<Object> argList) {
        String table = resolveTableName(entity);
        StringJoiner setClauses = new StringJoiner(SEPARATOR, fieldsSize);
        readValueToArgList(fields, entity, argList, setClauses);
        return buildUpdateSql(table, setClauses.toString());
    }

    public String buildPatchAndArgsWithId(E entity, List<Object> argList) {
        String sql = buildPatchAndArgs(entity, argList) + whereId;
        argList.add(readField(idField, entity));
        return sql;
    }

    public SqlAndArgs buildPatchAndArgsWithId(E entity) {
        ArrayList<Object> argList = new ArrayList<>();
        return new SqlAndArgs(buildPatchAndArgsWithId(entity, argList), argList);
    }

    public String buildPatchAndArgsWithQuery(E entity, Object query, List<Object> argList) {
        String sql = buildPatchAndArgs(entity, argList);
        return buildWhere(sql, query, argList);
    }

    public SqlAndArgs buildPatchAndArgsWithQuery(E entity, Object query) {
        ArrayList<Object> argList = new ArrayList<>();
        String sql = buildPatchAndArgsWithQuery(entity, query, argList);
        return new SqlAndArgs(sql, argList);
    }

    public String buildDeleteAndArgs(PageQuery query, List<Object> argList) {
        return build(query, argList, "DELETE", new String[0], tableName);
    }

    public SqlAndArgs buildDeleteAndArgs(PageQuery query) {
        ArrayList<Object> argList = new ArrayList<>();
        return new SqlAndArgs(buildDeleteAndArgs(query, argList), argList);
    }

    protected String buildDeleteById(IdWrapper<?> w) {
        return "DELETE FROM " + resolveTableName(w) + whereId;
    }

    protected String resolveTableName(E e) {
        return resolveTableName(e.toIdWrapper());
    }
}
