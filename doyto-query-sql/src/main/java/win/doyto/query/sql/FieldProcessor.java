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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import win.doyto.query.annotation.DomainPath;
import win.doyto.query.annotation.NestedQueries;
import win.doyto.query.annotation.QueryField;
import win.doyto.query.annotation.QueryTableAlias;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.core.Or;
import win.doyto.query.core.QuerySuffix;
import win.doyto.query.util.ColumnUtil;
import win.doyto.query.util.CommonUtil;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;

import static win.doyto.query.sql.Constant.*;
import static win.doyto.query.sql.NestedQueryInitializer.initFieldAnnotatedByNestedQueries;

/**
 * FieldProcessor
 *
 * @author f0rb on 2019-06-04
 */
@SuppressWarnings("java:S1874")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class FieldProcessor {

    private static final Map<Field, Processor> FIELD_PROCESSOR_MAP = new ConcurrentHashMap<>();

    public static String execute(Field field, List<Object> argList, Object value) {
        return FIELD_PROCESSOR_MAP.get(field).process(argList, value);
    }

    public static void init(Field field) {
        Processor processor;
        if (Or.class.isAssignableFrom(field.getType())) {
            processor = initFieldMappedByOr(field);
        } else if (DoytoQuery.class.isAssignableFrom(field.getType())) {
            if (field.isAnnotationPresent(DomainPath.class)) {
                processor = new DomainPathProcessor(field);
            } else {
                processor = (argList, value) -> null;
            }
        } else if (field.isAnnotationPresent(QueryTableAlias.class)) {
            processor = initFieldAnnotatedByQueryTableAlias(field);
        } else if (field.isAnnotationPresent(QueryField.class)) {
            processor = initFieldAnnotatedByQueryField(field);
        } else if (field.isAnnotationPresent(NestedQueries.class)) {
            processor = initFieldAnnotatedByNestedQueries(field);
        } else {
            processor = initCommonField(field);
        }
        FIELD_PROCESSOR_MAP.put(field, processor);
    }

    private static Processor initFieldMappedByOr(Field field) {
        Field[] fields = ColumnUtil.initFields(field.getType());
        Arrays.stream(fields).forEach(FieldProcessor::init);
        return (argList, value) -> {
            StringJoiner or = new StringJoiner(SPACE_OR, OP, CP);
            for (Field subField : fields) {
                Object subValue = CommonUtil.readField(subField, value);
                if (QuerySuffix.isValidValue(subValue, subField)) {
                    String condition = execute(subField, argList, subValue);
                    or.add(condition);
                }
            }
            return or.length() == "()".length() ? null : or.toString();
        };
    }

    private static Processor initCommonField(Field field) {
        String fieldName = field.getName();
        return chooseProcessorForFieldWithOr(fieldName);
    }

    private static Processor initFieldAnnotatedByQueryTableAlias(Field field) {
        String fieldName = field.getName();
        String tableAlias = field.getAnnotation(QueryTableAlias.class).value();
        String fieldNameWithAlias = tableAlias + "." + fieldName;
        return chooseProcessorForFieldWithOr(fieldNameWithAlias);
    }

    private static Processor chooseProcessorForFieldWithOr(String fieldName) {
        if (CommonUtil.containsOr(fieldName)) {
            return (argList, value) -> SqlQuerySuffix.buildConditionForFieldContainsOr(fieldName, argList, value);
        } else {
            return (argList, value) -> SqlQuerySuffix.buildConditionForField(fieldName, argList, value);
        }
    }

    private static Processor initFieldAnnotatedByQueryField(Field field) {
        String andSQL = field.getAnnotation(QueryField.class).and();
        int holderCount = StringUtils.countMatches(andSQL, PLACE_HOLDER);
        return (argList, value) -> {
            for (int i = 0; i < holderCount; i++) {
                argList.add(value);
            }
            return andSQL;
        };
    }

    interface Processor {
        String process(List<Object> argList, Object value);
    }

}
