package win.doyto.query.core;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import win.doyto.query.config.GlobalConfiguration;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

import static win.doyto.query.core.CommonUtil.*;

/**
 * QueryBuilder
 *
 * @author f0rb
 */
@Slf4j
public class QueryBuilder {

    private static final Map<Class, Field[]> classFieldsMap = new ConcurrentHashMap<>();
    private static final Map<Class, String> tableMap = new ConcurrentHashMap<>();

    static final String SEPARATOR = ", ";
    static final String REPLACE_HOLDER = "?";

    private static String build(DatabaseOperation operation, Object query, List<Object> argList, String... columns) {
        String table = tableMap.computeIfAbsent(query.getClass(), c -> ((QueryTable) c.getAnnotation(QueryTable.class)).table());
        String sql = start(operation, table, columns);

        sql = buildWhere(sql, query, argList);

        if (operation == DatabaseOperation.SELECT && query instanceof PageQuery) {
            PageQuery pageQuery = (PageQuery) query;
            if (pageQuery.getSort() != null) {
                sql += " ORDER BY " + pageQuery.getSort().replaceAll(",", " ").replaceAll(";", ", ");
            }
        }
        if (operation != DatabaseOperation.COUNT && query instanceof PageQuery && ((PageQuery) query).needPaging()) {
            PageQuery pageQuery = (PageQuery) query;
            sql = GlobalConfiguration.instance().getDialect().buildPageSql(sql, pageQuery.getPageSize(), pageQuery.getOffset());
        }
        return sql;
    }

    private static String start(DatabaseOperation operation, String table, String[] columns) {
        String sql;
        if (operation == DatabaseOperation.COUNT) {
            sql = "SELECT count(*) FROM " + table;
        } else if (operation == DatabaseOperation.DELETE) {
            sql = "DELETE FROM " + table;
        } else {
            sql = "SELECT " + StringUtils.join(columns, SEPARATOR) + " FROM " + table;
        }
        return sql;
    }

    public static String buildWhere(String sql, Object query, List<Object> argList) {
        initFields(query);
        Field[] fields = classFieldsMap.get(query.getClass());
        List<Object> whereList = new ArrayList<>(fields.length);
        for (Field field : fields) {
            String fieldName = field.getName();
            Object value = readFieldGetter(field, query);
            if (isValidValue(value, field)) {
                if (sql.contains("${" + fieldName + "}") && StringUtils.isAlphanumeric(String.valueOf(value))) {
                    sql = sql.replaceAll("\\$\\{" + fieldName + "}", String.valueOf(value));
                } else {
                    processField(value, field, whereList, argList);
                }
            }
        }
        if (!whereList.isEmpty()) {
            sql += " WHERE " + StringUtils.join(whereList, " AND ");
        }
        return sql;
    }

    private static void initFields(Object query) {
        Class<?> clazz = query.getClass();
        if (!classFieldsMap.containsKey(clazz)) {
            classFieldsMap.put(clazz, Arrays.stream(clazz.getDeclaredFields()).filter(field -> !ignoreField(field)).toArray(Field[]::new));
        }
    }

    private static void processField(Object value, Field field, List<Object> whereList, List<Object> argList) {
        String andSQL;
        QueryField queryField = field.getAnnotation(QueryField.class);
        if (queryField != null) {
            andSQL = queryField.and();
            IntStream.range(0, StringUtils.countMatches(andSQL, REPLACE_HOLDER)).mapToObj(i -> value).forEach(argList::add);
        } else if (field.isAnnotationPresent(NestedQuery.class) || field.isAnnotationPresent(NestedQueries.class)) {
            andSQL = resolvedNestedQuery(field);
            argList.add(value);
        } else {
            String fieldName = field.getName();
            andSQL = QuerySuffix.buildAndSql(fieldName, value, argList);
        }
        whereList.add(andSQL);
    }

    static String resolvedNestedQuery(Field field) {
        NestedQueries nestedQueries = field.getAnnotation(NestedQueries.class);
        if (nestedQueries == null) {
            NestedQuery nestedQuery = field.getAnnotation(NestedQuery.class);
            String subquery = getSubquery(nestedQuery);
            return concatNestedQueries(nestedQuery.column(), subquery);
        }
        String subquery = getSubquery(nestedQueries);
        return concatNestedQueries(nestedQueries.column(), subquery) + StringUtils.repeat(')', nestedQueries.value().length - 1);
    }

    private static String concatNestedQueries(String column, String string) {
        return column + string + " = " + REPLACE_HOLDER + ")";
    }

    private static String getSubquery(NestedQueries nestedQueries) {
        StringBuilder subquery = new StringBuilder();
        for (NestedQuery nestedQuery : nestedQueries.value()) {
            subquery.append(getSubquery(nestedQuery));
        }
        return subquery.toString();
    }

    private static String getSubquery(NestedQuery nestedQuery) {
        return " IN (SELECT " +
            nestedQuery.left() +
            " FROM " +
            nestedQuery.table() +
            (nestedQuery.extra().isEmpty() ? "" : " ") +
            nestedQuery.extra() +
            " WHERE " +
            nestedQuery.right();
    }

    public String buildSelectAndArgs(Object query, List<Object> argList) {
        return buildSelectColumnsAndArgs(query, argList, "*");
    }

    public String buildCountAndArgs(Object query, List<Object> argList) {
        return build(DatabaseOperation.COUNT, query, argList);
    }

    public SqlAndArgs buildCountAndArgs(Object query) {
        ArrayList<Object> argList = new ArrayList<>();
        return new SqlAndArgs(buildCountAndArgs(query, argList), argList);
    }

    public String buildDeleteAndArgs(Object query, List<Object> argList) {
        return build(DatabaseOperation.DELETE, query, argList);
    }

    public SqlAndArgs buildDeleteAndArgs(Object query) {
        ArrayList<Object> argList = new ArrayList<>();
        return new SqlAndArgs(buildDeleteAndArgs(query, argList), argList);
    }

    public String buildSelectColumnsAndArgs(Object query, List<Object> argList, String... columns) {
        return build(DatabaseOperation.SELECT, query, argList, columns);
    }

    public SqlAndArgs buildSelectColumnsAndArgs(Object query, String[] columns) {
        ArrayList<Object> argList = new ArrayList<>();
        return new SqlAndArgs(buildSelectColumnsAndArgs(query, argList, columns), argList);
    }

    private enum DatabaseOperation {
        SELECT, COUNT, DELETE
    }
}
