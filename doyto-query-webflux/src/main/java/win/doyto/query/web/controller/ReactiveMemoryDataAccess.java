package win.doyto.query.web.controller;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import win.doyto.query.core.MemoryDataAccess;
import win.doyto.query.core.PageQuery;
import win.doyto.query.entity.Persistable;

import java.io.Serializable;

/**
 * ReactiveDataAccess
 *
 * @author f0rb on 2021-10-27
 */
public class ReactiveMemoryDataAccess<E extends Persistable<I>, I extends Serializable, Q extends PageQuery> {

    private final MemoryDataAccess<E, I, Q> delegate;

    public ReactiveMemoryDataAccess(Class<E> entityClass) {
        delegate = new MemoryDataAccess<>(entityClass);
    }

    public Mono<E> create(E e) {
        delegate.create(e);
        return Mono.just(e);
    }

    public Flux<E> query(Q q) {
        return Flux.fromIterable(delegate.query(q));
    }
}
