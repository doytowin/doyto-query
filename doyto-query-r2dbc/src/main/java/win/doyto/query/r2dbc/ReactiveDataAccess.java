package win.doyto.query.r2dbc;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import win.doyto.query.core.Pageable;
import win.doyto.query.entity.Persistable;

import java.io.Serializable;

/**
 * ReactiveDataAccess
 *
 * @author f0rb on 2021-10-28
 */
public interface ReactiveDataAccess<E extends Persistable<I>, I extends Serializable, Q extends Pageable> {

    Mono<E> create(E e);

    Flux<E> query(Q q);

    Mono<Long> count(Q q);

    Mono<E> get(I id);

    Mono<Integer> delete(I id);

    Mono<Integer> update(E e);

    Mono<Integer> patch(E e);

    default Mono<Long> create(Iterable<E> entities) {
        return Flux.fromIterable(entities).flatMap(this::create).count();
    }
}
