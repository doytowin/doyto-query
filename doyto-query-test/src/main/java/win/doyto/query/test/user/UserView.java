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

package win.doyto.query.test.user;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.annotation.DomainPath;
import win.doyto.query.annotation.Id;
import win.doyto.query.entity.Persistable;
import win.doyto.query.test.menu.MenuView;
import win.doyto.query.test.perm.PermView;
import win.doyto.query.test.role.RoleStatView;
import win.doyto.query.test.role.RoleView;

import java.util.List;

/**
 * UserCountByRoleView
 *
 * @author f0rb on 2019-06-15
 */
@Getter
@Setter
public class UserView implements Persistable<Long> {

    @Id
    private Long id;
    private String username;
    private String email;

    // many-to-many
    @DomainPath({"user", "role"})
    private List<RoleView> roles;

    // many-to-many
    @DomainPath({"user", "role", "perm"})
    private List<PermView> perms;

    // many-to-many
    @DomainPath({"user", "role", "perm", "menu"})
    private List<MenuView> menus;

    // many-to-one
    @DomainPath(value = "user", localField = "create_user_id")
    private UserView createUser;

    // one-to-many
    @DomainPath(value = "role", foreignField = "create_user_id")
    private List<RoleView> createRoles;

    // many-to-many aggregation
    @DomainPath({"user", "role"})
    private RoleStatView roleStat;

}
