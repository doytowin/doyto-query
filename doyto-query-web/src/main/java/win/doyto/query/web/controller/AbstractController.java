package win.doyto.query.web.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import win.doyto.query.core.IdWrapper;
import win.doyto.query.core.Pageable;
import win.doyto.query.entity.Persistable;
import win.doyto.query.service.DynamicService;
import win.doyto.query.service.PageList;
import win.doyto.query.util.BeanUtil;
import win.doyto.query.web.component.ListValidator;
import win.doyto.query.web.response.ErrorCode;
import win.doyto.query.web.response.JsonBody;
import win.doyto.query.web.response.PresetErrorCode;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;

/**
 * AbstractController
 *
 * @author f0rb on 2020-05-05
 */
@JsonBody
abstract class AbstractController<
        E extends Persistable<I>,
        I extends Serializable,
        Q extends Pageable,
        R, S,
        W extends IdWrapper<I>,
        C extends DynamicService<E, I, Q>
        > {

    @Resource
    protected ListValidator listValidator = new ListValidator();

    private final Class<E> entityClass;
    private final Class<S> responseClass;
    private final TypeReference<W> typeReference;
    protected final C service;

    @SuppressWarnings("unchecked")
    protected AbstractController(C service, TypeReference<W> typeReference) {
        this.service = service;
        this.typeReference = typeReference;
        Type[] types = BeanUtil.getActualTypeArguments(getClass());
        this.entityClass = (Class<E>) types[0];
        this.responseClass = (Class<S>) types[4];
    }

    protected S buildResponse(E e) {
        return BeanUtil.convertTo(e, responseClass);
    }

    protected E buildEntity(R r) {
        return BeanUtil.convertTo(r, entityClass);
    }

    protected E buildEntity(E e, R r) {
        return BeanUtil.copyTo(r, e);
    }

    protected void checkResult(E e) {
        ErrorCode.assertNotNull(e, PresetErrorCode.ENTITY_NOT_FOUND);
    }

    public PageList<S> page(Q q) {
        return service.page(q, this::buildResponse);
    }

    public List<S> query(Q q) {
        return service.query(q, this::buildResponse);
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
        buildEntity(e, request).setId(w.getId());
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

}
