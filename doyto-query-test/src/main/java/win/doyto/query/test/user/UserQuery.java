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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import win.doyto.query.annotation.DomainPath;
import win.doyto.query.core.PageQuery;
import win.doyto.query.test.perm.PermissionQuery;
import win.doyto.query.test.role.RoleQuery;

import java.util.List;

/**
 * TestQuery
 *
 * @author f0rb
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("unused")
public class UserQuery extends PageQuery {
    private Integer id;
    private List<Integer> idIn;

    @DomainPath({"user", "role"})
    private RoleQuery role;
    private RoleQuery roleQuery;

    @DomainPath({"user", "role", "perm"})
    private PermissionQuery perm;
    private PermissionQuery permQuery;

    private String username;

    private String usernameOrEmailOrMobile;

    private String mobile;

    private String usernameLike;

    private String emailLike;

    private boolean memoNull;

    private UserLevel userLevel;


    public void setAccount(String account) {
        this.usernameOrEmailOrMobile = account;
    }
}
