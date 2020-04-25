package win.doyto.query.web.controller;

import win.doyto.query.core.PageQuery;
import win.doyto.query.service.PageList;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * RestApi
 *
 * @author f0rb on 2019-05-28
 */
public interface RestApi<I extends Serializable, Q extends PageQuery, R, S> {

    List<S> list(Q q);

    PageList<S> page(Q q);

    S getById(I id);

    S deleteById(I id);

    void update(I id, R request);

    void patch(I id, R request);

    default void add(R request) {
        add(Collections.singletonList(request));
    }

    void add(List<R> requests);

}
