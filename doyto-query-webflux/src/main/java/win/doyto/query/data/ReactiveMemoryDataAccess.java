package win.doyto.query.data;

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
public class ReactiveMemoryDataAccess<E extends Persistable<I>, I extends Serializable, Q extends PageQuery> implements ReactiveDataAccess<E, I, Q> {

    private final MemoryDataAccess<E, I, Q> delegate;

    public ReactiveMemoryDataAccess(Class<E> entityClass) {
        delegate = new MemoryDataAccess<>(entityClass);
    }

    @Override
    public Mono<E> create(E e) {
        delegate.create(e);
        return Mono.just(e);
    }

    @Override
    public Flux<E> query(Q q) {
        return Flux.fromIterable(delegate.query(q));
    }

    @Override
    public Mono<E> get(I id) {
        return Mono.fromSupplier(() -> delegate.get(id));
    }

    @Override
    public Mono<Integer> delete(I id) {
        return Mono.fromSupplier(() -> delegate.delete(id));
    }

}
