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

    <E, Q extends DoytoQuery> List<E> query(Q query, Class<E> entityClass);

    <E, Q extends DoytoQuery> Long count(Q query, Class<E> entityClass);

    default <E, Q extends DoytoQuery> PageList<E> page(Q q, Class<E> clazz) {
        return new PageList<>(query(q, clazz), count(q, clazz));
    }
}
