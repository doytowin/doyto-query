package win.doyto.query.service;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import win.doyto.query.core.PageQuery;
import win.doyto.query.entity.Persistable;
import win.doyto.query.validation.CreateGroup;
import win.doyto.query.validation.PageGroup;
import win.doyto.query.validation.PatchGroup;
import win.doyto.query.validation.UpdateGroup;

import java.io.Serializable;

/**
 * SimpleRestController
 *
 * @author f0rb on 2019-05-26
 */
public abstract class SimpleRestController<E extends Persistable<I>, I extends Serializable, Q extends PageQuery>
    extends AbstractCrudService<E, I, Q> {

    protected E checkResult(E e) {
        if (e == null) {
            throw new EntityNotFoundException();
        }
        return e;
    }

    @GetMapping
    public Object queryOrPage(@Validated(PageGroup.class) Q q) {
        return q.needPaging() ? new PageList<>(query(q), count(q)) : query(q);
    }

    @GetMapping("{id}")
    public E getById(@PathVariable I id) {
        return checkResult(get(id));
    }

    @DeleteMapping("{id}")
    public void deleteById(@PathVariable I id) {
        checkResult(delete(id));
    }

    @PostMapping
    public void post(@RequestBody @Validated(CreateGroup.class) E request) {
        save(request);
    }

    @PutMapping("{id}")
    public void update(@PathVariable I id, @RequestBody @Validated(UpdateGroup.class) E e) {
        e.setId(id);
        save(e);
    }

    @PatchMapping("{id}")
    public void patch(@PathVariable I id, @RequestBody @Validated(PatchGroup.class) E e) {
        e.setId(id);
        patch(e);
    }

}
