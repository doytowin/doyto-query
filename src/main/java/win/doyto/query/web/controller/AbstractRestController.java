package win.doyto.query.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import win.doyto.query.core.IdWrapper;
import win.doyto.query.core.PageQuery;
import win.doyto.query.entity.Persistable;
import win.doyto.query.service.CrudService;
import win.doyto.query.service.PageList;
import win.doyto.query.util.BeanUtil;
import win.doyto.query.validation.PatchGroup;
import win.doyto.query.validation.UpdateGroup;
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
 * AbstractRestController
 *
 * @author f0rb on 2020-01-29
 */
@JsonBody
public abstract class AbstractRestController<E extends Persistable<I>, I extends Serializable, Q extends PageQuery, R, S>
        implements RestApi<I, Q, R, S> {

    @Resource
    private ListValidator listValidator = new ListValidator();

    private final Class<E> entityClass;
    private final Class<S> responseClass;

    @SuppressWarnings("unchecked")
    public AbstractRestController() {
        Type[] types = BeanUtil.getActualTypeArguments(getClass());
        entityClass = (Class<E>) types[0];
        responseClass = (Class<S>) types[4];
    }

    @Autowired
    protected CrudService<E, I, Q> service;

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

    @Override
    public PageList<S> page(Q q) {
        return service.page(q, this::buildResponse);
    }

    @Override
    public List<S> query(Q q) {
        return service.query(q, this::buildResponse);
    }

    @GetMapping("{id}")
    public S get(@PathVariable I id) {
        E e = service.get(id);
        checkResult(e);
        return buildResponse(e);
    }

    @DeleteMapping("{id}")
    public S delete(@PathVariable I id) {
        E e = service.delete(id);
        checkResult(e);
        return buildResponse(e);
    }

    @PutMapping("{id}")
    public void update(@PathVariable I id, @RequestBody @Validated(UpdateGroup.class) R request) {
        E e = service.get(id);
        checkResult(e);
        buildEntity(e, request).setId(id);
        service.update(e);
    }

    @PatchMapping("{id}")
    public void patch(@PathVariable I id, @RequestBody @Validated(PatchGroup.class) R request) {
        E e = buildEntity(request);
        e.setId(id);
        service.patch(e);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void update(R request) {
        IdWrapper<I> idWrapper = BeanUtil.convertTo(request, IdWrapper.Simple.class);
        E e = service.get(idWrapper);
        checkResult(e);
        buildEntity(e, request).setId(idWrapper.getId());
        service.update(e);
    }

    @Override
    public void patch(R request) {
        E e = buildEntity(request);
        service.patch(e);
    }

    @Override
    @PostMapping
    public void create(@RequestBody List<R> requests) {
        listValidator.validateList(requests);
        if (requests.size() == 1) {
            service.create(buildEntity(requests.get(0)));
        } else {
            service.create(requests.stream().map(this::buildEntity).collect(Collectors.toList()));
        }
    }

}
