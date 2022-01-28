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

package win.doyto.query.mongodb;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.BsonField;
import lombok.Getter;
import org.bson.Document;
import org.bson.conversions.Bson;
import win.doyto.query.annotation.Aggregation;
import win.doyto.query.entity.MongoEntity;
import win.doyto.query.mongodb.filter.MongoGroupBuilder;
import win.doyto.query.util.ColumnUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private <V> AggregationMetadata(Class<V> viewClass, MongoClient mongoClient) {
        this.collection = getCollection(viewClass, mongoClient);
        this.groupBy = buildGroupBy(viewClass);
        this.project = buildProject(viewClass);
    }

    static AggregationMetadata build(Class<?> viewClass, MongoClient mongoClient) {
        return holder.computeIfAbsent(viewClass, clazz -> new AggregationMetadata(clazz, mongoClient));
    }

    private <V> MongoCollection<Document> getCollection(Class<V> viewClass, MongoClient mongoClient) {
        MongoEntity table = viewClass.getAnnotation(MongoEntity.class);
        MongoDatabase database = mongoClient.getDatabase(table.database());
        return database.getCollection(table.collection());
    }

    private <V> Bson buildGroupBy(Class<V> viewClass) {
        return Aggregates.group(buildGroupId(viewClass), buildAggregation(viewClass));
    }

    private <V> List<BsonField> buildAggregation(Class<V> viewClass) {
        Field[] fields = ColumnUtil.initFields(viewClass);
        List<BsonField> list = new ArrayList<>();
        for (Field field : fields) {
            BsonField bsonField = MongoGroupBuilder.getBsonField(field.getName());
            if (bsonField != null) {
                list.add(bsonField);
            }
        }
        return list;
    }

    private <V> Bson buildGroupId(Class<V> viewClass) {
        Aggregation aggregation = viewClass.getAnnotation(Aggregation.class);
        Document id = new Document();
        if (aggregation != null) {
            for (String field : aggregation.groupBy()) {
                id.append(field, "$" + field);
            }
        }
        return id;
    }

    private <V> Bson buildProject(Class<V> viewClass) {
        Field[] fields = ColumnUtil.initFields(viewClass);
        Document columns = new Document("_id", 0);
        for (Field field : fields) {
            String column = field.getName();
            columns.append(column, "$" + column);
        }
        Aggregation aggregation = viewClass.getAnnotation(Aggregation.class);
        if (aggregation != null) {
            for (String field : aggregation.groupBy()) {
                columns.append(field, "$_id." + field);
            }
        }

        return Aggregates.project(columns);
    }

}
