package win.doyto.query.web.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import win.doyto.query.core.IdWrapper;
import win.doyto.query.core.PageQuery;
import win.doyto.query.entity.Persistable;
import win.doyto.query.service.DynamicService;
import win.doyto.query.web.response.JsonBody;

import java.io.Serializable;

/**
 * AbstractRestController
 *
 * @author f0rb on 2020-01-29
 */
@JsonBody
@SuppressWarnings("unchecked")
public abstract class AbstractDynamicController
        <E extends Persistable<I>, I extends Serializable, Q extends PageQuery, R, S, W extends IdWrapper<I>>
        extends AbstractController<E, I, Q, R, S, W>
        implements RestApi<W, Q, R, S> {

    protected AbstractDynamicController(DynamicService<E, I, Q> service, TypeReference<W> typeReference) {
        super(service, typeReference);
    }

    @Override
    public S get(W w) {
        E e = service.get(w);
        checkResult(e);
        return buildResponse(e);
    }

    @Override
    public S delete(W w) {
        E e = service.delete(w);
        checkResult(e);
        return buildResponse(e);
    }

}
