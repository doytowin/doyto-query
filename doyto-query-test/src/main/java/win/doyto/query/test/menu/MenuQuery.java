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

package win.doyto.query.test.menu;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import win.doyto.query.annotation.DomainPath;
import win.doyto.query.core.PageQuery;
import win.doyto.query.test.user.UserQuery;

/**
 * MenuQuery
 *
 * @author f0rb on 2019-05-28
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MenuQuery extends PageQuery {

    @DomainPath(value = "menu", foreignField = "parent_id")
    private MenuQuery parentQuery;

    @DomainPath(value = "menu", localField = "parent_id")
    private MenuQuery childrenQuery;

    @DomainPath({"user", "role", "perm", "menu"})
    private UserQuery user;

    private String nameLike;

    private Boolean valid;
}
