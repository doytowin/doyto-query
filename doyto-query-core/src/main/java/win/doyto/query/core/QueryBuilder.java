package win.doyto.query.core;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.persistence.Id;
import javax.persistence.Transient;

/**
 * QueryBuilder
 *
 * @author f0rb
 */
@Slf4j
public class QueryBuilder {

    private static String build(DatabaseOperation operation, Object query, List<Object> argList) {
        QueryTable queryTable = query.getClass().getAnnotation(QueryTable.class);
        String table = queryTable.table();
        String sql;
        if (operation == DatabaseOperation.COUNT) {
            sql = "SELECT count(*) FROM " + table;
        } else if (operation == DatabaseOperation.DELETE) {
            sql = "DELETE FROM " + table;
        } else {
            sql = "SELECT * FROM " + table;
        }

        sql = buildWhere(sql, query, argList);

        if (operation == DatabaseOperation.SELECT && query instanceof PageQuery) {
            PageQuery pageQuery = (PageQuery) query;
            if (pageQuery.getSort() != null) {
                sql += " ORDER BY " + pageQuery.getSort().replaceAll(",", " ").replaceAll(";", ", ");
            }
        }
        if (operation != DatabaseOperation.COUNT && query instanceof PageQuery) {
            PageQuery pageQuery = (PageQuery) query;
            if (pageQuery.needPaging()) {
                sql += " LIMIT " + pageQuery.getPageSize();
                if (operation == DatabaseOperation.SELECT) {
                   sql += " OFFSET " + pageQuery.getOffset();
                }
            }
        }
        log.info("SQL: {}", sql);
        return sql;
    }

    private static String buildWhere(String sql, Object query, List<Object> argList) {
        LinkedList<Object> whereList = new LinkedList<>();
        for (Field field : query.getClass().getDeclaredFields()) {
            String fieldName = field.getName();
            if (fieldName.startsWith("$")) {
                continue;
            }
            Object value = readFieldGetter(field, query);
            if (value != null) {
                if (sql.contains("${" + fieldName + "}")) {
                    sql = sql.replaceAll("\\$\\{" + fieldName + "}", String.valueOf(value));
                } else {
                    processField(value, field, whereList, argList);
                }
            }
        }
        String where = "";
        if (!whereList.isEmpty()) {
            where = " WHERE " + StringUtils.join(whereList, " AND ");
        }
        return sql + where;
    }

    private static void processField(Object value, Field field, LinkedList<Object> whereList, List<Object> argList) {
        QueryField queryField = field.getAnnotation(QueryField.class);
        NestedQuery nestedQuery = field.getAnnotation(NestedQuery.class);
        String andSQL;
        if (queryField != null) {
            andSQL = replaceArgs(value, argList, queryField.and());
        } else if (nestedQuery != null) {
            andSQL = resolveNestedQuery(nestedQuery, argList, field.getName());
            if (argList != null) {
                argList.add(value);
            }
        } else {
            String fieldName = field.getName();
            andSQL = QuerySuffix.buildAndSql(fieldName, value, argList);
        }
        whereList.add(andSQL);
    }

    static String resolveNestedQuery(NestedQuery nestedQuery, List<Object> argList, String fieldName) {
        String subquery = "id in (SELECT " + nestedQuery.left() + " FROM " + nestedQuery.table() + " WHERE " + nestedQuery.right();
        return subquery + " = " + ColumnMeta.getEx(argList, fieldName) + ")";
    }

    private static final Pattern PLACE_HOLDER_PTN = Pattern.compile("#\\{\\w+}");
    private static String replaceArgs(Object value, List<Object> argList, String andSQL) {
        if (argList == null) {
            return andSQL;
        }
        Matcher matcher = PLACE_HOLDER_PTN.matcher(andSQL);
        while (matcher.find()) {
            argList.add(value);
        }
        return matcher.replaceAll("?");
    }

    public static Object readFieldGetter(Field field, Object target) {
        Object value;
        try {
            String fieldName = field.getName();
            String prefix = field.getType().isAssignableFrom(boolean.class) ? "is" : "get";
            value = MethodUtils.invokeMethod(target, true, prefix + StringUtils.capitalize(fieldName));
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            log.warn("is/get调用异常 : {}-{}", e.getClass().getName(), e.getMessage());
            value = readField(field, target);
        }
        return value;
    }

    public static Object readField(Field field, Object target) {
        try {
            return FieldUtils.readField(field, target, true);
        } catch (IllegalAccessException e) {
            log.warn("字段读取异常 : {}-{}", e.getClass().getName(), e.getMessage());
        }
        return null;
    }

    public static Object readField(Object target, String fieldName) {
        try {
            return FieldUtils.readField(target, fieldName, true);
        } catch (IllegalAccessException e) {
            log.error("FieldUtils.readField failed: {}", e.getMessage());
        }
        return null;
    }

    public static boolean ignoreField(Field field) {
        return field.getName().startsWith("$")              // $jacocoData
            || Modifier.isStatic(field.getModifiers())      // static field
            || field.isAnnotationPresent(Id.class)          // id
            || field.isAnnotationPresent(Transient.class)   // Transient field
            ;
    }

    public String buildSelect(Object query) {
        return buildSelectAndArgs(query, null);
    }

    public String buildSelectAndArgs(Object query, List<Object> argList) {
        return build(DatabaseOperation.SELECT, query, argList);
    }

    public String buildCount(Object query) {
        return buildCountAndArgs(query, null);
    }

    public String buildCountAndArgs(Object query, List<Object> argList) {
        return build(DatabaseOperation.COUNT, query, argList);
    }

    public String buildDeleteAndArgs(Object query, List<Object> argList) {
        return build(DatabaseOperation.DELETE, query, argList);
    }

    private enum DatabaseOperation {
        SELECT, COUNT, DELETE
    }
}
