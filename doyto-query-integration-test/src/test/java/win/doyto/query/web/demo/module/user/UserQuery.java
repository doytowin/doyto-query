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

package win.doyto.query.web.demo.module.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import win.doyto.query.annotation.DomainPath;
import win.doyto.query.core.PageQuery;
import win.doyto.query.test.PermissionQuery;

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
public class UserQuery extends PageQuery {
    @DomainPath({"user", "role", "perm"})
    private PermissionQuery perm;

    private String username;
    private String email;
    private String mobile;
    private String usernameOrEmailOrMobile;
    private String usernameLike;
    private String emailLike;
    private boolean memoNull;
    private UserLevel userLevel;

    @SuppressWarnings("unused")
    public void setAccount(String account) {
        this.usernameOrEmailOrMobile = account;
    }
}
