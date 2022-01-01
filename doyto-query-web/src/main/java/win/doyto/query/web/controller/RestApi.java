/*
 * Copyright © 2019-2022 Forb Yuan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package win.doyto.query.web.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import win.doyto.query.core.DoytoQuery;
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
public interface RestApi<I, Q extends DoytoQuery, R, S> {

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
