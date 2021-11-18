package win.doyto.query.data;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import win.doyto.query.core.PageQuery;
import win.doyto.query.core.SqlAndArgs;
import win.doyto.query.entity.Persistable;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * ReactiveDefaultDataAccess
 *
 * @author f0rb on 2021-11-18
 */
public class ReactiveDatabaseDataAccess<E extends Persistable<I>, I extends Serializable, Q extends PageQuery> implements ReactiveDataAccess<E, I, Q> {

    private R2dbcOperations r2dbcOperations;

    public ReactiveDatabaseDataAccess(R2dbcOperations r2dbcOperations) {
        this.r2dbcOperations = r2dbcOperations;
    }

    @Override
    public Mono<E> create(E e) {
        return null;
    }

    @Override
    public Flux<E> query(Q q) {
        return null;
    }

    @Override
    public Mono<Long> count(Q q) {
        return r2dbcOperations.count(new SqlAndArgs("", new ArrayList<>()));
    }

    @Override
    public Mono<E> get(I id) {
        return null;
    }

    @Override
    public Mono<Integer> delete(I id) {
        return null;
    }

    @Override
    public Mono<Integer> update(E e) {
        return null;
    }

    @Override
    public Mono<Integer> patch(E e) {
        return null;
    }
}
