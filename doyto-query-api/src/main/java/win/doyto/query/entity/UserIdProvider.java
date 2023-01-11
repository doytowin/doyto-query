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

package win.doyto.query.entity;

import java.io.Serializable;

/**
 * UserIdProvider
 *
 * @author f0rb
 */
public interface UserIdProvider<I extends Serializable> {

    I getUserId();

    @SuppressWarnings("unchecked")
    default void setupUserId(Object e) {
        I userId = getUserId();
        if (userId != null) {
            if (e instanceof Persistable && ((Persistable<?>) e).isNew() && e instanceof CreateUserAware) {
                CreateUserAware<I> createUserAware = (CreateUserAware<I>) e;
                createUserAware.setCreateUserId(userId);
            }
            if (e instanceof UpdateUserAware) {
                UpdateUserAware<I> updateUserAware = (UpdateUserAware<I>) e;
                updateUserAware.setUpdateUserId(userId);
            }
        }
    }

}