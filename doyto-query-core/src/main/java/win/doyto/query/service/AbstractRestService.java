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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * AbstractRestController
 *
 * @author f0rb on 2019-05-26
 */
@SuppressWarnings("squid:S4529")
public class AbstractRestService<E extends Persistable<I>, I extends Serializable, Q extends PageQuery,
    R extends EntityRequest<E>, S extends EntityResponse<E, S>>
    extends AbstractCrudService<E, I, Q> implements RestService<E, I, Q, R, S> {

    private final S noumenon;

    @SuppressWarnings("unchecked")
    @SneakyThrows
    public AbstractRestService() {
        Type type = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[4];
        Class<S> clazz = (Class<S>) type;
        Constructor<S> constructor = clazz.getDeclaredConstructor();
        noumenon = constructor.newInstance();
    }

    protected EntityResponse<E, S> getEntityView() {
        return noumenon;
    }

    @GetMapping
    public Object queryOrPage(@Validated(PageGroup.class) Q q) {
        return q.needPaging() ? page(q) : list(q);
    }

    @Override
    public List<S> list(Q q) {
        return query(q, getEntityView()::from);
    }

    @Override
    public PageList<S> page(Q q) {
        return page(q, getEntityView()::from);
    }

    @Override
    @GetMapping("{id}")
    public S getById(@PathVariable I id) {
        E e = get(id);
        if (e == null) {
            throw new IllegalArgumentException("Record not found");
        }
        return getEntityView().from(e);
    }

    @Override
    @DeleteMapping("{id}")
    public void deleteById(@PathVariable I id) {
        E e = delete(id);
        if (e == null) {
            throw new IllegalArgumentException("Record not found");
        }
    }

    @Override
    @PutMapping("{id}")
    public void update(@RequestBody @Validated(UpdateGroup.class) R request) {
        update(request.toEntity());
    }


    @Override
    @PatchMapping("{id}")
    public void patch(@RequestBody @Validated(PatchGroup.class) R request) {
        patch(request.toEntity());
    }

    @Override
    @PostMapping
    public void create(@RequestBody @Validated(CreateGroup.class) R request) {
        create(request.toEntity());
    }

}
