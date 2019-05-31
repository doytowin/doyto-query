package win.doyto.query.service;

import win.doyto.query.core.PageQuery;
import win.doyto.query.entity.EntityRequest;
import win.doyto.query.entity.EntityResponse;
import win.doyto.query.entity.Persistable;

import java.io.Serializable;
import java.util.List;

/**
 * RestService
 *
 * @author f0rb on 2019-05-28
 */
public interface RestService<E extends Persistable<I>, I extends Serializable, Q extends PageQuery, R extends EntityRequest<E>, S extends EntityResponse<E, S>> {

    List<S> list(Q q);

    PageList<S> page(Q q);

    EntityResponse getById(I id);

    void deleteById(I id);

    void update(I id, R request);

    void patch(R request);

    void create(R request);

}
