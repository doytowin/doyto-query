package win.doyto.query.web.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import win.doyto.query.core.PageQuery;
import win.doyto.query.entity.Persistable;
import win.doyto.query.service.AbstractCrudService;
import win.doyto.query.service.PageList;
import win.doyto.query.util.BeanUtil;
import win.doyto.query.validation.CreateGroup;
import win.doyto.query.validation.PageGroup;
import win.doyto.query.validation.PatchGroup;
import win.doyto.query.validation.UpdateGroup;
import win.doyto.query.web.response.ErrorCode;
import win.doyto.query.web.response.JsonBody;
import win.doyto.query.web.response.PresetErrorCode;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AbstractIQRSController
 *
 * @author f0rb on 2020-01-29
 */
@JsonBody
public abstract class AbstractIQRSController<E extends Persistable<I>, I extends Serializable, Q extends PageQuery, R, S>
        extends AbstractCrudService<E, I, Q>
        implements RestApi<I, Q, R, S> {

    private final Class<S> responseClass;

    public AbstractIQRSController() {
        this(4);
    }

    @SuppressWarnings("unchecked")
    public AbstractIQRSController(int sTypeIndex) {
        this.responseClass = (Class<S>) BeanUtil.getActualTypeArguments(getClass())[sTypeIndex];
    }

    protected E checkResult(I id, E e) {
        ErrorCode.assertNotNull(e, PresetErrorCode.ENTITY_NOT_FOUND, entityClass.getSimpleName() + (":" + id).replaceAll("[\n\r\t]", " "));
        return e;
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

    @GetMapping
    public Object queryOrPage(@Validated(PageGroup.class) Q q) {
        return q.needPaging() ? page(q) : list(q);
    }

    @Override
    @GetMapping("{id}")
    public S getById(@PathVariable I id) {
        return buildResponse(checkResult(id, super.get(id)));
    }

    @Override
    @DeleteMapping("{id}")
    public S deleteById(@PathVariable I id) {
        return buildResponse(checkResult(id, super.delete(id)));
    }

    @PostMapping
    public void add(@RequestBody @Validated(CreateGroup.class) R request) {
        super.create(buildEntity(request));
    }

    @PutMapping("{id}")
    public void update(@PathVariable I id, @RequestBody @Validated(UpdateGroup.class) R request) {
        E e = get(id);
        checkResult(id, e);
        buildEntity(e, request).setId(id);
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
    public void add(@RequestBody @Validated(CreateGroup.class) List<R> requests) {
        super.batchInsert(requests.stream().map(this::buildEntity).collect(Collectors.toList()));
    }
}
