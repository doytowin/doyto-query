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
import win.doyto.query.core.Having;
import win.doyto.query.core.JoinQuery;
import win.doyto.query.entity.Persistable;
import win.doyto.query.mongodb.filter.MongoFilterBuilder;
import win.doyto.query.util.BeanUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static win.doyto.query.mongodb.MongoDataAccess.MONGO_ID;

/**
 * MongoDataQuery
 *
 * @author f0rb on 2022-01-25
 */
@AllArgsConstructor
public class MongoDataQueryClient implements DataQueryClient {

    private static final Document SORT_BY_ID = new Document(MONGO_ID, 1);

    private MongoClient mongoClient;

    @Override
    public <V extends Persistable<I>, I extends Serializable, Q extends JoinQuery<V, I>>
    List<V> query(Q query, Class<V> viewClass) {
        AggregationMetadata md = AggregationMetadata.build(viewClass, mongoClient);
        List<Bson> list = new ArrayList<>();
        list.add(md.getGroupBy());
        if (query instanceof JoinQuery) {
            Having having = ((JoinQuery<?, ?>) query).getHaving();
            if (having != null) {
                list.add(buildHaving(having));
            }
        }
        list.add(buildSort(query));
        list.add(md.getProject());
        list.add(Aggregates.match(MongoFilterBuilder.buildFilter(query)));
        return md.getCollection().aggregate(list)
                 .map(document -> BeanUtil.parse(document.toJson(), viewClass))
                 .into(new ArrayList<>());
    }

    private <H extends Having> Bson buildHaving(H having) {
        return Aggregates.match(MongoFilterBuilder.buildFilter(having));
    }

    private <Q extends DoytoQuery> Bson buildSort(Q query) {
        Bson sort = SORT_BY_ID;
        if (query.getSort() != null) {
            sort = MongoFilterBuilder.buildSort(query.getSort());
        }
        return Aggregates.sort(sort);
    }

    @Override
    public <V extends Persistable<I>, I extends Serializable, Q extends JoinQuery<V, I>>
    long count(Q query, Class<V> viewClass) {
        return 0;
    }
}
