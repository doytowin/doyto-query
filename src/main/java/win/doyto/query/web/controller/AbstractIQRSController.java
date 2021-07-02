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
import win.doyto.query.web.component.ListValidator;
import win.doyto.query.web.response.ErrorCode;
import win.doyto.query.web.response.JsonBody;
import win.doyto.query.web.response.PresetErrorCode;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;

/**
 * AbstractIQRSController
 *
 * @author f0rb on 2020-01-29
 */
@JsonBody
public abstract class AbstractIQRSController
        <E extends Persistable<I>, I extends Serializable, Q extends PageQuery, R, S>
        extends AbstractCrudService<E, I, Q> {

    @Resource
    private ListValidator listValidator = new ListValidator();

    private final Class<S> responseClass;

    protected AbstractIQRSController() {
        this(4);
    }

    @SuppressWarnings("unchecked")
    protected AbstractIQRSController(int sTypeIndex) {
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
    protected PageList<S> paging(@Validated(PageGroup.class) Q q) {
        return super.page(q, this::buildResponse);
    }

    @GetMapping("{id}")
    public S getById(@PathVariable I id) {
        return buildResponse(checkResult(id, super.get(id)));
    }

    @DeleteMapping("{id}")
    public S deleteById(@PathVariable I id) {
        return buildResponse(checkResult(id, super.delete(id)));
    }

    public void add(R request) {
        add(Collections.singletonList(request));
    }

    @PostMapping
    public void add(@RequestBody @Validated(CreateGroup.class) List<R> requests) {
        listValidator.validateList(requests);
        if (requests.size() == 1) {
            super.create(buildEntity(requests.get(0)));
        } else {
            super.create(requests.stream().map(this::buildEntity).collect(Collectors.toList()));
        }
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

}
