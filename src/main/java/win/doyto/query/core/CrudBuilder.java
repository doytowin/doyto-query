package win.doyto.query.core;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import win.doyto.query.entity.Persistable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
public class CrudBuilder<E extends Persistable> extends QueryBuilder {

    private static final String SQL_LOG = "SQL: {}";

    private static final Pattern PTN_$EX = Pattern.compile("\\$\\{(\\w+)}");

    private final Logger logger;

    private final Field idField;
    private final String idColumn;
    private final String tableName;
    private final List<Field> fields = new ArrayList<>();
    private final boolean isDynamicTable;
    private final String wildInsertValue;   // ?, ?, ?
    private String insertColumns;
    private final String wildSetClause;     // column1 = ?, column2 = ?

    public CrudBuilder(Class<E> entityClass) {
        logger = LoggerFactory.getLogger(entityClass);
        tableName = entityClass.getAnnotation(Table.class).name();
        idField = FieldUtils.getFieldsWithAnnotation(entityClass, Id.class)[0];
        idColumn = resolveColumn(idField);

        Arrays.stream(FieldUtils.getAllFields(entityClass))
              .filter(field -> !ignoreField(field)).forEachOrdered(fields::add);

        List<String> columnList = new ArrayList<>();
        List<String> insertFields2 = new ArrayList<>();

        List<String> updateFields2 = new ArrayList<>();

        for (Field field : fields) {
            String columnName = resolveColumn(field);
            columnList.add(columnName);
            insertFields2.add("?");

            updateFields2.add(columnName + " = ?");
        }
        this.insertColumns = StringUtils.join(columnList, SEPARATOR);
        wildInsertValue = StringUtils.join(insertFields2, SEPARATOR);

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
        } while (matcher.find());
        return sb.toString();
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
        fields.stream().map(field -> readFieldGetter(field, entity)).forEach(argList::add);
    }

    private static void readValueToArgList(List<Field> fields, Object entity, List<Object> argList, List<String> setClauses) {
        for (Field field : fields) {
            Object o = readFieldGetter(field, entity);
            if (o != null) {
                setClauses.add(resolveColumn(field) + " = ?");
                argList.add(o);
            }
        }
    }

    public String buildCreateAndArgs(E entity, List<Object> argList) {
        String table = isDynamicTable ? replaceTableName(entity, tableName) : tableName;
        readValueToArgList(fields, entity, argList);
        String sql = buildInsertSql(table, insertColumns, wildInsertValue);
        logger.debug(SQL_LOG, sql);
        return sql;
    }

    public String buildUpdateAndArgs(E entity, List<Object> argList) {
        String table = isDynamicTable ? replaceTableName(entity, tableName) : tableName;
        String setClauses;
        setClauses = wildSetClause;
        readValueToArgList(fields, entity, argList);
        argList.add(readField(idField, entity));
        String sql = buildUpdateSql(table, setClauses, idColumn + " = ?");
        logger.debug(SQL_LOG, sql);
        return sql;
    }

    public String buildPatchAndArgs(E entity, List<Object> argList) {
        String table = isDynamicTable ? replaceTableName(entity, tableName) : tableName;
        List<String> setClauses = new LinkedList<>();
        readValueToArgList(fields, entity, argList, setClauses);
        argList.add(readField(idField, entity));
        String sql = buildUpdateSql(table, StringUtils.join(setClauses, SEPARATOR), idColumn + " = ?");
        logger.debug(SQL_LOG, sql);
        return sql;
    }
}
