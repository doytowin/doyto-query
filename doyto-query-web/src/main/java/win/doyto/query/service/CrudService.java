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
import win.doyto.query.core.IdWrapper;
import win.doyto.query.entity.Persistable;

import java.io.Serializable;

/**
 * CrudService
 *
 * @author f0rb
 */
public interface CrudService<E extends Persistable<I>, I extends Serializable, Q extends DoytoQuery>
        extends DynamicService<E, I, Q> {

    default E get(I id) {
        return get(IdWrapper.build(id));
    }

    /**
     * force to get a new entity object from database
     *
     * @param id entity id
     * @return a new entity object
     */
    default E fetch(I id) {
        return fetch(IdWrapper.build(id));
    }

    default E remove(I id) {
        return remove(IdWrapper.build(id));
    }

}
