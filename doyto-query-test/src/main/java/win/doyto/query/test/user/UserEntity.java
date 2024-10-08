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

package win.doyto.query.test.user;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.annotation.DomainPath;
import win.doyto.query.entity.AbstractPersistable;
import win.doyto.query.test.menu.MenuView;
import win.doyto.query.test.perm.PermView;
import win.doyto.query.test.role.RoleStatView;
import win.doyto.query.test.role.RoleView;

import java.util.List;

/**
 * UserEntity
 *
 * @author f0rb on 2022/11/24
 * @since 1.0.0
 */
@Getter
@Setter
public class UserEntity extends AbstractPersistable<Long> {
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
    private UserEntity createUser;

    // one-to-many
    @DomainPath(value = "role", foreignField = "create_user_id")
    private List<RoleView> createRoles;

    // many-to-many aggregation
    @DomainPath({"user", "role"})
    private RoleStatView roleStat;
}
