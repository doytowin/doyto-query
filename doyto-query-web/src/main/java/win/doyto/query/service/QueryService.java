/*
 * Copyright © 2019-2025 DoytoWin, Inc.
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

package win.doyto.query.service;

import win.doyto.query.core.DoytoQuery;
import win.doyto.query.core.PageList;

import java.util.List;
import java.util.function.Function;

/**
 * QueryService
 *
 * @author f0rb
 */
public interface QueryService<E, Q extends DoytoQuery> {

    List<E> query(Q query);

    long count(Q query);

    PageList<E> page(Q query);

    default boolean exists(Q query) {
        return count(query) > 0;
    }

    default boolean notExists(Q query) {
        return !exists(query);
    }

    default E get(Q query) {
        return get(query, e -> e);
    }

    default <V> V get(Q query, Function<E, V> transfer) {
        query.setPageSize(1);
        List<E> list = query(query);
        return list.isEmpty() ? null : transfer.apply(list.get(0));
    }

    default <V> List<V> query(Q query, Function<E, V> transfer) {
        return query(query).stream().map(transfer).toList();
    }

    default <V> PageList<V> page(Q query, Function<E, V> transfer) {
        PageList<E> page = page(query);
        List<V> list = page.getList().stream().map(transfer).toList();
        return new PageList<>(list, page.getTotal());
    }

}
