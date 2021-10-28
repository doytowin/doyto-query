package win.doyto.query.web.controller;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import win.doyto.query.core.PageQuery;
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

    private ReactiveMemoryDataAccess<E, I, Q> reactiveMemoryDataAccess;

    @SuppressWarnings("unchecked")
    protected ReactiveEIQController() {
        Type[] types = BeanUtil.getActualTypeArguments(getClass());
        reactiveMemoryDataAccess = new ReactiveMemoryDataAccess<>((Class<E>) types[0]);
    }

    public Mono<E> add(E e) {
        return reactiveMemoryDataAccess.create(e);
    }

    public Flux<E> query(Q query) {
        return reactiveMemoryDataAccess.query(query);
    }
}
