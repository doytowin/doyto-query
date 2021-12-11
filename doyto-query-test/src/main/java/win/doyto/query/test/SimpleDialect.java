package win.doyto.query.test;

import win.doyto.query.core.Dialect;

/**
 * MySQLDialect
 *
 * @author f0rb on 2019-07-22
 */
public class SimpleDialect implements Dialect {
    @Override
    public String buildPageSql(String sql, int limit, long offset) {
        return sql + " LIMIT " + limit + " OFFSET " + offset;
    }
}
