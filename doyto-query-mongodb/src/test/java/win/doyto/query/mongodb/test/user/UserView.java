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

package win.doyto.query.mongodb.test.user;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.annotation.DomainPath;
import win.doyto.query.mongodb.entity.MongoPersistable;
import win.doyto.query.mongodb.test.menu.MenuView;
import win.doyto.query.mongodb.test.perm.PermView;
import win.doyto.query.mongodb.test.role.RoleView;

import java.math.BigInteger;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.EntityType;

/**
 * UserView
 *
 * @author f0rb on 2022-05-20
 * @since 1.0.0
 */
@Getter
@Setter
@Entity(type = EntityType.MONGO_DB, database = "doyto", name = "t_user")
public class UserView extends MongoPersistable<BigInteger> {

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
    @DomainPath(value = "user", localField = "createUserId")
    private UserView createUser;

    // one-to-many
    @DomainPath(value = "user", foreignField = "createUserId")
    private List<UserView> createdUsers;

}
