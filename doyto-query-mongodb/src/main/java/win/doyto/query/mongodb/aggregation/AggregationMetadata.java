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

package win.doyto.query.mongodb.aggregation;

import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.BsonField;
import lombok.Getter;
import org.bson.Document;
import org.bson.conversions.Bson;
import win.doyto.query.annotation.DomainPath;
import win.doyto.query.annotation.GroupBy;
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.core.AggregationQuery;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.core.Having;
import win.doyto.query.mongodb.filter.EmptyBson;
import win.doyto.query.mongodb.filter.MongoFilterBuilder;
import win.doyto.query.mongodb.filter.MongoGroupBuilder;
import win.doyto.query.util.ColumnUtil;
import win.doyto.query.util.CommonUtil;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.persistence.GeneratedValue;

import static win.doyto.query.mongodb.MongoConstant.*;
import static win.doyto.query.mongodb.aggregation.DomainPathBuilder.buildLookUpForSubDomain;

/**
 * AggregationMetadata
 *
 * @author f0rb on 2022-01-27
 */
@Getter
public class AggregationMetadata<C> {
    private static final Map<Class<?>, AggregationMetadata<?>> holder = new ConcurrentHashMap<>();
    private static final Bson SORT_BY_ID = new Document(MONGO_ID, 1);

    private final Class<?> viewClass;
    private final C collection;
    private final Bson groupBy;
    private final Bson project;
    private final Field[] domainFields;
    private final Document groupId;

    <V> AggregationMetadata(Class<V> viewClass, C collection) {
        this.viewClass = viewClass;
        this.collection = collection;
        this.groupId = buildGroupId(viewClass);
        this.groupBy = buildGroupBy(viewClass, this.groupId);
        this.project = buildProject(viewClass, groupBy != null);
        this.domainFields = buildDomainFields(viewClass);
    }

    @SuppressWarnings("unchecked")
    public static <C> AggregationMetadata<C> build(Class<?> viewClass, Function<Class<?>, C> collectionProvider) {
        return (AggregationMetadata<C>) holder.computeIfAbsent(viewClass, clazz
                -> new AggregationMetadata<>(clazz, collectionProvider.apply(clazz)));
    }

    private static <V> Field[] buildDomainFields(Class<V> viewClass) {
        return ColumnUtil.filterFields(viewClass, field -> field.isAnnotationPresent(DomainPath.class))
                         .toArray(Field[]::new);
    }

    private static <V> Bson buildGroupBy(Class<V> viewClass, Document groupDoc) {
        List<BsonField> fieldAccumulators = collectAccumulators(viewClass);
        if (groupDoc.isEmpty() && fieldAccumulators.isEmpty()) {
            return null;
        }
        return Aggregates.group(groupDoc, fieldAccumulators);
    }

    private static <V> List<BsonField> collectAccumulators(Class<V> viewClass) {
        Field[] fields = ColumnUtil.initFields(viewClass);
        return Arrays.stream(fields)
                     .map(MongoGroupBuilder::getBsonField)
                     .filter(Objects::nonNull)
                     .collect(Collectors.toList());
    }

    private static <V> Document buildGroupId(Class<V> viewClass) {
        Document id = new Document();
        Field[] fields = ColumnUtil.initFields(viewClass);
        for (Field field : fields) {
            if (field.isAnnotationPresent(GroupBy.class)) {
                String fieldName = field.getName();
                id.append(fieldName, ex(fieldName));
            }
        }
        return id;
    }

    private static <V> Bson buildProject(Class<V> viewClass, boolean isAggregated) {
        Field[] fields = ColumnUtil.initFields(viewClass);
        Document columns = new Document();
        if (isAggregated) {
            columns.append(MONGO_ID, 0);// don't show _id when do aggregation
        }
        for (Field field : fields) {
            if (field.isAnnotationPresent(GeneratedValue.class)) {
                continue;
            }
            String column = field.getName();
            if (isManyToOneField(field)) {
                columns.append(column, new Document("$arrayElemAt", Arrays.asList(ex(column), 0)));
            } else {
                columns.append(column, ex(column));
            }
            if (field.isAnnotationPresent(GroupBy.class)) {
                String fieldName = field.getName();
                columns.append(fieldName, "$_id." + fieldName); //grouped fields are in _id
            }
        }

        return Aggregates.project(columns);
    }

