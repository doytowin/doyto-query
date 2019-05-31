package win.doyto.query.core;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import win.doyto.query.entity.Persistable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

import static org.apache.commons.lang3.StringUtils.SPACE;

/**
 * CrudBuilder
 *
 * @author f0rb
 */
@SuppressWarnings("squid:CommentedOutCodeLine")
class CrudBuilder<E extends Persistable> extends QueryBuilder {

    private static final String SQL_LOG = "SQL: {}";
    private static final String EQUALS_REPLACE_HOLDER = " = " + REPLACE_HOLDER;

    private final Logger logger;

    private final Field idField;
    private final String idColumn;
    private final String tableName;
    private final List<Field> fields;
    private final int fieldsSize;
    private final boolean isDynamicTable;
    private final String wildInsertValue;   // ?, ?, ?
    private final String insertColumns;
    private final String wildSetClause;     // column1 = ?, column2 = ?

    public CrudBuilder(Class<E> entityClass) {
        logger = LoggerFactory.getLogger(entityClass);
        tableName = entityClass.getAnnotation(Table.class).name();
        idField = FieldUtils.getFieldsWithAnnotation(entityClass, Id.class)[0];
        idColumn = resolveColumn(idField);
        isDynamicTable = CommonUtil.isDynamicTable(tableName);

        // init fields
        Field[] allFields = FieldUtils.getAllFields(entityClass);
        List<Field> tempFields = new ArrayList<>(allFields.length);
        Arrays.stream(allFields).filter(field -> !CommonUtil.ignoreField(field)).forEachOrdered(tempFields::add);
        fields = Collections.unmodifiableList(tempFields);
        fieldsSize = fields.size();

        wildInsertValue = StringUtils.join(IntStream.range(0, fieldsSize).mapToObj(i -> REPLACE_HOLDER).collect(Collectors.toList()), SEPARATOR);

        List<String> columnList = fields.stream().map(CrudBuilder::resolveColumn).collect(Collectors.toList());
        insertColumns = StringUtils.join(columnList, SEPARATOR);
        wildSetClause = StringUtils.join(columnList.stream().map(c -> c + EQUALS_REPLACE_HOLDER).collect(Collectors.toList()), SEPARATOR);

    }

    private static String resolveColumn(Field field) {
        Column column = field.getAnnotation(Column.class);
        if (column != null && !column.name().isEmpty()) {
            return column.name();
        } else {
            return GlobalConfiguration.convertColumn(field.getName());
        }
    }

    private static String buildInsertSql(String table, String columns, String fields) {
        ArrayList<String> insertList = new ArrayList<>();
        insertList.add("INSERT INTO");
        insertList.add(table);
        insertList.add("(" + columns + ")");
        insertList.add("VALUES");
        insertList.add("(" + fields + ")");
        return StringUtils.join(insertList, SPACE);
    }

    private static String buildUpdateSql(String tableName, String setClauses, String whereId) {
        ArrayList<String> updateList = new ArrayList<>();
        updateList.add("UPDATE");
        updateList.add(tableName);
        updateList.add("SET");
        updateList.add(setClauses);
        updateList.add("WHERE");
        updateList.add(whereId);
        return StringUtils.join(updateList, SPACE);
    }

    private static void readValueToArgList(List<Field> fields, Object entity, List<Object> argList) {
        fields.stream().map(field -> CommonUtil.readFieldGetter(field, entity)).forEach(argList::add);
    }

    private static void readValueToArgList(List<Field> fields, Object entity, List<Object> argList, List<String> setClauses) {
        for (Field field : fields) {
            Object o = CommonUtil.readFieldGetter(field, entity);
            if (o != null) {
                setClauses.add(resolveColumn(field) + EQUALS_REPLACE_HOLDER);
                argList.add(o);
            }
        }
    }

    public String buildCreateAndArgs(E entity, List<Object> argList) {
        String table = isDynamicTable ? CommonUtil.replaceTableName(entity, tableName) : tableName;
        readValueToArgList(fields, entity, argList);
        String sql = buildInsertSql(table, insertColumns, wildInsertValue);
        logger.debug(SQL_LOG, sql);
        return sql;
    }

    public String buildUpdateAndArgs(E entity, List<Object> argList) {
        String table = isDynamicTable ? CommonUtil.replaceTableName(entity, tableName) : tableName;
        readValueToArgList(fields, entity, argList);
        argList.add(CommonUtil.readField(idField, entity));
        String sql = buildUpdateSql(table, wildSetClause, idColumn + EQUALS_REPLACE_HOLDER);
        logger.debug(SQL_LOG, sql);
        return sql;
    }

    public String buildPatchAndArgs(E entity, List<Object> argList) {
        String table = isDynamicTable ? CommonUtil.replaceTableName(entity, tableName) : tableName;
        List<String> setClauses = new ArrayList<>(fieldsSize);
        readValueToArgList(fields, entity, argList, setClauses);
        argList.add(CommonUtil.readField(idField, entity));
        String sql = buildUpdateSql(table, StringUtils.join(setClauses, SEPARATOR), idColumn + EQUALS_REPLACE_HOLDER);
        logger.debug(SQL_LOG, sql);
        return sql;
    }
}
