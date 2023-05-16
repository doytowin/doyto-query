/*
 * Copyright © 2019-2023 Forb Yuan
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

package win.doyto.query.web.role;

import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import win.doyto.query.test.role.RoleEntity;
import win.doyto.query.test.role.RoleQuery;
import win.doyto.query.validation.PatchGroup;
import win.doyto.query.validation.UpdateGroup;
import win.doyto.query.web.component.ListValidator;
import win.doyto.query.web.component.NotEmptyQuery;
import win.doyto.query.web.response.ErrorCode;
import win.doyto.query.web.response.JsonBody;
import win.doyto.query.web.response.JsonResponse;
import win.doyto.query.web.response.PresetErrorCode;

import java.util.List;

/**
 * UserController
 *
 * @author f0rb on 2020-04-01
 */
@JsonBody
@Validated
@RestController
@RequestMapping("role")
public class RoleController {

    @Resource
    private ListValidator listValidator;

    @GetMapping("/roleName")
    public JsonResponse<RoleEntity> getByRoleName(@Size(min = 4, max = 20) @NotNull String roleName) {
        return ErrorCode.build((RoleEntity) null);
    }

    @PostMapping("/")
    public void create(@RequestBody List<RoleEntity> requests) {
        listValidator.validateList(requests);
        throw new DuplicateKeyException("");
    }

    @GetMapping("/{id}")
    public RoleEntity get(@PathVariable Long id) {
        ErrorCode.assertNotNull(null, PresetErrorCode.ENTITY_NOT_FOUND);
        return null;
    }

    @PutMapping("{id}")
    void update(@RequestBody @Validated(UpdateGroup.class) RoleEntity roleEntity) {
    }

    @PatchMapping("{id}")
    void patch(@RequestBody @Validated(PatchGroup.class) RoleEntity request) {
    }

    @DeleteMapping("/")
    void delete(@NotEmptyQuery RoleQuery roleQuery) {
    }

}
