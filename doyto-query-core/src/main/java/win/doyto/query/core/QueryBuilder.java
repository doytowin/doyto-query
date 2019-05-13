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

    public String buildSelect(Object query) {
        return buildSelectAndArgs(query, null);
    }

    public String buildSelectAndArgs(Object query, List<Object> argList) {
        QueryTable queryTable = query.getClass().getAnnotation(QueryTable.class);
        String table = queryTable.table();
        String select = "SELECT * FROM " + table;

        select += buildWhere(query, argList);
        if (query instanceof PageQuery) {
            PageQuery pageQuery = (PageQuery) query;
            if (pageQuery.needPaging()) {
                select += " LIMIT " + pageQuery.getPageSize() + " OFFSET " + pageQuery.getOffset();
            }
        }
        return select;
    }

    static final Pattern PLACE_HOLDER_PTN = Pattern.compile("#\\{\\w+}");

    private String buildWhere(Object query, List<Object> argList) {
        LinkedList<Object> whereList = new LinkedList<>();
        for (Field field : query.getClass().getDeclaredFields()) {
            Object value = readField(field, query);
            if (value == null) {
                continue;
            }
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
        String where = "";
        if (!whereList.isEmpty()) {
            where = " WHERE " + StringUtils.join(whereList, " and ");
        }
        return where;
    }

    private static Object readField(Field field, Object query) {
        try {
            return FieldUtils.readField(field, query, true);
        } catch (IllegalAccessException e) {
            log.error("FieldUtils.readField failed: {}", e.getMessage());
        }
        return null;
    }
}
