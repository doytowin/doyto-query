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

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.annotation.DomainPath;
import win.doyto.query.annotation.Id;
import win.doyto.query.entity.Persistable;
import win.doyto.query.test.user.UserView;

import java.util.List;

/**
 * MenuView
 *
 * @author f0rb on 2022-04-13
 */
@Getter
@Setter
public class MenuView implements Persistable<Integer> {

    @Id
    private Integer id;
    private String menuName;
    private String platform;

    @DomainPath({"menu", "~", "perm",  "~", "role", "~", "user"})
    private List<UserView> users;

    // many-to-one
    @DomainPath(value = "menu", localField = "parent_id")
    private MenuView parent;

    // one-to-many
    @DomainPath(value = "menu", foreignField = "parent_id")
    private List<MenuView> children;
}
