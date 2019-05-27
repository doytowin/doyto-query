package win.doyto.query.entity;

import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.*;
import win.doyto.query.core.CrudService;
import win.doyto.query.core.PageList;
import win.doyto.query.core.PageQuery;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * AbstractRestController
 *
 * @author f0rb on 2019-05-26
 */
@SuppressWarnings("squid:S4529")
public class AbstractRestController<E extends Persistable<I>, I extends Serializable, Q extends PageQuery,
    R extends EntityRequest<E>, S extends EntityResponse<E, S>> {

    protected CrudService<E, I, Q> crudService;

    private final S noumenon;

    @SuppressWarnings("unchecked")
    @SneakyThrows
    public AbstractRestController(CrudService<E, I, Q> crudService) {
        this.crudService = crudService;

        Type type = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[4];
        Class<S> clazz = (Class<S>) type;
        Constructor<S> constructor = clazz.getDeclaredConstructor();
        noumenon = constructor.newInstance();
    }

    protected EntityResponse<E, S> getEntityView() {
        return noumenon;
    }

    @GetMapping
    public Object query(Q q) {
        if (q.needPaging()) {
            return page(q);
        }
        return crudService.query(q, getEntityView()::from);
    }

    public PageList<S> page(Q q) {
        return crudService.page(q, getEntityView()::from);
    }

    @GetMapping("{id}")
    public S get(@PathVariable I id) {
        E e = crudService.get(id);
        if (e == null) {
            throw new IllegalArgumentException("Record not found");
        }
        return getEntityView().from(e);
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable I id) {
        E e = crudService.delete(id);
        if (e == null) {
            throw new IllegalArgumentException("Record not found");
        }
    }

    @PutMapping("{id}")
    public void update(@RequestBody R request) {
        crudService.update(request.toEntity());
    }


    @PatchMapping("{id}")
    public void patch(@RequestBody R request) {
        crudService.patch(request.toEntity());
    }

    @PostMapping
    public void create(@RequestBody R request) {
        crudService.create(request.toEntity());
    }

}
