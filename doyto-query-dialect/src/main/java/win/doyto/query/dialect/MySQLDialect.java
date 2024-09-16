/*
 * Copyright © 2019-2024 Forb Yuan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package win.doyto.query.dialect;

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
    private final Pattern fromPtn = Pattern.compile("FROM \\w+", Pattern.CASE_INSENSITIVE);
    private final Pattern joinPtn = Pattern.compile("(JOIN|GROUP)", Pattern.CASE_INSENSITIVE);
    private final Pattern alias = Pattern.compile("(,|SELECT)\\s*([\\w*]+)");

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

    @Override
    public String wrapSelectForUpdate(String sql, String column) {
        return "SELECT " + column + " FROM " + "(" + sql + ") AS TEMP";
    }
}
