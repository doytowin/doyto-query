package win.doyto.query.core;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import win.doyto.query.entity.Persistable;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

import static org.apache.commons.lang3.StringUtils.SPACE;
import static win.doyto.query.core.CommonUtil.*;

/**
 * CrudBuilder
 *
 * @author f0rb
 */
@SuppressWarnings("squid:CommentedOutCodeLine")
@Slf4j
final class CrudBuilder<E extends Persistable> extends QueryBuilder {

    private static final String EQUALS_REPLACE_HOLDER = " = " + REPLACE_HOLDER;

    private final Field idField;
    private final String tableName;
    private final List<Field> fields;
    private final int fieldsSize;
    private final boolean isDynamicTable;
    private final String wildInsertValue;   // ?, ?, ?
    private final String insertColumns;
    private final String wildSetClause;     // column1 = ?, column2 = ?
    private final String whereId;

    public CrudBuilder(Class<E> entityClass) {
        tableName = entityClass.getAnnotation(Table.class).name();
        idField = FieldUtils.getFieldsWithAnnotation(entityClass, Id.class)[0];
        whereId = " WHERE " + resolveColumn(idField) + EQUALS_REPLACE_HOLDER;

        isDynamicTable = isDynamicTable(tableName);

        // init fields
        Field[] allFields = FieldUtils.getAllFields(entityClass);
        List<Field> tempFields = new ArrayList<>(allFields.length);
        Arrays.stream(allFields).filter(field -> !ignoreField(field)).forEachOrdered(tempFields::add);
        fields = Collections.unmodifiableList(tempFields);
        fieldsSize = fields.size();

        wildInsertValue = wrapWithParenthesis(StringUtils.join(IntStream.range(0, fieldsSize).mapToObj(i -> REPLACE_HOLDER).collect(Collectors.toList()), SEPARATOR));

        List<String> columnList = fields.stream().map(CrudBuilder::resolveColumn).collect(Collectors.toList());
        insertColumns = wrapWithParenthesis(StringUtils.join(columnList, SEPARATOR));
        wildSetClause = StringUtils.join(columnList.stream().map(c -> c + EQUALS_REPLACE_HOLDER).collect(Collectors.toList()), SEPARATOR);

    }

    static String resolveColumn(Field field) {
        Column column = field.getAnnotation(Column.class);
        if (column != null && !column.name().isEmpty()) {
            return column.name();
        } else {
            return convertColumn(field.getName());
        }
    }

    private static String buildInsertSql(String table, String columns, String fields) {
        ArrayList<String> insertList = new ArrayList<>();
        insertList.add("INSERT INTO");
        insertList.add(table);
        insertList.add(columns);
        insertList.add("VALUES");
        insertList.add(fields);
        return StringUtils.join(insertList, SPACE);
    }

    private static String buildUpdateSql(String tableName, String setClauses) {
        ArrayList<String> updateList = new ArrayList<>();
        updateList.add("UPDATE");
        updateList.add(tableName);
        updateList.add("SET");
        updateList.add(setClauses);
        return StringUtils.join(updateList, SPACE);
    }

    private static void readValueToArgList(List<Field> fields, Object entity, List<Object> argList) {
        fields.stream().map(field -> readFieldGetter(field, entity)).forEach(argList::add);
    }

    private static void readValueToArgList(List<Field> fields, Object entity, List<Object> argList, List<String> setClauses) {
        for (Field field : fields) {
            Object o = readFieldGetter(field, entity);
            if (o != null) {
                setClauses.add(resolveColumn(field) + EQUALS_REPLACE_HOLDER);
                argList.add(o);
            }
        }
    }

    private String resolveTableName(E entity) {
        return isDynamicTable ? replaceTableName(entity, tableName) : tableName;
    }

    public String buildCreateAndArgs(E entity, List<Object> argList) {
        String table = resolveTableName(entity);
        readValueToArgList(fields, entity, argList);
        return buildInsertSql(table, insertColumns, wildInsertValue);
    }

    public SqlAndArgs buildCreateAndArgs(Iterable<E> entities) {
        Iterator<E> iterator = entities.iterator();
        E next = iterator.next();
        String table = resolveTableName(next);
        StringBuilder insertSql = new StringBuilder(buildInsertSql(table, insertColumns, wildInsertValue));

        ArrayList<Object> argList = new ArrayList<>();
        readValueToArgList(fields, next, argList);
        while (iterator.hasNext()) {
            E entity = iterator.next();
            readValueToArgList(fields, entity, argList);
            insertSql.append(", ").append(wildInsertValue);
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
        List<String> setClauses = new ArrayList<>(fieldsSize);
        readValueToArgList(fields, entity, argList, setClauses);
        return buildUpdateSql(table, StringUtils.join(setClauses, SEPARATOR));
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

    public String buildSelectById() {
        return "SELECT * FROM " + tableName + whereId;
    }

    public String buildDeleteById() {
        return "DELETE FROM " + tableName + whereId;
    }

    protected String buildSelectById(E e) {
        return "SELECT * FROM " + replaceTableName(e, tableName) + whereId;
    }

    protected String buildDeleteById(E e) {
        return "DELETE FROM " + replaceTableName(e, tableName) + whereId;
    }
}
