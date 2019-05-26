package win.doyto.query.core;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.util.*;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * CrudBuilder
 *
 * @author f0rb
 */
@Slf4j
public class CrudBuilder extends QueryBuilder {

    private static final String SQL_LOG = "SQL: {}";
    private static final String SEPARATOR = ", ";

    private static final Map<Class, String> insertSqlMap = new HashMap<>();
    private static final Map<Class, Field> idFieldMap = new HashMap<>();

    private static String resolveTableName(Class<?> clazz) {
        try {
            return clazz.getAnnotation(Table.class).name();
        } catch (Exception e) {
            return clazz.getSimpleName();
        }
    }

    private static String resolveColumn(Field field) {
        Column column = field.getAnnotation(Column.class);
        if (column != null && !column.name().isEmpty()) {
            return column.name();
        } else {
            return field.getName();
        }
    }

    private static String resolveField(Object entity, Field field, List<Object> argList) {
        if (argList == null) {
            return "#{" + field.getName() + "}";
        } else {
            argList.add(readField(field, entity));
            return "?";
        }
    }

    private static LinkedList<Field> resolveDeclaredFields(Class<?> entityClass) {
        LinkedList<Field> fields = new LinkedList<>();
        Class<?> clazz = entityClass;
        while (!clazz.equals(Object.class)) {
            Collections.addAll(fields, clazz.getDeclaredFields());
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    private static String buildInsertSql(String table, String columns, String fields) {
        ArrayList<String> insertList = new ArrayList<>();
        insertList.add("INSERT INTO");
        insertList.add(table);
        insertList.add("(" + columns + ")");
        insertList.add("VALUES");
        insertList.add(fields);
        return StringUtils.join(insertList, " ");
    }

    private static String buildInsertSql(Object entity, List<Object> argList) {
        Class<?> entityClass = entity.getClass();
        ArrayList<String> fieldList = new ArrayList<>();
        ArrayList<String> columnList = new ArrayList<>();
        LinkedList<Field> fields = resolveDeclaredFields(entityClass);
        for (Field field : fields) {
            if (ignoreField(field)) {
                continue;
            }
            columnList.add(resolveColumn(field));
            fieldList.add(resolveField(entity, field, argList));
        }
        return buildInsertSql(resolveTableName(entityClass), StringUtils.join(columnList, SEPARATOR), "(" + StringUtils.join(fieldList, SEPARATOR) + ")");
    }

    private static String buildUpdateFields(Object entity, List<Object> argList) {
        List<String> updateFields = new LinkedList<>();
        for (Field field : resolveDeclaredFields(entity.getClass())) {
            if (ignoreField(field)) {
                continue;
            }
            updateFields.add(resolveColumn(field) + " = " + resolveField(entity, field, argList));
        }
        return StringUtils.join(updateFields, ", ");
    }

    public String create(Object entity) {
        return buildCreateAndArgs(entity, null);
    }

    public String buildCreateAndArgs(Object entity, List<Object> argList) {
        Class<?> entityClass = entity.getClass();
        if (!insertSqlMap.containsKey(entityClass)) {
            insertSqlMap.put(entityClass, CrudBuilder.buildInsertSql(entity, argList));
        }
        String insertSql = insertSqlMap.get(entityClass);
        log.debug(SQL_LOG, insertSql);
        return insertSql;
    }

    public String update(Object entity) {
        return buildUpdateAndArgs(entity, null);
    }

    public String buildUpdateAndArgs(Object entity, List<Object> argList) {
        Class<?> entityClass = entity.getClass();
        ArrayList<String> updateList = new ArrayList<>();
        updateList.add("UPDATE");
        updateList.add(resolveTableName(entityClass));
        updateList.add("SET");
        updateList.add(buildUpdateFields(entity, argList));
        updateList.add("WHERE");
        if (!idFieldMap.containsKey(entityClass)) {
            idFieldMap.put(entityClass, FieldUtils.getFieldsWithAnnotation(entityClass, Id.class)[0]);
        }
        Field id = idFieldMap.get(entityClass);

        updateList.add(getEqualsSql(entity, argList, id));
        String updateSql = StringUtils.join(updateList, " ");
        log.debug(SQL_LOG, updateSql);
        return updateSql;
    }

    private static String getEqualsSql(Object entity, List<Object> argList, Field id) {
        return resolveColumn(id) + " = " + resolveField(entity, id, argList);
    }
}
