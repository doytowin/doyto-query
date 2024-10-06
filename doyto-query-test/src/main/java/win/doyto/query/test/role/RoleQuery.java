/*
 * Copyright © 2019-2024 Forb Yuan
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

package win.doyto.query.test.role;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import win.doyto.query.annotation.DomainPath;
import win.doyto.query.core.PageQuery;
import win.doyto.query.test.perm.PermissionQuery;
import win.doyto.query.test.user.UserQuery;

import java.util.List;

/**
 * UserQuery
 *
 * @author f0rb on 2020-04-01
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class RoleQuery extends PageQuery {
    private Integer id;
    private List<Integer> idIn;

    @DomainPath({"role", "~", "user"})
    private UserQuery user;

    private String roleName;
    private String roleNameLike;
    private Boolean valid;

    private UserQuery withUsers;
    private UserQuery withCreateUser;
    private PermissionQuery withPerms;

}
