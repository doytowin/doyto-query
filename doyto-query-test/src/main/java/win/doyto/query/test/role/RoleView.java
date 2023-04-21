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

package win.doyto.query.test.role;

import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import win.doyto.query.annotation.DomainPath;
import win.doyto.query.entity.Persistable;
import win.doyto.query.test.perm.PermView;
import win.doyto.query.test.user.UserView;

import java.util.List;

/**
 * UserEntity
 *
 * @author f0rb on 2020-04-01
 */
@Getter
@Setter
public class RoleView implements Persistable<Integer> {

    @Id
    private Integer id;

    private String roleName;

    private String roleCode;

    private Boolean valid;

    // many-to-many
    @DomainPath({"role", "~", "user"})
    private List<UserView> users;

    // many-to-many
    @DomainPath({"role", "perm"})
    private List<PermView> perms;

    // many-to-one
    @DomainPath(value = "user", localField = "create_user_id")
    private UserView createUser;

}
