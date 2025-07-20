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

import com.fasterxml.jackson.core.type.TypeReference;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.core.IdWrapper;
import win.doyto.query.entity.Persistable;
import win.doyto.query.service.DynamicService;
import win.doyto.query.web.response.JsonBody;

import java.io.Serializable;

/**
 * AbstractRestController
 *
 * @author f0rb on 2020-01-29
 */
@JsonBody
public abstract class AbstractDynamicController
        <E extends Persistable<I>, I extends Serializable, Q extends DoytoQuery, R, S, W extends IdWrapper<I>>
        extends AbstractController<E, I, Q, R, S, W, DynamicService<E, I, Q>>
        implements RestApi<W, Q, R, S> {

    protected AbstractDynamicController(DynamicService<E, I, Q> service, TypeReference<W> typeReference) {
        super(service, typeReference);
    }

    @Override
    public S get(W w) {
        E e = service.get(w);
        checkResult(e);
        return buildResponse(e);
    }

    @Override
    public S remove(W w) {
        E e = service.remove(w);
        checkResult(e);
        return buildResponse(e);
    }

    @Override
    public int delete(Q q) {
        return service.delete(q);
    }

    @Override
    public int patch(R request, Q q) {
        E e = buildEntity(request);
        return service.patch(e, q);
    }

}
