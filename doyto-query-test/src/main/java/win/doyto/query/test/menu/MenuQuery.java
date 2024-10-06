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

    // many-to-one
    @DomainPath(value = "menu", localField = "parentId")
    private MenuQuery parent;

    // one-to-many
    @DomainPath(value = "menu", foreignField = "parentId")
    private MenuQuery children;

    @DomainPath({"menu", "~", "perm",  "~", "role", "~", "user"})
    private UserQuery user;

    private Long id;

    private String nameLike;

    private Boolean valid;

    private MenuQuery withParent;

    private MenuQuery withChildren;
}
