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

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.BsonField;
import lombok.Getter;
import org.bson.Document;
import org.bson.conversions.Bson;
import win.doyto.query.annotation.DomainPath;
import win.doyto.query.annotation.GroupBy;
import win.doyto.query.mongodb.filter.MongoGroupBuilder;
import win.doyto.query.util.ColumnUtil;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import javax.persistence.Entity;

import static win.doyto.query.mongodb.MongoConstant.MONGO_ID;

/**
 * AggregationMetadata
 *
 * @author f0rb on 2022-01-27
 */
@Getter
public class AggregationMetadata {
    private static final Map<Class<?>, AggregationMetadata> holder = new HashMap<>();

    private final MongoCollection<Document> collection;
    private final Bson groupBy;
    private final Bson project;
    private final Field[] domainFields;
    private final Document groupId;

    private <V> AggregationMetadata(Class<V> viewClass, MongoClient mongoClient) {
        this.collection = getCollection(mongoClient, viewClass.getAnnotation(Entity.class));
        this.groupId = buildGroupId(viewClass);
        this.groupBy = buildGroupBy(viewClass, this.groupId);
        this.project = buildProject(viewClass);
        this.domainFields = buildDomainFields(viewClass);
    }

    private <V> Field[] buildDomainFields(Class<V> viewClass) {
        return ColumnUtil.filterFields(viewClass, field -> field.isAnnotationPresent(DomainPath.class))
                         .toArray(Field[]::new);
    }

    public static AggregationMetadata build(Class<?> viewClass, MongoClient mongoClient) {
        return holder.computeIfAbsent(viewClass, clazz -> new AggregationMetadata(clazz, mongoClient));
    }

    private static MongoCollection<Document> getCollection(MongoClient mongoClient, Entity mongoEntity) {
        MongoDatabase database = mongoClient.getDatabase(mongoEntity.database());
        return database.getCollection(mongoEntity.name());
    }

    private static <V> Bson buildGroupBy(Class<V> viewClass, Document groupDoc) {
        List<BsonField> fieldAccumulators = buildAggregation(viewClass);
        if (groupDoc.isEmpty() && fieldAccumulators.isEmpty()) {
            return null;
        }
        return Aggregates.group(groupDoc, fieldAccumulators);
    }

    private static <V> List<BsonField> buildAggregation(Class<V> viewClass) {
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
                id.append(fieldName, "$" + fieldName);
            }
        }
        return id;
    }

    private static <V> Bson buildProject(Class<V> viewClass) {
        Field[] fields = ColumnUtil.initFields(viewClass);
        Document columns = new Document(MONGO_ID, 0); // don't want to show _id
        for (Field field : fields) {
            String column = field.getName();
            if (isManyToOneField(field)) {
                columns.append(column, new Document("$arrayElemAt", Arrays.asList("$" + column, 0)));
            } else {
                columns.append(column, "$" + column);
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

}
