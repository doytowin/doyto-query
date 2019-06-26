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

    protected void checkResult(int count) {
        if (count < 1) {
            throw new EntityNotFoundException();
        }
    }

    protected void checkResult(E e) {
        if (e == null) {
            throw new EntityNotFoundException();
        }
    }

    @GetMapping
    public Object queryOrPage(@Validated(PageGroup.class) Q q) {
        return q.needPaging() ? new PageList<>(query(q), count(q)) : query(q);
    }

    @GetMapping("{id}")
    public E getById(@PathVariable I id) {
        E e = get(id);
        checkResult(e);
        return e;
    }

    @DeleteMapping("{id}")
    public void deleteById(@PathVariable I id) {
        checkResult(delete(id));
    }

    @PostMapping
    public void create(@RequestBody @Validated(CreateGroup.class) E request) {
        super.create(request);
    }

    @PutMapping("{id}")
    public void update(@PathVariable I id, @RequestBody @Validated(UpdateGroup.class) E e) {
        e.setId(id);
        checkResult(update(e));
    }

    @PatchMapping("{id}")
    public void patch(@PathVariable I id, @RequestBody @Validated(PatchGroup.class) E e) {
        e.setId(id);
        checkResult(patch(e));
    }

}
