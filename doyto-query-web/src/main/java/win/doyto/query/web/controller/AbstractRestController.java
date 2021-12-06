package win.doyto.query.web.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.web.bind.annotation.PathVariable;
import win.doyto.query.core.IdWrapper;
import win.doyto.query.core.Pageable;
import win.doyto.query.entity.Persistable;
import win.doyto.query.service.CrudService;
import win.doyto.query.web.response.JsonBody;

import java.io.Serializable;

/**
 * AbstractRestController
 *
 * @author f0rb on 2020-01-29
 */
@JsonBody
public abstract class AbstractRestController<E extends Persistable<I>, I extends Serializable, Q extends Pageable, R, S>
        extends AbstractController<E, I, Q, R, S, IdWrapper.Simple<I>, CrudService<E, I, Q>>
        implements RestApi<I, Q, R, S> {

    protected AbstractRestController(CrudService<E, I, Q> service) {
        super(service, new TypeReference<IdWrapper.Simple<I>>() {});
    }

    @Override
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

}
