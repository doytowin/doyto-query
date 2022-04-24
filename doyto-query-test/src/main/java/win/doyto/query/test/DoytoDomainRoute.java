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

package win.doyto.query.test;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import win.doyto.query.core.AbstractDomainRoute;
import win.doyto.query.test.role.RoleQuery;

import java.util.List;

/**
 * DoytoDomainRoute
 *
 * @author f0rb on 2022-04-22
 */
@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DoytoDomainRoute extends AbstractDomainRoute {
    private Integer userId;
    private Integer roleId;
    private Integer permId;
    private List<Integer> roleIdIn;
    private UserQuery userQuery;
    private RoleQuery roleQuery;
    private PermissionQuery permQuery;
    private MenuQuery menuQuery;
}
