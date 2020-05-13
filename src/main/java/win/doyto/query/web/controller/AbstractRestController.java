package win.doyto.query.web.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import win.doyto.query.core.IdWrapper;
import win.doyto.query.core.PageQuery;
import win.doyto.query.entity.Persistable;
import win.doyto.query.service.CrudService;
import win.doyto.query.validation.UpdateGroup;
import win.doyto.query.web.response.JsonBody;

import java.io.Serializable;

/**
 * AbstractRestController
 *
 * @author f0rb on 2020-01-29
 */
@JsonBody
public abstract class AbstractRestController<E extends Persistable<I>, I extends Serializable, Q extends PageQuery, R, S>
        extends AbstractController<E, I, Q, R, S, IdWrapper<I>>
        implements RestApi<I, Q, R, S> {

    @SuppressWarnings("java:S2387")
    protected final CrudService<E, I, Q> service;

    @SuppressWarnings("unchecked")
    public AbstractRestController(CrudService<E, I, Q> service) {
        super(service, new TypeReference<IdWrapper.Simple<I>>() {});
        this.service = service;
    }

    @Override
    @GetMapping("{id}")
    public S get(@PathVariable I id) {
        E e = service.get(id);
        checkResult(e);
        return buildResponse(e);
    }

    @Override
    public S delete(@PathVariable I id) {
        E e = service.delete(id);
        checkResult(e);
        return buildResponse(e);
    }

    @Override
    @PutMapping("{id}")
    public void update(@PathVariable I id, @RequestBody @Validated(UpdateGroup.class) R request) {
        super.update(IdWrapper.build(id), request);
    }

}
