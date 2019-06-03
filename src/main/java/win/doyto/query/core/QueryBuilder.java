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

    static final String SEPARATOR = ", ";
    static final String REPLACE_HOLDER = "?";
    static final String SPACE = " ";

    private static final Map<Class, Field[]> classFieldsMap = new ConcurrentHashMap<>();
    private static final Map<Class, String> tableMap = new ConcurrentHashMap<>();
    private static final String COUNT = "count(*)";
    private static final String SELECT = "SELECT ";
    private static final String WHERE = " WHERE ";
    private static final String FROM = " FROM ";

    private static String build(PageQuery pageQuery, List<Object> argList, String operation, String... columns) {
        @SuppressWarnings("unchecked")
        String table = tableMap.computeIfAbsent(pageQuery.getClass(), c -> ((QueryTable) c.getAnnotation(QueryTable.class)).table());

        String sql;
        sql = buildStart(operation, columns, table);
        sql = buildWhere(sql, pageQuery, argList);
        sql = buildOrderBy(sql, pageQuery, operation);
        sql = buildPaging(sql, pageQuery, columns);
        return sql;
    }

    private static String buildStart(String operation, String[] columns, String table) {
        return operation + StringUtils.join(columns, SEPARATOR) + FROM + table;
    }

    private static String buildOrderBy(String sql, PageQuery pageQuery, String operation) {
        if (SELECT == operation && pageQuery.getSort() != null) {
            sql += " ORDER BY " + pageQuery.getSort().replaceAll(",", " ").replaceAll(";", ", ");
        }
        return sql;
    }

    private static String buildPaging(String sql, PageQuery pageQuery, String[] columns) {
        if (!(columns.length == 1 && COUNT == columns[0]) && pageQuery.needPaging()) {
            sql = GlobalConfiguration.instance().getDialect().buildPageSql(sql, pageQuery.getPageSize(), pageQuery.getOffset());
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
            sql += WHERE + StringUtils.join(whereList, " AND ");
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
        } else if (field.isAnnotationPresent(SubQuery.class)) {
            andSQL = resolvedSubQuery(field);
            argList.add(value);
        } else if (field.isAnnotationPresent(NestedQueries.class)) {
            NestedQueries nestedQueries = field.getAnnotation(NestedQueries.class);
            andSQL = resolvedNestedQuery(nestedQueries, argList, value);
        } else {
            String fieldName = field.getName();
            andSQL = QuerySuffix.buildAndSql(fieldName, value, argList);
        }
        whereList.add(andSQL);
    }

    static String resolvedNestedQuery(NestedQueries nestedQueries, List<Object> argList, Object value) {
        String andSQL;
        String subquery = getNestedQueries(nestedQueries);
        andSQL = (nestedQueries.column() + subquery + (nestedQueries.right().isEmpty() ? "" : " = " + REPLACE_HOLDER ))
            + StringUtils.repeat(')', nestedQueries.value().length);
        argList.add(value);
        return andSQL;
    }

    private static String getNestedQueries(NestedQueries nestedQueries) {
        StringBuilder subquery = new StringBuilder();
        String lastOp = "IN";
        NestedQuery[] value = nestedQueries.value();
        NestedQuery nestedQuery = value[0];
        subquery.append(SPACE).append(lastOp).append(" (").append(getNestedQuery(nestedQuery));
        lastOp = nestedQuery.op();
        for (int i = 1, valueLength = value.length; i < valueLength; i++) {
            nestedQuery = value[i];
            subquery.append(WHERE).append(nestedQuery.left());
            subquery.append(SPACE).append(lastOp).append(" (").append(getNestedQuery(nestedQuery));
            lastOp = nestedQuery.op();
        }
        subquery.append((nestedQueries.right().isEmpty() ? "" : WHERE + nestedQueries.right()));
        return subquery.toString();
    }

    private static String getNestedQuery(NestedQuery nestedQuery) {
        return SELECT +
            nestedQuery.left() +
            FROM +
            nestedQuery.table() +
            (nestedQuery.extra().isEmpty() ? "" : " ") +
            nestedQuery.extra();
    }

    static String resolvedSubQuery(Field field) {
        SubQuery subQuery = field.getAnnotation(SubQuery.class);
        String clause = getSubQuery(subQuery);
        return subQuery.column() + SPACE + subQuery.op() + SPACE + wrapWithParenthesis(clause + " = " + REPLACE_HOLDER);
    }

    private static String getSubQuery(SubQuery subQuery) {
        return SELECT +
            subQuery.left() +
            FROM +
            subQuery.table() +
            (subQuery.extra().isEmpty() ? "" : " ") +
            subQuery.extra() +
            (subQuery.right().isEmpty() ? "" : WHERE + subQuery.right());
    }

    public String buildSelectAndArgs(PageQuery query, List<Object> argList) {
        return buildSelectColumnsAndArgs(query, argList, "*");
    }

    public String buildCountAndArgs(PageQuery query, List<Object> argList) {
        return build(query, argList, SELECT, COUNT);
    }

    public SqlAndArgs buildCountAndArgs(PageQuery query) {
        ArrayList<Object> argList = new ArrayList<>();
        return new SqlAndArgs(buildCountAndArgs(query, argList), argList);
    }

    public String buildDeleteAndArgs(PageQuery query, List<Object> argList) {
        return build(query, argList, "DELETE");
    }

    public SqlAndArgs buildDeleteAndArgs(PageQuery query) {
        ArrayList<Object> argList = new ArrayList<>();
        return new SqlAndArgs(buildDeleteAndArgs(query, argList), argList);
    }

    public String buildSelectColumnsAndArgs(PageQuery query, List<Object> argList, String... columns) {
        return build(query, argList, SELECT, columns);
    }

    public SqlAndArgs buildSelectColumnsAndArgs(PageQuery query, String... columns) {
        ArrayList<Object> argList = new ArrayList<>();
        return new SqlAndArgs(buildSelectColumnsAndArgs(query, argList, columns), argList);
    }

}
