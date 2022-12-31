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
import java.util.regex.Pattern;

import static win.doyto.query.sql.Constant.*;

/**
 * SubqueryProcessor
 *
 * @author f0rb on 2022/12/29
 * @since 1.0.1
 */
public class SubqueryProcessor implements FieldProcessor.Processor {
    private static final Pattern PTN_DIGITS_END = Pattern.compile("\\d++$");
    private final String clauseFormat;

    public SubqueryProcessor(Field field) {
        Subquery subquery = field.getAnnotation(Subquery.class);
        String fieldName = field.getName();
        fieldName = PTN_DIGITS_END.matcher(fieldName).replaceFirst(EMPTY);
        SqlQuerySuffix querySuffix = SqlQuerySuffix.resolve(fieldName);

        String clause;
        if (querySuffix == SqlQuerySuffix.Any || querySuffix == SqlQuerySuffix.All) {
            String tempName = querySuffix.removeSuffix(fieldName);

            SqlQuerySuffix querySuffix1 = SqlQuerySuffix.resolve(tempName);
            String columnName = querySuffix1.removeSuffix(tempName);

            clause = columnName + SPACE + querySuffix1.getOp() + SPACE + querySuffix.getOp();
        } else {
            String columnName = querySuffix.removeSuffix(fieldName);
            clause = columnName + SPACE + querySuffix.getOp() + SPACE;
        }
        clauseFormat = clause + OP + SELECT + subquery.select() + FROM + subquery.from() + "%s" + CP;

    }

    @Override
    public String process(List<Object> argList, Object value) {
        String where = BuildHelper.buildWhere((DoytoQuery) value, argList);
        return String.format(clauseFormat, where);
    }
}
