/*
 * Copyright © 2019-2021 Forb Yuan
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

package win.doyto.query.test.join;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.annotation.Joins;

import javax.persistence.Column;
import javax.persistence.Table;

/**
 * UserCountByRoleView
 *
 * @author f0rb on 2019-06-15
 */
@Getter
@Setter
@Table(name = "user u")
@Joins(value = {
    @Joins.Join("left join t_user_and_role ur on ur.userId = u.id"),
    @Joins.Join("inner join t_role r on r.id = ur.roleId")
}, groupBy = "r.roleName", having = "count(*) > 0")
public class UserCountByRoleView {

    @Column(name = "r.roleName")
    private String roleName;

    @Column(name = "count(u.id)")
    private Integer userCount;

}
