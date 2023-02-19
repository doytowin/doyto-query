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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import win.doyto.query.annotation.DomainPath;
import win.doyto.query.annotation.GroupBy;
import win.doyto.query.annotation.QueryField;
import win.doyto.query.annotation.Subquery;
import win.doyto.query.core.*;
import win.doyto.query.util.CommonUtil;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static win.doyto.query.sql.Constant.*;

/**
 * FieldProcessor
 *
 * @author f0rb on 2019-06-04
 */
@SuppressWarnings("java:S1874")
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class FieldProcessor {

    private static final Map<Field, Processor> FIELD_PROCESSOR_MAP = new ConcurrentHashMap<>();

    public static String execute(Field field, List<Object> argList, Object value) {
        return FIELD_PROCESSOR_MAP.get(field).process(argList, value);
    }

    public static void init(Field field) {
        Processor processor;
        if (Or.class.isAssignableFrom(field.getType())) {
            processor = new ConnectableFieldProcessor(field, OR);
        } else if (And.class.isAssignableFrom(field.getType())) {
            processor = new ConnectableFieldProcessor(field, AND);
        } else if (DoytoQuery.class.isAssignableFrom(field.getType())) {
            processor = initDoytoQueryField(field);
        } else if (field.isAnnotationPresent(QueryField.class)) {
            processor = initFieldAnnotatedByQueryField(field);
        } else if (Having.class.isAssignableFrom(field.getDeclaringClass())) {
            processor = initHavingField(field);
        } else if (boolean.class.isAssignableFrom(field.getType())) {
            processor = new PrimitiveBooleanProcessor(field.getName());
        } else {
            processor = initCommonField(field);
        }
        FIELD_PROCESSOR_MAP.put(field, processor);
    }

    private static Processor initDoytoQueryField(Field field) {
        Processor processor;
        if (field.isAnnotationPresent(DomainPath.class)) {
            if (field.getName().endsWith(QuerySuffix.Exists.name())) {
                processor = new ExistsProcessor(field);
            } else {
                processor = new DomainPathProcessor(field);
            }
        } else if (field.isAnnotationPresent(Subquery.class)) {
            processor = new SubqueryProcessor(field);
        } else if (SubqueryProcessor.matches(field.getName()) != null) {
            processor = new SubqueryProcessor(field.getName());
        } else {
            processor = (argList, value) -> {
                log.warn("Field configuration is invalid: {}.{}", field.getDeclaringClass(), field.getName());
                return null;
            };
        }
        return processor;
    }

    private static Processor initCommonField(Field field) {
        String fieldName = field.getName();
        return chooseProcessorForFieldWithOr(fieldName);
    }

    private static Processor initHavingField(Field field) {
        String fieldName = field.getName();
        if (!field.isAnnotationPresent(GroupBy.class)) {
             fieldName = HAVING_PREFIX + field.getName();
        }
        return chooseProcessorForFieldWithOr(fieldName);
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
