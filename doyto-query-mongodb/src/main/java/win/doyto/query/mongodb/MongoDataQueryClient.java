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
import org.bson.Document;
import org.bson.conversions.Bson;
import win.doyto.query.core.DataQueryClient;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.entity.Persistable;
import win.doyto.query.mongodb.filter.MongoFilterBuilder;
import win.doyto.query.util.BeanUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * MongoDataQuery
 *
 * @author f0rb on 2022-01-25
 */
@AllArgsConstructor
public class MongoDataQueryClient implements DataQueryClient {

    private static final Document SORT_BY_ID = new Document("_id", 1);

    private MongoClient mongoClient;

    @Override
    public <V extends Persistable<I>, I extends Serializable, Q extends DoytoQuery> List<V> query(Q query, Class<V> viewClass) {
        AggregationMetadata md = AggregationMetadata.build(viewClass, mongoClient);
        return md.getCollection().aggregate(Arrays.asList(md.getGroupBy(), buildSort(query), md.getProject()))
                 .map(document -> BeanUtil.parse(document.toJson(), viewClass))
                 .into(new ArrayList<>());
    }

    private <Q extends DoytoQuery> Bson buildSort(Q query) {
        Bson sort = SORT_BY_ID;
        if (query.getSort() != null) {
            sort = MongoFilterBuilder.buildSort(query.getSort());
        }
        return Aggregates.sort(sort);
    }

    @Override
    public <V extends Persistable<I>, I extends Serializable, Q extends DoytoQuery>
    long count(Q query, Class<V> viewClass) {
        return 0;
    }
}
