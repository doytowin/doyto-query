package win.doyto.query.data;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import win.doyto.query.core.PageQuery;
import win.doyto.query.entity.Persistable;

import java.io.Serializable;

/**
 * ReactiveDataAccess
 *
 * @author f0rb on 2021-10-28
 */
public interface ReactiveDataAccess<E extends Persistable<I>, I extends Serializable, Q extends PageQuery> {

    Mono<E> create(E e);

    Flux<E> query(Q q);

}
