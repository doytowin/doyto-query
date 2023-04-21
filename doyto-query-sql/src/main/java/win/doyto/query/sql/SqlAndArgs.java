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

package win.doyto.query.sql;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static win.doyto.query.sql.Constant.*;

/**
 * SqlAndArgs
 *
 * @author f0rb on 2019-05-31
 */
@Slf4j
@Getter
public class SqlAndArgs {
    private final String sql;
    private final Object[] args;

    public SqlAndArgs(String sql, List<?> argList) {
        this(sql, argList.toArray());
    }

    public SqlAndArgs(String sql, Object... args) {
        this.sql = sql;
        this.args = args;
        logSqlInfo();
    }

    private void logSqlInfo() {
        if (log.isDebugEnabled()) {
            log.debug("SQL  : {}", sql);
            String params = Arrays
                    .stream(args)
                    .map(arg -> arg + (arg == null ? EMPTY : OP + arg.getClass().getName() + CP))
                    .collect(Collectors.joining(SEPARATOR));
            log.debug("Param: {}", params);
        }
    }

    static SqlAndArgs buildSqlWithArgs(Function<List<Object>, String> sqlBuilder) {
        List<Object> argList = new ArrayList<>();
        String sql = sqlBuilder.apply(argList);
        return new SqlAndArgs(sql, argList);
    }
}
