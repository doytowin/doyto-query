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

package win.doyto.query.sql;

import win.doyto.query.core.DoytoQuery;
import win.doyto.query.core.IdWrapper;
import win.doyto.query.entity.Persistable;

import java.util.List;

/**
 * SqlBuilder
 *
 * @author f0rb on 2021-11-21
 */
public interface SqlBuilder<E extends Persistable<?>> {

    SqlAndArgs buildCountAndArgs(DoytoQuery query);

    SqlAndArgs buildSelectColumnsAndArgs(DoytoQuery query, String... columns);

    SqlAndArgs buildSelectById(IdWrapper<?> idWrapper, String... columns);

    SqlAndArgs buildSelectIdAndArgs(DoytoQuery query);

    SqlAndArgs buildCreateAndArgs(E testEntity);

    SqlAndArgs buildCreateAndArgs(Iterable<E> entities, String... columns);

    SqlAndArgs buildUpdateAndArgs(E entity);

    SqlAndArgs buildPatchAndArgsWithId(E entity);

    SqlAndArgs buildDeleteById(IdWrapper<?> w);

    SqlAndArgs buildDeleteByIdIn(IdWrapper<?> w, List<?> ids);

    SqlAndArgs buildPatchAndArgsWithIds(E entity, List<?> ids);

}
