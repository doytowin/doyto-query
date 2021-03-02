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
    private static final String OFFSET = " OFFSET ";
    private Pattern fromPtn = Pattern.compile("FROM \\w+", Pattern.CASE_INSENSITIVE);
    private Pattern joinPtn = Pattern.compile("JOIN", Pattern.CASE_INSENSITIVE);
    private Pattern alias = Pattern.compile("(,|SELECT)\\s*([\\w*]+)");

    @Override
    public String buildPageSql(String sql, int limit, long offset) {
        if (!sql.startsWith("SELECT")) {
            return sql + LIMIT + limit;
        }
        if (joinPtn.matcher(sql).find()) {
            return sql + LIMIT + limit + OFFSET + offset;
        }
        return buildPageForSelect(sql, limit, offset);
    }

    private String buildPageForSelect(String sql, int limit, long offset) {
        Matcher fromMatcher = fromPtn.matcher(sql);
        fromMatcher.find();
        String fromTable = fromMatcher.group();
        int fromIndex = fromMatcher.start();

        StringBuffer buffer = new StringBuffer(sql.length() * 3);
        Matcher aliasMatcher = alias.matcher(sql.substring(0, fromIndex));
        while (aliasMatcher.find()) {
            aliasMatcher.appendReplacement(buffer, "$1 a.$2");
        }
        aliasMatcher.appendTail(buffer);
        buffer.append(fromTable)
              .append(" a JOIN (SELECT id ")
              .append(sql.substring(fromIndex))
              .append(LIMIT).append(limit)
              .append(OFFSET).append(offset)
              .append(") b ON a.id = b.id");
        return buffer.toString();
    }
}
