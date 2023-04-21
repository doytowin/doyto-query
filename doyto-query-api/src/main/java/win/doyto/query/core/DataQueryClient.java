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

package win.doyto.query.core;

import win.doyto.query.entity.Persistable;

import java.io.Serializable;
import java.util.List;

/**
 * DataQueryClient
 *
 * @author f0rb on 2021-12-26
 * @since 0.3.1
 */
public interface DataQueryClient {

    <V extends Persistable<I>, I extends Serializable, Q extends DoytoQuery>
    List<V> query(Q query, Class<V> viewClass);

    <V extends Persistable<I>, I extends Serializable, Q extends DoytoQuery>
    long count(Q query, Class<V> viewClass);

    default <V extends Persistable<I>, I extends Serializable, Q extends DoytoQuery>
    PageList<V> page(Q query, Class<V> viewClass) {
        query.forcePaging();
        return new PageList<>(query(query, viewClass), count(query, viewClass));
    }

    default <V extends Persistable<I>, I extends Serializable, Q extends RelationalQuery<V, I>>
    List<V> query(Q query) {
        return query(query, query.getDomainClass());
    }

    default <V extends Persistable<I>, I extends Serializable, Q extends RelationalQuery<V, I>>
    long count(Q query) {
        return count(query, query.getDomainClass());
    }

    default <V extends Persistable<I>, I extends Serializable, Q extends RelationalQuery<V, I>>
    PageList<V> page(Q query) {
        return page(query, query.getDomainClass());
    }

    <V, Q extends AggregationQuery>
    List<V> aggregate(Q query, Class<V> viewClass);
}
