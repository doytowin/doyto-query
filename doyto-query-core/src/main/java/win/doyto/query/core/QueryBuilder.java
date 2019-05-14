package win.doyto.query.core;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * QueryBuilder
 *
 * @author f0rb
 * @date 2019-05-12
 */
@Slf4j
public class QueryBuilder {

    private static class Singleton {
        private static QueryBuilder instance = new QueryBuilder();
    }

    public static QueryBuilder instance() {
        return Singleton.instance;
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

    private static String build(DatabaseOperation operation, Object query, List<Object> argList) {
        QueryTable queryTable = query.getClass().getAnnotation(QueryTable.class);
        String table = queryTable.table();
        String sql;
        if (operation == DatabaseOperation.COUNT) {
            sql = "SELECT count(*) FROM " + table;
        } else {
            sql = "SELECT * FROM " + table;
        }

        sql += buildWhere(query, argList);
        if (operation == DatabaseOperation.SELECT && query instanceof PageQuery) {
            PageQuery pageQuery = (PageQuery) query;
            if (pageQuery.needPaging()) {
                sql += " LIMIT " + pageQuery.getPageSize() + " OFFSET " + pageQuery.getOffset();
            }
        }
        log.info("SQL: {}", sql);
        return sql;
    }

    static final Pattern PLACE_HOLDER_PTN = Pattern.compile("#\\{\\w+}");

    private static String buildWhere(Object query, List<Object> argList) {
        LinkedList<Object> whereList = new LinkedList<>();
        for (Field field : query.getClass().getDeclaredFields()) {
            if (field.getName().startsWith("$")) {
                continue;
            }
            Object value = readField(field, query);
            if (value != null) {
                processField(value, field, whereList, argList);
            }
        }
        String where = "";
        if (!whereList.isEmpty()) {
            where = " WHERE " + StringUtils.join(whereList, " and ");
        }
        return where;
    }

    private static void processField(Object value, Field field, LinkedList<Object> whereList, List<Object> argList) {
        QueryField queryField = field.getAnnotation(QueryField.class);
        String andSQL;
        if (queryField != null) {
            andSQL = queryField.and();
        } else {
            andSQL = field.getName() + " = " + "#{" + field.getName() + "}";
        }

        if (argList != null) {
            Matcher matcher = PLACE_HOLDER_PTN.matcher(andSQL);
            while (matcher.find()) {
                argList.add(value);
            }
            andSQL = matcher.replaceAll("?");
        }
        whereList.add(andSQL);
    }

    private static Object readField(Field field, Object query) {
        try {
            return FieldUtils.readField(field, query, true);
        } catch (IllegalAccessException e) {
            log.error("FieldUtils.readField failed: {}", e.getMessage());
        }
        return null;
    }

    private enum DatabaseOperation {
        SELECT, COUNT
    }
}
