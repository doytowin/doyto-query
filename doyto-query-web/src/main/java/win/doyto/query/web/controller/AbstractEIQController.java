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

package win.doyto.query.web.controller;

import win.doyto.query.core.DoytoQuery;
import win.doyto.query.entity.Persistable;
import win.doyto.query.web.response.ErrorCode;
import win.doyto.query.web.response.JsonBody;
import win.doyto.query.web.response.PresetErrorCode;

import java.io.Serializable;
import java.util.List;

/**
 * AbstractIQEEController
 *
 * @author f0rb on 2020-01-29
 */
@JsonBody
public abstract class AbstractEIQController<E extends Persistable<I>, I extends Serializable, Q extends DoytoQuery>
        extends AbstractRestController<E, I, Q, E, E> {

    @Override
    public List<E> query(Q q) {
        return service.query(q);
    }

    @Override
    public void update(E request) {
        int cnt = service.update(request);
        ErrorCode.assertTrue(cnt > 0, PresetErrorCode.ENTITY_NOT_FOUND);
    }
}
