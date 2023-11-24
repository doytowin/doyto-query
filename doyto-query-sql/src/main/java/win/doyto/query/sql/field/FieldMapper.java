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

package win.doyto.query.sql.field;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import win.doyto.query.annotation.DomainPath;
import win.doyto.query.annotation.GroupBy;
import win.doyto.query.annotation.QueryField;
import win.doyto.query.annotation.Subquery;
import win.doyto.query.core.*;

import javax.persistence.Column;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static win.doyto.query.sql.Constant.AND;
import static win.doyto.query.sql.Constant.OR;

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
        FieldProcessor processor;
        Class<?> fieldType = field.getType();
        if (Or.class.isAssignableFrom(fieldType)) {
            processor = new ConnectableFieldProcessor(fieldType, OR);
        } else if (And.class.isAssignableFrom(fieldType)) {
            processor = new ConnectableFieldProcessor(fieldType, AND);
        } else if (DoytoQuery.class.isAssignableFrom(fieldType)) {
            processor = initDoytoQueryField(field);
        } else if (field.isAnnotationPresent(QueryField.class)) {
            processor = new QueryFieldProcessor(field);
        } else if (Having.class.isAssignableFrom(field.getDeclaringClass())) {
            processor = initHavingField(field);
        } else if (boolean.class.isAssignableFrom(fieldType)) {
            processor = new PrimitiveBooleanProcessor(field.getName());
        } else if (OrCollectionProcessor.support(field)) {
            processor = new OrCollectionProcessor(field);
        } else if (field.isAnnotationPresent(Column.class)) {
            processor = new ColumnFieldProcessor(field);
        } else if (OrFieldProcessor.support(field.getName())) {
            processor = new OrFieldProcessor(field);
        } else {
            processor = new SuffixFieldProcessor(field);
        }
        FIELD_PROCESSOR_MAP.put(field, processor);
    }

    private static FieldProcessor initDoytoQueryField(Field field) {
        FieldProcessor processor;
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
            processor = (alias, argList, value) -> {
                log.info("Query field is ignored: {}.{}", field.getDeclaringClass(), field.getName());
                return null;
            };
        }
        return processor;
    }

    private static FieldProcessor initHavingField(Field field) {
        String fieldName = field.getName();
        boolean isAggregateField = !field.isAnnotationPresent(GroupBy.class);
        if (OrFieldProcessor.support(fieldName)) {
            return new OrFieldProcessor(field);
        } else {
            return new SuffixFieldProcessor(field, isAggregateField);
        }
    }

}
