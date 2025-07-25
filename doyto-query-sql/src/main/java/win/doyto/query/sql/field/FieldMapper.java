/*
 * Copyright © 2019-2025 DoytoWin, Inc.
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

package win.doyto.query.sql.field;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import win.doyto.query.annotation.*;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.core.Having;
import win.doyto.query.core.Query;
import win.doyto.query.core.QuerySuffix;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * FieldMapper
 *
 * @author f0rb on 2019-06-04
 */
@SuppressWarnings("java:S1874")
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FieldMapper {

    private static final Map<Field, FieldProcessor> FIELD_PROCESSOR_MAP = new ConcurrentHashMap<>();

    public static String execute(Field field, String alias, List<Object> argList, Object value) {
        return FIELD_PROCESSOR_MAP.get(field).process(alias, argList, value);
    }

    public static void init(Field field) {
        if (FIELD_PROCESSOR_MAP.containsKey(field)) return;
        FIELD_PROCESSOR_MAP.put(field, new LogProcessor(field)); //To avoid recursive init

        boolean isAggregateField = false;
        if (Having.class.isAssignableFrom(field.getDeclaringClass())) {
            // process aggregate prefix for the field
            isAggregateField = !field.isAnnotationPresent(GroupBy.class);
        }

        FieldProcessor processor;
        Class<?> fieldType = field.getType();
        if (field.getName().endsWith("Or") && Collection.class.isAssignableFrom(field.getType())) {
            processor = new OrCollectionProcessor(field);
        } else if (field.getName().endsWith("Or") && Query.class.isAssignableFrom(field.getType())) {
            processor = new OrSuffixFieldProcessor(fieldType);
        } else if (field.getName().endsWith("And") && Collection.class.isAssignableFrom(field.getType())) {
            processor = new CollectionProcessor(field);
        } else if (OrFieldProcessor.support(field.getName())) {
            processor = new OrFieldProcessor(field);
        } else if (DoytoQuery.class.isAssignableFrom(fieldType)) {
            processor = initDoytoQueryField(field);
        } else if (Query.class.isAssignableFrom(fieldType)) {
            processor = new ConnectableFieldProcessor(fieldType);
        } else if (field.isAnnotationPresent(QueryField.class)) {
            processor = new QueryFieldProcessor(field);
        } else if (ColumnComparisonProcessor.support(field)) {
            processor = new ColumnComparisonProcessor(field.getName());
        } else if (field.isAnnotationPresent(Column.class)) {
            processor = new ColumnFieldProcessor(field);
        } else {
            processor = new SuffixFieldProcessor(field, isAggregateField);
        }
        FIELD_PROCESSOR_MAP.put(field, processor);
    }

    private static FieldProcessor initDoytoQueryField(Field field) {
        FieldProcessor processor;
        if (field.getName().equals("or")) {
            processor = new OrSuffixFieldProcessor(field.getType());
        } else if (field.getName().equals("and")) {
            processor = new ConnectableFieldProcessor(field.getType());
        } else if (field.isAnnotationPresent(DomainPath.class)) {
            if (field.getName().endsWith(QuerySuffix.Exists.name())) {
                processor = new ExistsProcessor(field);
            } else {
                processor = new DomainPathProcessor(field);
            }
        } else if (field.isAnnotationPresent(SubqueryV2.class)) {
            processor = new SubqueryV2Processor(field);
        } else if (field.isAnnotationPresent(Subquery.class)) {
            processor = new SubqueryProcessor(field);
        } else if (SubqueryProcessor.matches(field.getName()) != null) {
            processor = new SubqueryProcessor(field.getName());
        } else {
            processor = new LogProcessor(field);
        }
        return processor;
    }


}
