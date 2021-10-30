package win.doyto.query.web.controller;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import win.doyto.query.core.PageQuery;
import win.doyto.query.data.ReactiveDataAccess;
import win.doyto.query.data.ReactiveMemoryDataAccess;
import win.doyto.query.entity.Persistable;
import win.doyto.query.util.BeanUtil;

import java.io.Serializable;
import java.lang.reflect.Type;

/**
 * ReactiveEIQController
 *
 * @author f0rb on 2021-10-26
 */
@Slf4j
public abstract class ReactiveEIQController<E extends Persistable<I>, I extends Serializable, Q extends PageQuery> {

    private ReactiveDataAccess<E, I, Q> reactiveDataAccess;

    @SuppressWarnings("unchecked")
    protected ReactiveEIQController() {
        Type[] types = BeanUtil.getActualTypeArguments(getClass());
        reactiveDataAccess = new ReactiveMemoryDataAccess<>((Class<E>) types[0]);
    }

    public Mono<E> add(E e) {
        return reactiveDataAccess.create(e);
    }

    public Flux<E> query(Q query) {
        return reactiveDataAccess.query(query);
    }

    public Mono<E> get(I id) {
        return reactiveDataAccess.get(id);
    }

    public Mono<E> delete(I id) {
        return get(id).flatMap(
                e -> reactiveDataAccess.delete(e.getId()).thenReturn(e)
        );
    }

    public Mono<Void> update(E e) {
        return get(e.getId()).flatMap(
                old -> reactiveDataAccess.update(e)
        ).then();
    }

    public Mono<Void> patch(E e) {
        return get(e.getId()).flatMap(
                old -> reactiveDataAccess.patch(e)
        ).then();
    }

    public Mono<Long> count(Q q) {
        return reactiveDataAccess.count(q);
    }
}
