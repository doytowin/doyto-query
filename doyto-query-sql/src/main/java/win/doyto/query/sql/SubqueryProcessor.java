/*
 * Copyright © 2019-2022 Forb Yuan
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

import win.doyto.query.annotation.Subquery;
import win.doyto.query.core.DoytoQuery;

import java.lang.reflect.Field;
import java.util.List;

import static win.doyto.query.sql.Constant.*;

/**
 * SubqueryProcessor
 *
 * @author f0rb on 2022/12/29
 * @since 1.0.1
 */
public class SubqueryProcessor implements FieldProcessor.Processor {
    private final String clauseFormat;

    public SubqueryProcessor(Field field) {
        Subquery subquery = field.getAnnotation(Subquery.class);
        String fieldName = field.getName();
        SqlQuerySuffix querySuffix2 = SqlQuerySuffix.resolve(fieldName);
        String tempName = querySuffix2.removeSuffix(fieldName);

        SqlQuerySuffix querySuffix1 = SqlQuerySuffix.resolve(tempName);
        String columnName = querySuffix1.removeSuffix(tempName);

        clauseFormat = (columnName + SPACE + querySuffix1.getOp() + SPACE + querySuffix2.getOp())
                + OP + SELECT + subquery.select() + FROM + subquery.from() + "%s" + CP;

    }

    @Override
    public String process(List<Object> argList, Object value) {
        String where = BuildHelper.buildWhere((DoytoQuery) value, argList);
        return String.format(clauseFormat, where);
    }
}
