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
import org.bson.Document;
import win.doyto.query.core.DataQuery;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.entity.MongoEntity;
import win.doyto.query.mongodb.filter.MongoGroupBuilder;
import win.doyto.query.util.BeanUtil;
import win.doyto.query.util.ColumnUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MongoDataQuery
 *
 * @author f0rb on 2022-01-25
 */
public class MongoDataQuery implements DataQuery {

    private MongoClient mongoClient;

    public MongoDataQuery(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    @Override
    public <V, Q extends DoytoQuery> List<V> query(Q query, Class<V> viewClass) {
        MongoEntity table = viewClass.getAnnotation(MongoEntity.class);
        MongoDatabase database = mongoClient.getDatabase(table.database());
        MongoCollection<Document> collection = database.getCollection(table.collection());

        Field[] fields = ColumnUtil.initFields(viewClass);
        List<BsonField> list = Arrays.stream(fields)
                                     .map(field -> MongoGroupBuilder.getBsonField(field.getName()))
                                     .collect(Collectors.toList());

        return collection.aggregate(Arrays.asList(Aggregates.group(null, list)))
                         .map(document -> BeanUtil.parse(document.toJson(), viewClass))
                         .into(new ArrayList<>());
    }

    @Override
    public <V, Q extends DoytoQuery> Long count(Q query, Class<V> viewClass) {
        return null;
    }
}
