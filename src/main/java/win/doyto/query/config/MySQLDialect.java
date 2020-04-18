package win.doyto.query.config;

import win.doyto.query.core.Dialect;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MySQLDialect
 *
 * @author f0rb on 2019-07-22
 */
public class MySQLDialect implements Dialect {
    private static final String LIMIT = " LIMIT ";
    private Pattern from = Pattern.compile("FROM \\w+");
    private Pattern join = Pattern.compile("join", Pattern.CASE_INSENSITIVE);
    private Pattern alias = Pattern.compile("(,|SELECT)\\s*([\\w*]+)");

    @Override
    public String buildPageSql(String sql, int limit, long offset) {
        if (!sql.startsWith("SELECT")) {
            return sql + LIMIT + limit;
        }
        Matcher fromMatcher = from.matcher(sql);
        if (!fromMatcher.find() || join.matcher(sql).find()) {
            return sql + LIMIT + limit + (sql.startsWith("SELECT") ? " OFFSET " + offset : "");
        }
        String fromTable = fromMatcher.group();
        int fromIndex = fromMatcher.start();

        StringBuffer start = new StringBuffer(sql.length() * 3);
        Matcher aliasMatcher = alias.matcher(sql.substring(0, fromIndex));
        while (aliasMatcher.find()) {
            aliasMatcher.appendReplacement(start, "$1 a.$2");
        }
        aliasMatcher.appendTail(start);
        start.append(fromTable)
             .append(" a JOIN (SELECT id ")
             .append(sql.substring(fromIndex))
             .append(LIMIT).append(limit).append(" OFFSET ").append(offset)
             .append(") b ON a.id = b.id");
        return start.toString();

    }
}
