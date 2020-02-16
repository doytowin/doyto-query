package win.doyto.query.demo.controller;

import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import win.doyto.query.core.PageQuery;
import win.doyto.query.entity.Persistable;
import win.doyto.query.service.AbstractCrudService;
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
 * AbstractIQRSController
 *
 * @author f0rb on 2020-01-29
 */
public abstract class AbstractIQRSController<E extends Persistable<I>, I extends Serializable, Q extends PageQuery, R, S>
        extends AbstractCrudService<E, I, Q>
        implements RestApi<I, Q, R, S> {

    protected E checkResult(E e) {
        if (e == null) {
            throw new EntityNotFoundException();
        }
        return e;
    }

    protected abstract S buildResponse(E e);

    protected abstract E buildEntity(R r);

    protected E buildEntity(R r, E e) {
        BeanUtils.copyProperties(r, e);
        return e;
    }

    @GetMapping
    public Object queryOrPage(@Validated(PageGroup.class) Q q) {
        return q.needPaging() ? page(q) : list(q);
    }

    @Override
    @GetMapping("{id}")
    public S getById(@PathVariable I id) {
        return buildResponse(checkResult(super.get(id)));
    }

    @Override
    @DeleteMapping("{id}")
    public S deleteById(@PathVariable I id) {
        return buildResponse(checkResult(super.delete(id)));
    }

    @PostMapping
    public void add(@RequestBody @Validated(CreateGroup.class) R request) {
        super.create(buildEntity(request));
    }

    @PutMapping("{id}")
    public void update(@PathVariable I id, @RequestBody @Validated(UpdateGroup.class) R request) {
        E e = get(id);
        checkResult(e);
        buildEntity(request, e).setId(id);
        update(e);
    }

    @PatchMapping("{id}")
    public void patch(@PathVariable I id, @RequestBody @Validated(PatchGroup.class) R request) {
        E e = buildEntity(request);
        e.setId(id);
        patch(e);
    }

    @Override
    public List<S> list(Q q) {
        return query(q, this::buildResponse);
    }

    @Override
    public PageList<S> page(Q q) {
        return super.page(q, this::buildResponse);
    }

    @PostMapping("batch")
    public void add(List<R> requests) {
        super.batchInsert(requests.stream().map(this::buildEntity).collect(Collectors.toList()));
    }
}
