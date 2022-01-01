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

package win.doyto.query.web.demo.test.role;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.entity.CommonEntity;
import win.doyto.query.validation.CreateGroup;

import javax.validation.constraints.NotNull;

/**
 * UserEntity
 *
 * @author f0rb on 2020-04-01
 */
@Getter
@Setter
public class RoleEntity extends CommonEntity<Long, Long> {

    @NotNull(groups = CreateGroup.class)
    private String roleName;

    @NotNull(groups = CreateGroup.class)
    private String roleCode;

    private Boolean valid;
}
