package win.doyto.query.core;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import win.doyto.query.entity.Persistable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * CrudBuilder
 *
 * @author f0rb
 */
@Slf4j
@SuppressWarnings("squid:CommentedOutCodeLine")
public class CrudBuilder<E extends Persistable> extends QueryBuilder {

    private static final String SQL_LOG = "SQL: {}";
    private static final String SEPARATOR = ", ";

    private static final Pattern PTN_$EX = Pattern.compile("\\$\\{(\\w+)}");

    private final Field idField;
    private final String idColumn;
    private final String tableName;
    private final List<Field> fields = new ArrayList<>();
    private final boolean isDynamicTable;
    private final String holderInsertValue; // format: #{filed1}, #{filed2}, #{filed3}
    private final String wildInsertValue;   // ?, ?, ?
    private String insertColumns;
    private final String holderSetClause;   // column1 = #{field1}, column2 = #{field2}
    private final String wildSetClause;     // column1 = ?, column2 = ?

    public CrudBuilder(Class<E> entityClass) {
        tableName = entityClass.getAnnotation(Table.class).name();
        idField = FieldUtils.getFieldsWithAnnotation(entityClass, Id.class)[0];
        idColumn = resolveColumn(idField);

        Arrays.stream(FieldUtils.getAllFields(entityClass))
              .filter(field -> !ignoreField(field)).forEachOrdered(fields::add);

        List<String> columnList = new ArrayList<>();
        List<String> insertFields = new ArrayList<>();
        List<String> insertFields2 = new ArrayList<>();

        List<String> updateFields = new ArrayList<>();
        List<String> updateFields2 = new ArrayList<>();

        for (Field field : fields) {
            String columnName = resolveColumn(field);
            columnList.add(columnName);
            insertFields.add("#{" + field.getName() + "}");
            insertFields2.add("?");

            updateFields.add(columnName + " = " + "#{" + field.getName() + "}");
            updateFields2.add(columnName + " = ?");
        }
        this.insertColumns = StringUtils.join(columnList, SEPARATOR);
        holderInsertValue = StringUtils.join(insertFields, SEPARATOR);
        wildInsertValue = StringUtils.join(insertFields2, SEPARATOR);

        holderSetClause = StringUtils.join(updateFields, SEPARATOR);
        wildSetClause = StringUtils.join(updateFields2, SEPARATOR);

        isDynamicTable = PTN_$EX.matcher(tableName).find();
    }

    public static String replaceTableName(Object entity, String tableName) {
        Matcher matcher = PTN_$EX.matcher(tableName);
        if (!matcher.find()) {
            return tableName;
        }

        StringBuffer sb = new StringBuffer();
        do {
            String fieldName = matcher.group(1);
            matcher = matcher.appendReplacement(sb, String.valueOf(readField(entity, fieldName)));
            log.info(sb.toString());
        } while (matcher.find());
        return sb.toString();
    }

    private static String resolveColumn(Field field) {
        Column column = field.getAnnotation(Column.class);
        if (column != null && !column.name().isEmpty()) {
            return column.name();
        } else {
            return field.getName();
        }
    }

    private static String buildInsertSql(String table, String columns, String fields) {
        ArrayList<String> insertList = new ArrayList<>();
        insertList.add("INSERT INTO");
        insertList.add(table);
        insertList.add("(" + columns + ")");
        insertList.add("VALUES");
        insertList.add("(" + fields + ")");
        String sql = StringUtils.join(insertList, " ");
        log.debug(SQL_LOG, sql);
        return sql;
    }

    private static String buildUpdateSql(String tableName, String setClauses, String whereId) {
        ArrayList<String> updateList = new ArrayList<>();
        updateList.add("UPDATE");
        updateList.add(tableName);
        updateList.add("SET");
        updateList.add(setClauses);
        updateList.add("WHERE");
        updateList.add(whereId);
        String sql = StringUtils.join(updateList, " ");
        log.debug(SQL_LOG, sql);
        return sql;
    }

    private static void readValueToArgList(List<Field> fields, Object entity, List<Object> argList) {
        fields.stream().map(field -> readField(field, entity)).forEach(argList::add);
    }

    public String buildCreate(E entity) {
        return buildCreateAndArgs(entity, null);
    }

    public String buildCreateAndArgs(E entity, List<Object> argList) {
        String table = isDynamicTable ? replaceTableName(entity, tableName) : tableName;
        if (argList != null) {
            readValueToArgList(fields, entity, argList);
            return buildInsertSql(table, insertColumns, wildInsertValue);
        }
        return buildInsertSql(table, insertColumns, holderInsertValue);
    }

    public String buildUpdate(E entity) {
        return buildUpdateAndArgs(entity, null);
    }

    public String buildUpdateAndArgs(E entity, List<Object> argList) {
        String table = isDynamicTable ? replaceTableName(entity, tableName) : tableName;
        String setClauses;
        String whereId;
        if (argList == null) {
            setClauses = holderSetClause;
            whereId = "#{" + idField.getName() + "}";
        } else {
            setClauses = wildSetClause;
            readValueToArgList(fields, entity, argList);
            argList.add(readField(idField, entity));
            whereId = "?";
        }
        return buildUpdateSql(table, setClauses, idColumn + " = " + whereId);
    }

}
