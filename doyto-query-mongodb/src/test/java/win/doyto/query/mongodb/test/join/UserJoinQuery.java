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

package win.doyto.query.mongodb.test.join;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import win.doyto.query.annotation.DomainPath;
import win.doyto.query.core.PageQuery;
import win.doyto.query.core.RelationalQuery;
import win.doyto.query.test.role.RoleQuery;

import java.math.BigInteger;

/**
 * UserJoinQuery
 *
 * @author f0rb on 2022-03-26
 */
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class UserJoinQuery extends PageQuery implements RelationalQuery<UserEntity, BigInteger> {

    @DomainPath({"user", "role"})
    private RoleQuery role;

    private UserJoinQuery createUserQuery;
    private UserJoinQuery createdUsersQuery;

    @Override
    public Class<UserEntity> getDomainClass() {
        return UserEntity.class;
    }
}
