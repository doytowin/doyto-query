package win.doyto.query.core;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import win.doyto.query.config.GlobalConfiguration;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static win.doyto.query.core.CommonUtil.*;

/**
 * QueryBuilder
 *
 * @author f0rb
 */
@Slf4j
public class QueryBuilder {

    static final String SEPARATOR = ", ";
    static final String REPLACE_HOLDER = "?";

    private static String build(DatabaseOperation operation, Object query, List<Object> argList, String... columns) {
        QueryTable queryTable = query.getClass().getAnnotation(QueryTable.class);
        String table = queryTable.table();
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

    protected static String buildWhere(String sql, Object query, List<Object> argList) {
        Field[] fields = query.getClass().getDeclaredFields();
        List<Object> whereList = new ArrayList<>(fields.length);
        for (Field field : fields) {
            String fieldName = field.getName();
            if (ignoreField(field)) {
                continue;
            }
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

    private static void processField(Object value, Field field, List<Object> whereList, List<Object> argList) {
        String andSQL;
        QueryField queryField = field.getAnnotation(QueryField.class);
        if (queryField != null) {
            andSQL = replaceArgs(value, argList, queryField.and());
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

    private static final Pattern PLACE_HOLDER_PTN = Pattern.compile("#\\{\\w+}");

    private static String replaceArgs(Object value, List<Object> argList, String andSQL) {
        String and = PLACE_HOLDER_PTN.matcher(andSQL).replaceAll(REPLACE_HOLDER);
        IntStream.range(0, StringUtils.countMatches(and, REPLACE_HOLDER)).mapToObj(i -> value).forEach(argList::add);
        return and;
    }

    public String buildSelectAndArgs(Object query, List<Object> argList) {
        return buildSelectColumnsAndArgs(query, argList, "*");
    }

    public String buildCountAndArgs(Object query, List<Object> argList) {
        return build(DatabaseOperation.COUNT, query, argList);
    }

    public <Q> SqlAndArgs buildCountAndArgs(Q q) {
        ArrayList<Object> argList = new ArrayList<>();
        return new SqlAndArgs(buildCountAndArgs(q, argList), argList);
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
