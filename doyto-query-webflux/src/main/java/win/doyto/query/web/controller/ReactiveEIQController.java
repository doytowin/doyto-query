package win.doyto.query.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import win.doyto.query.core.PageQuery;
import win.doyto.query.data.ReactiveDataAccess;
import win.doyto.query.data.ReactiveMemoryDataAccess;
import win.doyto.query.entity.Persistable;
import win.doyto.query.service.PageList;
import win.doyto.query.util.BeanUtil;
import win.doyto.query.web.response.ErrorCode;
import win.doyto.query.web.response.JsonBody;
import win.doyto.query.web.response.PresetErrorCode;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * ReactiveEIQController
 *
 * @author f0rb on 2021-10-26
 */
@Slf4j
@JsonBody
public abstract class ReactiveEIQController<E extends Persistable<I>, I extends Serializable, Q extends PageQuery> {

    private ReactiveDataAccess<E, I, Q> reactiveDataAccess;
    private Class<E> entityClass;

    @SuppressWarnings("unchecked")
    protected ReactiveEIQController() {
        this.entityClass = (Class<E>) BeanUtil.getActualTypeArguments(getClass())[0];
        this.reactiveDataAccess = new ReactiveMemoryDataAccess<>(entityClass);
    }

    private void assertNotNull(E e, I id) {
        ErrorCode.assertNotNull(e, PresetErrorCode.ENTITY_NOT_FOUND, entityClass.getSimpleName() + ":" + id);
    }

    @PostMapping
    public Mono<Void> create(@RequestBody List<E> list) {
        return reactiveDataAccess.create(list).then();
    }

    public Mono<E> create(E e) {
        return create(Arrays.asList(e)).thenReturn(e);
    }

    public Flux<E> query(Q query) {
        return reactiveDataAccess.query(query);
    }

    @GetMapping("/{id}")
    public Mono<E> get(@PathVariable I id) {
        return reactiveDataAccess.get(id).doOnSuccess(e -> assertNotNull(e, id));
    }

    @DeleteMapping("/{id}")
    public Mono<E> delete(@PathVariable I id) {
        return get(id).flatMap(
                e -> reactiveDataAccess.delete(e.getId()).thenReturn(e)
        ).doOnSuccess(e -> assertNotNull(e, id));
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

    @GetMapping("/")
    public Mono<PageList<E>> page(Q q) {
        q.forcePaging();
        return query(q)
                .collectList()
                .zipWith(count(q))
                .map(t -> new PageList<>(t.getT1(), t.getT2()));
    }
}
