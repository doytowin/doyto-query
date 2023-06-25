/*
 * Copyright © 2019-2023 Forb Yuan
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

/**
 * SQLServerDialect
 *
 * @author f0rb on 2023/6/23
 * @since 1.0.2
 */
public class SQLServerDialect implements Dialect {
    private static final String ORDER_BY = " ORDER BY ";

    @Override
    public String buildPageSql(String sql, int limit, long offset) {
        String orderById = "";
        if (!sql.contains(ORDER_BY)) {
            orderById = ORDER_BY + "id";
        }
        return sql + orderById + " offset " + offset + " row fetch next " + limit + " row only";
    }

    @Override
    public String resolveKeyColumn(String idColumn) {
        return "GENERATED_KEYS";
    }
}
