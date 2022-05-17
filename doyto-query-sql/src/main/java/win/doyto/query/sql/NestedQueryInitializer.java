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

import lombok.Generated;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import win.doyto.query.annotation.NestedQueries;
import win.doyto.query.annotation.NestedQuery;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.util.CommonUtil;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.IntStream;

import static win.doyto.query.sql.Constant.*;

/**
 * NestedQueryInitializer
 *
 * @author f0rb on 2022-04-23
 */
@SuppressWarnings("java:S1874")
@UtilityClass
@Generated
public class NestedQueryInitializer {
    private static final FieldProcessor.Processor EMPTY_PROCESSOR = ((argList, value) -> EMPTY);

    static FieldProcessor.Processor initFieldAnnotatedByNestedQueries(Field field) {
        NestedQueries nestedQueries = field.getAnnotation(NestedQueries.class);
        FieldProcessor.Processor processor = chooseProcessorForNestedQuery(field);
        return (argList, value) -> resolvedNestedQueries(argList, value, nestedQueries, processor);
    }

    private static FieldProcessor.Processor chooseProcessorForNestedQuery(Field field) {
        FieldProcessor.Processor processor;
        Class<?> fieldType = field.getType();
        if (boolean.class.isAssignableFrom(fieldType)) {
            processor = EMPTY_PROCESSOR;
        } else if (DoytoQuery.class.isAssignableFrom(fieldType)) {
            processor = (argList, value) -> BuildHelper.buildWhere((DoytoQuery) value, argList);
        } else {
            String fieldName = field.getName();
            if (CommonUtil.containsOr(fieldName)) {
                processor = (argList, value) -> WHERE + SqlQuerySuffix.buildConditionForFieldContainsOr(fieldName, argList, value);
            } else {
                processor = (argList, value) -> WHERE + SqlQuerySuffix.buildConditionForField(fieldName, argList, value);
            }
        }
        return processor;
    }

    private static String resolvedNestedQueries(List<Object> argList, Object value, NestedQueries nestedQueries, FieldProcessor.Processor processor) {
        StringBuilder nestQuery = resolvedNestedQueries(nestedQueries);
        IntStream.range(0, StringUtils.countMatches(nestQuery, PLACE_HOLDER)).mapToObj(i -> value).forEach(argList::add);
        if (nestedQueries.appendWhere()) {
            nestQuery.append(processor.process(argList, value));
        }
        return nestedQueries.column() + nestQuery + StringUtils.repeat(')', nestedQueries.value().length);
    }

    private static StringBuilder resolvedNestedQueries(NestedQueries nestedQueries) {
        StringBuilder nestedQueryBuilder = new StringBuilder();
        String lastOp = nestedQueries.op();
        String lastWhere = nestedQueries.column();
        NestedQuery[] nestedQueryArr = nestedQueries.value();

        for (int i = 0; i < nestedQueryArr.length; i++) {
            NestedQuery nestedQuery = nestedQueryArr[i];
            if (i > 0) {
                nestedQueryBuilder.append(WHERE).append(StringUtils.defaultIfBlank(lastWhere, nestedQuery.select()));
            }
            nestedQueryBuilder.append(SPACE).append(lastOp).append(" (").append(getNestedQuery(nestedQuery));

            lastOp = nestedQuery.op();
            lastWhere = nestedQuery.where();
        }
        return nestedQueryBuilder;
    }

    private static String getNestedQuery(NestedQuery nestedQuery) {
        return SELECT +
                nestedQuery.select() +
                FROM +
                nestedQuery.from() +
                StringUtils.defaultIfBlank(SPACE + nestedQuery.extra(), EMPTY);
    }
}
