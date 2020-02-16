package win.doyto.query.demo.controller;

import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import win.doyto.query.core.PageQuery;
import win.doyto.query.entity.Persistable;
import win.doyto.query.service.CrudService;
import win.doyto.query.service.PageList;
import win.doyto.query.validation.CreateGroup;
import win.doyto.query.validation.PageGroup;
import win.doyto.query.validation.PatchGroup;
import win.doyto.query.validation.UpdateGroup;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.EntityNotFoundException;

/**
 * AbstractRestController
 *
 * @author f0rb on 2020-01-29
 */
public abstract class AbstractRestController<E extends Persistable<I>, I extends Serializable, Q extends PageQuery, R, S>
        implements RestApi<I, Q, R, S> {

    protected abstract CrudService<E, I, Q> getService();

    protected abstract S buildResponse(E e);

    protected abstract E buildEntity(R r);

    protected E buildEntity(E e, R r) {
        BeanUtils.copyProperties(r, e);
        return e;
    }

    protected void checkResult(E e) {
        if (e == null) {
            throw new EntityNotFoundException();
        }
    }

    @GetMapping
    public Object queryOrPage(@Validated(PageGroup.class) Q q) {
        return q.needPaging() ? page(q) : list(q);
    }

    @Override
    public PageList<S> page(Q q) {
        if (!q.needPaging()) {
            q.setPageNumber(0);
        }
        return new PageList<>(list(q), getService().count(q));
    }

    public List<S> list(Q q) {
        return getService().query(q, this::buildResponse);
    }

    @GetMapping("{id}")
    public S getById(@PathVariable I id) {
        E e = getService().get(id);
        checkResult(e);
        return buildResponse(e);
    }

    @DeleteMapping("{id}")
    public S deleteById(@PathVariable I id) {
        E e = getService().delete(id);
        checkResult(e);
        return buildResponse(e);
    }

    @PutMapping("{id}")
    public void update(@PathVariable I id, @RequestBody @Validated(UpdateGroup.class) R request) {
        E e = getService().get(id);
        checkResult(e);
        buildEntity(e, request).setId(id);
        getService().update(e);
    }

    @PatchMapping("{id}")
    public void patch(@PathVariable I id, @RequestBody @Validated(PatchGroup.class) R request) {
        E e = buildEntity(request);
        e.setId(id);
        getService().patch(e);
    }

    @PostMapping
    public void add(@RequestBody @Validated(CreateGroup.class) R request) {
        getService().create(buildEntity(request));
    }

    @PostMapping("batch")
    public void add(@RequestBody @Validated(CreateGroup.class) List<R> requests) {
        getService().batchInsert(requests.stream().map(this::buildEntity).collect(Collectors.toList()));
    }

}
