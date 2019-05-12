package win.doyto.query.core;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

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

    private String buildWhere(Object query, List<Object> argList) {
        LinkedList<Object> whereList = new LinkedList<>();
        for (Field field : query.getClass().getDeclaredFields()) {
            Object value = readField(field, query);
            if (value == null) {
                continue;
            }
            QueryField queryField = field.getAnnotation(QueryField.class);
            if (queryField != null) {
                whereList.add(queryField.and());
                if (argList != null) {
                    argList.add(value);
                }
            } else {
                if (argList != null) {
                    argList.add(value);
                    whereList.add(field.getName() + " = ?");
                } else {
                    whereList.add(field.getName() + " = " + "#{" + field.getName() + "}");
                }
            }
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
