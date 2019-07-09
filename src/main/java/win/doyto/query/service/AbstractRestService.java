package win.doyto.query.service;

import lombok.SneakyThrows;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import win.doyto.query.core.PageQuery;
import win.doyto.query.entity.EntityRequest;
import win.doyto.query.entity.EntityResponse;
import win.doyto.query.entity.Persistable;
import win.doyto.query.validation.CreateGroup;
import win.doyto.query.validation.PageGroup;
import win.doyto.query.validation.PatchGroup;
import win.doyto.query.validation.UpdateGroup;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.List;

/**
 * AbstractRestController
 *
 * @author f0rb on 2019-05-26
 */
@SuppressWarnings("squid:S4529")
public abstract class AbstractRestService<E extends Persistable<I>, I extends Serializable, Q extends PageQuery,
    R extends EntityRequest<E>, S extends EntityResponse<E, S>>
    extends AbstractCrudService<E, I, Q> implements RestService<E, I, Q, R, S> {

    private final S noumenon;

    @SuppressWarnings("unchecked")
    @SneakyThrows
    public AbstractRestService() {
        Class<S> clazz = (Class<S>) getActualTypeArguments()[4];
        Constructor<S> constructor = clazz.getDeclaredConstructor();
        noumenon = constructor.newInstance();
    }

    protected EntityResponse<E, S> getEntityView() {
        return noumenon;
    }

    protected void checkResult(E e) {
        if (e == null) {
            throw new EntityNotFoundException();
        }
    }

    @GetMapping
    public Object queryOrPage(@Validated(PageGroup.class) Q q) {
        return q.needPaging() ? new PageList<>(list(q), count(q)) : list(q);
    }

    @Override
    public List<S> list(Q q) {
        return query(q, getEntityView()::buildBy);
    }

    @Override
    @GetMapping("{id}")
    public S getById(@PathVariable I id) {
        E e = get(id);
        checkResult(e);
        return getEntityView().buildBy(e);
    }

    @Override
    @DeleteMapping("{id}")
    public void deleteById(@PathVariable I id) {
        checkResult(delete(id));
    }

    @Override
    @PutMapping("{id}")
    public void update(@PathVariable I id, @RequestBody @Validated(UpdateGroup.class) R request) {
        E e = get(id);
        checkResult(e);
        update(request.toEntity(e));
    }

    @Override
    @PatchMapping("{id}")
    public void patch(@PathVariable I id, @RequestBody @Validated(PatchGroup.class) R request) {
        E e = request.toEntity();
        e.setId(id);
        patch(e);
    }

    @Override
    @PostMapping
    public void create(@RequestBody @Validated(CreateGroup.class) R request) {
        save(request.toEntity());
    }

}
