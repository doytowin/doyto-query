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

package win.doyto.query.core;

import win.doyto.query.service.PageList;

import java.util.List;

/**
 * DataQuery
 *
 * @author f0rb on 2021-12-26
 */
public interface DataQuery {

    <V, Q extends DoytoQuery> List<V> query(Q query, Class<V> viewClass);

    <V, Q extends DoytoQuery> Long count(Q query, Class<V> viewClass);

    default <V, Q extends DoytoQuery> PageList<V> page(Q query, Class<V> viewClass) {
        return new PageList<>(query(query, viewClass), count(query, viewClass));
    }
}
