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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * SQLServerDialect
 *
 * @author f0rb on 2023/6/23
 * @since 1.0.2
 */
public class SQLServerDialect implements Dialect {
    private static final String ORDER_BY = " ORDER BY ";
    private final Pattern groupPtn = Pattern.compile("GROUP BY (\\w+(,\\s*\\w+)++)", Pattern.CASE_INSENSITIVE);

    @Override
    public String buildPageSql(String sql, int limit, long offset) {
        String orderById = "";
        if (!sql.contains(ORDER_BY)) {
            Matcher matcher = groupPtn.matcher(sql);
            if (matcher.find()) {
                orderById = ORDER_BY + matcher.group(1);
            } else {
                orderById = ORDER_BY + "id";
            }
        }
        return sql + orderById + " offset " + offset + " row fetch next " + limit + " row only";
    }

    @Override
    public String buildInsertIgnore(StringBuilder insertBuilder, String tableName, String k1, String k2) {
        return insertBuilder.toString();
    }

    @Override
    public String resolveKeyColumn(String idColumn) {
        return "GENERATED_KEYS";
    }

    @Override
    public String convertMultiColumnsIn(StringBuilder sqlBuilder, String k1Column, String k2Column, int size) {
        int indexOfWhere = sqlBuilder.indexOf("WHERE ") + 6;
        String s1 = IntStream.range(0, size).mapToObj(i -> k1Column + " = ? AND " + k2Column + " = ?")
                             .collect(Collectors.joining(" OR "));
        return sqlBuilder.replace(indexOfWhere, sqlBuilder.length(), s1).toString();
    }

}
