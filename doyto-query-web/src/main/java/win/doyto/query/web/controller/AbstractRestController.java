/*
 * Copyright © 2019-2024 Forb Yuan
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
import org.springframework.web.bind.annotation.PathVariable;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.core.IdWrapper;
import win.doyto.query.entity.Persistable;
import win.doyto.query.service.AbstractCrudService;
import win.doyto.query.service.CrudService;
import win.doyto.query.web.response.JsonBody;

import java.io.Serializable;

/**
 * AbstractRestController
 * <p>
 * 4 kind of usage: <br>
 * <ol>
 * <li>Custom service with R/S type</li>
 * <li>Custom service with E/E type</li>
 * <li>Build-in service with R/S type</li>
 * <li>Build-in service with E/E type</li>
 * </ol>
 *
 * @author f0rb on 2020-01-29
 */
@JsonBody
public abstract class AbstractRestController<E extends Persistable<I>, I extends Serializable, Q extends DoytoQuery, R, S>
        extends AbstractController<E, I, Q, R, S, IdWrapper.Simple<I>, CrudService<E, I, Q>>
        implements RestApi<I, Q, R, S> {

    protected AbstractRestController(CrudService<E, I, Q> service) {
        super(service, new TypeReference<>() {});
    }

    protected AbstractRestController() {
        this(null);
        service = new AbstractCrudService<>() {
            @Override
            protected Class<?> getConcreteClass() {
                return AbstractRestController.this.getClass();
            }
        };
    }

    @Override
    public S get(@PathVariable I id) {
        E e = service.get(id);
        checkResult(e);
        return buildResponse(e);
    }

    @Override
    public S remove(@PathVariable I id) {
        E e = service.remove(id);
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
