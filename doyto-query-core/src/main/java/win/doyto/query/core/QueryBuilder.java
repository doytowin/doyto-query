package win.doyto.query.core;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.util.LinkedList;

/**
 * QueryBuilder
 *
 * @author f0rb
 * @date 2019-05-12
 */
@Slf4j
public class QueryBuilder {
    public String buildSelect(Object query) {
        QueryTable queryTable = query.getClass().getAnnotation(QueryTable.class);
        String table = queryTable.table();
        String select = "SELECT * FROM " + table;

        LinkedList<Object> whereList = new LinkedList<>();
        for (Field field : query.getClass().getDeclaredFields()) {
            Object value = readField(field, query);
            if (value == null) {
                continue;
            }
            QueryField queryField = field.getAnnotation(QueryField.class);
            if (queryField != null) {
                whereList.add(queryField.and());
            } else {
                whereList.add(field.getName() + " = " + "#{" + field.getName() + "}");
            }
        }
        if (!whereList.isEmpty()) {
            String where = " WHERE " + StringUtils.join(whereList, " and ");
            select += where;
        }
        if (query instanceof PageQuery) {
            PageQuery pageQuery = (PageQuery) query;
            if (pageQuery.needPaging()) {
                select += " LIMIT " + pageQuery.getPageSize() + " OFFSET " + pageQuery.getOffset();
            }
        }

        return select;
    }

    private Object readField(Field field, Object query) {
        try {
            return FieldUtils.readField(field, query, true);
        } catch (IllegalAccessException e) {
            log.error("FieldUtils.readField failed: {}", e.getMessage());
        }
        return null;
    }
}
