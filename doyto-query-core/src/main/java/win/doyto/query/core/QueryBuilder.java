package win.doyto.query.core;

/**
 * QueryBuilder
 *
 * @author f0rb
 * @date 2019-05-12
 */
public class QueryBuilder {
    public String buildSelect(Object query) {
        QueryTable queryTable = query.getClass().getAnnotation(QueryTable.class);
        String table = queryTable.table();
        return "SELECT * FROM " + table;
    }
}
