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

import win.doyto.query.entity.Persistable;

import java.io.Serializable;
import java.util.List;

/**
 * DataAccess
 *
 * @author f0rb
 * @since 0.0.1
 */
public interface DataAccess<E extends Persistable<I>, I extends Serializable, Q> {

    List<E> query(Q query);

    long count(Q query);

    <V> List<V> queryColumns(Q q, Class<V> clazz, String... columns);

    default E get(I id) {
        return get(IdWrapper.build(id));
    }

    E get(IdWrapper<I> w);

    default int delete(I id) {
        return delete(IdWrapper.build(id));
    }

    int delete(IdWrapper<I> w);

    int delete(Q query);

    void create(E e);

    default int batchInsert(Iterable<E> entities, String... columns) {
        int count = 0;
        for (E entity : entities) {
            create(entity);
            count++;
        }
        return count;
    }

    int update(E e);

    int patch(E e);

    int patch(E e, Q q);

    List<I> queryIds(Q query);
}
