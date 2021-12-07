package win.doyto.query.web.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import win.doyto.query.core.Pageable;
import win.doyto.query.service.PageList;
import win.doyto.query.validation.PageGroup;
import win.doyto.query.validation.PatchGroup;
import win.doyto.query.validation.UpdateGroup;

import java.util.Collections;
import java.util.List;

/**
 * RestApi
 *
 * @author f0rb on 2019-05-28
 */
public interface RestApi<I, Q extends Pageable, R, S> {

    List<S> query(Q q);

    long count(Q q);

    @GetMapping
    PageList<S> page(@Validated(PageGroup.class) Q q);

    @GetMapping("{id}")
    S get(I id);

    @DeleteMapping("{id}")
    S delete(I id);

    @PutMapping("{id}")
    void update(@RequestBody @Validated(UpdateGroup.class) R request);

    @PatchMapping("{id}")
    void patch(@RequestBody @Validated(PatchGroup.class) R request);

    default void create(R request) {
        create(Collections.singletonList(request));
    }

    @PostMapping
    void create(@RequestBody List<R> requests);

}
