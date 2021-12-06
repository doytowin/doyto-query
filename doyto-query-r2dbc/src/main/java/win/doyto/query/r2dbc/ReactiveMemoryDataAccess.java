package win.doyto.query.r2dbc;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import win.doyto.query.core.MemoryDataAccess;
import win.doyto.query.core.Pageable;
import win.doyto.query.entity.Persistable;

import java.io.Serializable;

/**
 * ReactiveDataAccess
 *
 * @author f0rb on 2021-10-27
 */
public class ReactiveMemoryDataAccess<E extends Persistable<I>, I extends Serializable, Q extends Pageable> implements ReactiveDataAccess<E, I, Q> {

    private final MemoryDataAccess<E, I, Q> delegate;

    public ReactiveMemoryDataAccess(Class<E> entityClass) {
        delegate = new MemoryDataAccess<>(entityClass);
    }

    @Override
    public Mono<E> create(E e) {
        return Mono.fromSupplier(() -> {
            delegate.create(e);
            return e;
        });
    }

    @Override
    public Flux<E> query(Q q) {
        return Flux.fromStream(() -> delegate.query(q).stream());
    }

    @Override
    public Mono<E> get(I id) {
        return Mono.fromSupplier(() -> delegate.get(id));
    }

    @Override
    public Mono<Integer> delete(I id) {
        return Mono.fromSupplier(() -> delegate.delete(id));
    }

    @Override
    public Mono<Integer> update(E e) {
        return Mono.fromSupplier(() -> delegate.update(e));
    }

    @Override
    public Mono<Integer> patch(E e) {
        return Mono.fromSupplier(() -> delegate.patch(e));
    }

    @Override
    public Mono<Long> count(Q q) {
        return Mono.fromSupplier(() -> delegate.count(q));
    }

}
