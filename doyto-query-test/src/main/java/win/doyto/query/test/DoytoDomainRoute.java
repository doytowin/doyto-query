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

import lombok.*;
import win.doyto.query.core.DomainRoute;
import win.doyto.query.test.role.RoleQuery;

import java.util.List;
import javax.persistence.Transient;

/**
 * DoytoDomainRoute
 *
 * @author f0rb on 2022-04-22
 */
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoytoDomainRoute implements DomainRoute {
    @Transient
    private List<String> path;
    private Integer userId;
    private List<Integer> roleIdIn;
    private UserQuery userQuery;
    private RoleQuery roleQuery;
}