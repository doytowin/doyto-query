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
import com.mongodb.client.model.Aggregates;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.bson.conversions.Bson;
import win.doyto.query.core.AggregationQuery;
import win.doyto.query.core.DataQueryClient;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.entity.Persistable;
import win.doyto.query.mongodb.aggregation.AggregationMetadata;
import win.doyto.query.mongodb.aggregation.AggregationPipelineBuilder;
import win.doyto.query.mongodb.session.MongoSessionSupplier;
import win.doyto.query.mongodb.session.MongoSessionThreadLocalSupplier;
import win.doyto.query.util.BeanUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * MongoDataQuery
 *
 * @author f0rb on 2022-01-25
 */
@AllArgsConstructor
public class MongoDataQueryClient implements DataQueryClient {
    public static final String COUNT_KEY = "count";
    private MongoClient mongoClient;
    private final MongoSessionSupplier mongoSessionSupplier;

    public MongoDataQueryClient(MongoClient mongoClient) {
        this(mongoClient, MongoSessionThreadLocalSupplier.create(mongoClient));
    }

    @Override
    public <V extends Persistable<I>, I extends Serializable, Q extends DoytoQuery>
    List<V> query(Q query, Class<V> viewClass) {
        return commonQuery(query, viewClass);
    }

    private <V, Q extends DoytoQuery> ArrayList<V>
    commonQuery(Q query, Class<V> viewClass) {
        AggregationMetadata md = AggregationMetadata.build(viewClass, mongoClient);
        List<Bson> list = AggregationPipelineBuilder.build(query, viewClass, md);
        return md.getCollection().aggregate(mongoSessionSupplier.get(), list)
                 .map(document -> BeanUtil.parse(document.toJson(), viewClass))
                 .into(new ArrayList<>());
    }

    @Override
    public <V extends Persistable<I>, I extends Serializable, Q extends DoytoQuery>
    long count(Q query, Class<V> viewClass) {
        AggregationMetadata md = AggregationMetadata.build(viewClass, mongoClient);
        List<Bson> list = AggregationPipelineBuilder.build(query, viewClass, md);
        list.set(list.size() - 1, Aggregates.count(COUNT_KEY));
        Integer count = md.getCollection().aggregate(mongoSessionSupplier.get(), list)
                          .map(document -> document.getInteger(COUNT_KEY, -1))
                          .first();
        return ObjectUtils.defaultIfNull(count, 0);
    }

    @Override
    public <V, Q extends AggregationQuery> List<V> aggregate(Q query, Class<V> viewClass) {
        return commonQuery(query, viewClass);
    }
}
