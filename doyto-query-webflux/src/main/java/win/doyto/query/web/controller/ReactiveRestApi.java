package win.doyto.query.web.controller;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import win.doyto.query.core.PageQuery;
import win.doyto.query.entity.Persistable;
import win.doyto.query.service.PageList;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * RestApi
 *
 * @author f0rb on 2021-11-13
 */
public interface ReactiveRestApi<E extends Persistable<I>, I extends Serializable, Q extends PageQuery> {
    @PostMapping
    Mono<Void> create(@RequestBody List<E> list);

    default Mono<E> create(E e) {
        return create(Arrays.asList(e)).thenReturn(e);
    }

    @GetMapping("/{id}")
    Mono<E> get(@PathVariable I id);

    @DeleteMapping("/{id}")
    Mono<E> delete(@PathVariable I id);

    @PutMapping("/{id}")
    Mono<Void> update(@PathVariable I id, @RequestBody E e);

    @PatchMapping("/{id}")
    Mono<Void> patch(@PathVariable I id, @RequestBody E e);

    @GetMapping("/")
    Mono<PageList<E>> page(Q q);
}
