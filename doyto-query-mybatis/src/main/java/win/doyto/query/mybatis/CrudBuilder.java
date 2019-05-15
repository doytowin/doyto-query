package win.doyto.query.mybatis;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import win.doyto.query.core.QueryBuilder;

import java.lang.reflect.Field;
import java.util.*;
import javax.persistence.Column;
import javax.persistence.Table;

/**
 * CrudBuilder
 *
 * @author f0rb
 * @date 2019-05-14
 */
@Slf4j
public class CrudBuilder extends QueryBuilder {

    private static final String SQL_LOG = "SQL: {}";
    private static final String SEPARATOR = ", ";

    private final Map<Class, String> insertSqlMap = new HashMap<>();

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

    private static String resolveField(Field field) {
        return "#{" + field.getName() + "}";
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

    private static String buildInsertSql(Class<?> entityClass) {
        ArrayList<String> fieldList = new ArrayList<>();
        ArrayList<String> columnList = new ArrayList<>();
        LinkedList<Field> fields = resolveDeclaredFields(entityClass);
        for (Field field : fields) {
            if (ignoreField(field)) {
                continue;
            }
            fieldList.add(resolveField(field));
            columnList.add(resolveColumn(field));
        }
        return buildInsertSql(resolveTableName(entityClass), StringUtils.join(columnList, SEPARATOR), "(" + StringUtils.join(fieldList, SEPARATOR) + ")");
    }

    private static String buildUpdateFields(Object entity) {
        List<String> updateFields = new LinkedList<>();
        for (Field field : resolveDeclaredFields(entity.getClass())) {
            if (ignoreField(field)) {
                continue;
            }
            updateFields.add(resolveColumn(field) + " = " + ("#{" + field.getName() + "}"));
        }
        return StringUtils.join(updateFields, ", ");
    }

    public String create(Object entity) {
        String insertSql = insertSqlMap.computeIfAbsent(entity.getClass(), CrudBuilder::buildInsertSql);
        log.debug(SQL_LOG, insertSql);
        return insertSql;
    }

    public String update(Object entity) {
        ArrayList<String> updateList = new ArrayList<>();
        updateList.add("UPDATE");
        updateList.add(resolveTableName(entity.getClass()));
        updateList.add("SET");
        updateList.add(buildUpdateFields(entity));
        updateList.add("WHERE");
        updateList.add("id = #{id}");
        String updateSql = StringUtils.join(updateList, " ");
        log.debug(SQL_LOG, updateSql);
        return updateSql;
    }
}
