package win.doyto.query.config;

import win.doyto.query.core.Dialect;

/**
 * MySQLDialect
 *
 * @author f0rb on 2019-07-22
 */
class MySQLDialect implements Dialect {
    @Override
    public String buildPageSql(String sql, int limit, long offset) {
        return sql + " LIMIT " + limit + (sql.startsWith("SELECT") ? " OFFSET " + offset : "");
    }
}
