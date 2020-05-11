package win.doyto.query.web.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import win.doyto.query.core.PageQuery;
import win.doyto.query.service.PageList;
import win.doyto.query.validation.PageGroup;
import win.doyto.query.validation.PatchGroup;
import win.doyto.query.validation.UpdateGroup;
import win.doyto.query.web.response.JsonBody;

import java.util.Collections;
import java.util.List;

/**
 * RestApi
 *
 * @author f0rb on 2019-05-28
 */
public interface RestApi<I, Q extends PageQuery, R, S> {

    @JsonBody
    @GetMapping
    default Object queryOrPage(@Validated(PageGroup.class) Q q) {
        return q.needPaging() ? page(q) : query(q);
    }

    List<S> query(Q q);

    PageList<S> page(Q q);

    @GetMapping("{id}")
    S get(I id);

    @DeleteMapping("{id}")
    S delete(I id);

    @PutMapping
    void update(@RequestBody @Validated(UpdateGroup.class) R request);

    @PatchMapping
    void patch(@RequestBody @Validated(PatchGroup.class) R request);

    default void create(R request) {
        create(Collections.singletonList(request));
    }

    @PostMapping
    void create(@RequestBody List<R> requests);

}
