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
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.core.IdWrapper;
import win.doyto.query.core.PageList;
import win.doyto.query.entity.Persistable;
import win.doyto.query.service.DynamicService;
import win.doyto.query.util.BeanUtil;
import win.doyto.query.web.component.ListValidator;
import win.doyto.query.web.response.ErrorCode;
import win.doyto.query.web.response.JsonBody;
import win.doyto.query.web.response.PresetErrorCode;

import javax.annotation.Resource;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * AbstractController
 *
 * @author f0rb on 2020-05-05
 */
@JsonBody
abstract class AbstractController<
        E extends Persistable<I>,
        I extends Serializable,
        Q extends DoytoQuery,
        R, S,
        W extends IdWrapper<I>,
        C extends DynamicService<E, I, Q>
        > {

    @Resource
    protected ListValidator listValidator = new ListValidator();

    private final Class<E> entityClass;
    private final TypeReference<W> typeReference;
    protected C service;
    protected Function<E, S> e2rspTransfer;
    protected Function<R, E> req2eTransfer;

    @SuppressWarnings("unchecked")
    protected AbstractController(C service, TypeReference<W> typeReference) {
        this.service = service;
        this.typeReference = typeReference;
        Type[] types = BeanUtil.getActualTypeArguments(getClass());
        if (!(types[0] instanceof Class)) {
            throw new ControllerDefinitionException("Miss type parameters.");
        }
        this.entityClass = (Class<E>) types[0];

        checkController();

        req2eTransfer = r -> (E) r;
        e2rspTransfer = e -> (S) e;
        if (types.length > 4) {
            if (!entityClass.equals(types[3])) {
                req2eTransfer = r -> BeanUtil.convertTo(r, entityClass);
            }
            if (!entityClass.equals(types[4])) {
                Class<S> responseClass = (Class<S>) types[4];
                e2rspTransfer = e -> BeanUtil.convertTo(e, responseClass);
            }
        }
    }

    private void checkController() {
        Class<? extends AbstractController> clazz = getClass();

        if (!clazz.isAnnotationPresent(RequestMapping.class)) {
            throw new ControllerDefinitionException("Miss @RequestMapping annotation.");
        } else if (clazz.getAnnotation(RequestMapping.class).value().length == 0) {
            throw new ControllerDefinitionException("@RequestMapping has no values.");
        }

        if (!clazz.isAnnotationPresent(RestController.class)) {
            throw new ControllerDefinitionException("Miss @RestController annotation.");
        }
    }

    protected S buildResponse(E e) {
        return e2rspTransfer.apply(e);
    }

    protected E buildEntity(R r) {
        return req2eTransfer.apply(r);
    }

    protected void checkResult(E e) {
        ErrorCode.assertNotNull(e, PresetErrorCode.ENTITY_NOT_FOUND);
    }

    public PageList<S> page(Q q) {
        q.forcePaging();
        return new PageList<>(this.query(q), service.count(q));
    }

    public List<S> query(Q q) {
        return service.query(q, this::buildResponse);
    }

    public long count(Q q) {
        return service.count(q);
    }

    public void patch(R request) {
        E e = buildEntity(request);
        int count = service.patch(e);
        ErrorCode.assertTrue(count == 1, PresetErrorCode.ENTITY_NOT_FOUND);
    }

    public void update(R request) {
        W w = BeanUtil.convertTo(request, typeReference);
        E e = service.get(w);
        checkResult(e);
        BeanUtil.copyTo(request, e).setId(w.getId());
        service.update(e);
    }

    public void create(List<R> requests) {
        listValidator.validateList(requests);
        if (requests.size() == 1) {
            service.create(buildEntity(requests.get(0)));
        } else {
            service.create(requests.stream().map(this::buildEntity).collect(Collectors.toList()));
        }
    }

    @Resource
    public void setBeanFactory(AutowireCapableBeanFactory beanFactory) throws BeansException {
        if (service.getClass().isAnonymousClass()) {
            beanFactory.autowireBean(service);
            beanFactory.initializeBean(service, this.getClass().getName() + ".DynamicService");
        }
    }
}