    private static boolean isManyToOneField(Field field) {
        return field.isAnnotationPresent(DomainPath.class) && !Collection.class.isAssignableFrom(field.getType());
    }

    public <Q extends DoytoQuery> List<Bson> buildAggregation(Q query) {
        List<Bson> pipeline = build(query);
        pipeline.add(this.getProject());
        return pipeline;
    }

    public <Q extends DoytoQuery> List<Bson> buildCount(Q query) {
        List<Bson> pipeline = build(query);
        pipeline.add(Aggregates.count(COUNT_KEY));
        return pipeline;
    }

    @SuppressWarnings("java:S3776")
    private <Q extends DoytoQuery> List<Bson> build(Q query) {
        List<Bson> pipeline = new ArrayList<>();

        List<Field> lookupFields = new ArrayList<>();
        List<String> unwindFields = new ArrayList<>();
        List<String> unsetFields = new ArrayList<>();

        Field[] fields = ColumnUtil.initFields(query.getClass());
        for (Field field : fields) {
            if (field.isAnnotationPresent(DomainPath.class)) {
                Object value = CommonUtil.readFieldGetter(field, query);
                if (value instanceof DoytoQuery) {
                    String subDomainName = field.getName();
                    DomainPath domainPath = field.getAnnotation(DomainPath.class);

                    lookupFields.add(field);

                    if (domainPath.value().length == 1) {
                        unwindFields.add(subDomainName);
                    }
                    unsetFields.add(subDomainName);
                }
            }
        }
        for (Field lookupField : lookupFields) {
            String subDomainName = lookupField.getName();
            DomainPath domainPath = lookupField.getAnnotation(DomainPath.class);
            pipeline.add(DomainPathBuilder.buildLookUpForNestedQuery(subDomainName, domainPath));
        }
        if (!unwindFields.isEmpty()) {
            for (String unwindField : unwindFields) {
                pipeline.add(new Document("$unwind", ex(unwindField)));
            }
        }
        Bson filter = MongoFilterBuilder.buildFilter(query);
        if (!(filter instanceof EmptyBson)) {
            pipeline.add(Aggregates.match(filter));
        }
        if (!unsetFields.isEmpty()) {
            pipeline.add(new Document("$unset", unsetFields));
        }

        for (Field field : this.getDomainFields()) {
            Object domainQuery = CommonUtil.readField(query, field.getName() + "Query");
            if (domainQuery instanceof DoytoQuery) {
                Class<?> relatedViewClass = field.getType();
                if (Collection.class.isAssignableFrom(field.getType())) {
                    ParameterizedType type = (ParameterizedType) field.getGenericType();
                    relatedViewClass = (Class<?>) type.getActualTypeArguments()[0];
                }
                Bson lookupDoc = buildLookUpForSubDomain((DoytoQuery) domainQuery, relatedViewClass, field);
                pipeline.add(lookupDoc);
            }
        }
        if (this.getGroupBy() != null) {
            pipeline.add(this.getGroupBy());
        }
        if (query instanceof AggregationQuery) {
            Having having = ((AggregationQuery) query).getHaving();
            if (having != null) {
                pipeline.add(buildHaving(having));
            }
        }
        pipeline.add(buildSort(query, this.getGroupId().keySet()));
        if (query.needPaging()) {
            pipeline.add(Aggregates.skip(GlobalConfiguration.calcOffset(query)));
            pipeline.add(Aggregates.limit(query.getPageSize()));
        }
        return pipeline;
    }

    private <H extends Having> Bson buildHaving(H having) {
        return Aggregates.match(MongoFilterBuilder.buildFilter(having));
    }

    private <Q extends DoytoQuery> Bson buildSort(Q query, Set<String> groupColumns) {
        Bson sort = SORT_BY_ID;
        if (query.getSort() != null) {
            sort = MongoFilterBuilder.buildSort(query.getSort(), groupColumns);
        }
        return Aggregates.sort(sort);
    }
}
