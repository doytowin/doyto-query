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

package win.doyto.query.entity;

import java.io.Serializable;

/**
 * UserIdProvider
 *
 * @author f0rb
 */
public interface UserIdProvider<U extends Serializable> {

    U getUserId();

    @SuppressWarnings("unchecked")
    default void setupUserId(Object e) {
        U userId = getUserId();
        if (userId != null) {
            if (e instanceof Persistable && ((Persistable<?>) e).isNew() && e instanceof CreateUserAware) {
                CreateUserAware<U> createUserAware = (CreateUserAware<U>) e;
                createUserAware.setCreateUserId(userId);
            }
            if (e instanceof UpdateUserAware) {
                UpdateUserAware<U> updateUserAware = (UpdateUserAware<U>) e;
                updateUserAware.setUpdateUserId(userId);
            }
        }
    }

    @SuppressWarnings("unchecked")
    default void setupPatchUserId(Object e) {
        U userId = getUserId();
        if (userId != null && e instanceof UpdateUserAware) {
            UpdateUserAware<U> updateUserAware = (UpdateUserAware<U>) e;
            updateUserAware.setUpdateUserId(userId);
        }
    }

}