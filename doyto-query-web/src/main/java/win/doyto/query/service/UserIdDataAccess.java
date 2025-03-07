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

import lombok.AllArgsConstructor;
import lombok.experimental.Delegate;
import win.doyto.query.core.DataAccess;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.entity.Persistable;
import win.doyto.query.entity.UserIdProvider;

import java.io.Serializable;

/**
 * UserIdDataAccess
 *
 * @author f0rb on 2023/6/21
 * @since 1.0.2
 */
@AllArgsConstructor
public class UserIdDataAccess<E extends Persistable<I>, I extends Serializable, Q extends DoytoQuery> implements DataAccess<E, I, Q> {
    @Delegate
    private final DataAccess<E, I, Q> delegate;
    private final UserIdProvider<?> userIdProvider;

    @Override
    public void create(E e) {
        userIdProvider.setupUserId(e);
        delegate.create(e);
    }

    @Override
    public int batchInsert(Iterable<E> entities, String... columns) {
        if (userIdProvider.getUserId() != null) {
            for (E e : entities) {
                userIdProvider.setupUserId(e);
            }
        }
        return delegate.batchInsert(entities, columns);
    }

    @Override
    public int update(E e) {
        userIdProvider.setupPatchUserId(e);
        return delegate.update(e);
    }

    @Override
    public int patch(E e) {
        userIdProvider.setupPatchUserId(e);
        return delegate.patch(e);
    }

    public int patch(E e, Q q) {
        userIdProvider.setupPatchUserId(e);
        return delegate.patch(e, q);
    }
}
