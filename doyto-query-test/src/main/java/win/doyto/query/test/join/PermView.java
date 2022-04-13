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

package win.doyto.query.test.join;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.annotation.DomainPath;
import win.doyto.query.entity.Persistable;

import java.util.List;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * PermView
 *
 * @author f0rb on 2022-03-26
 */
@Getter
@Setter
@Table(name = "t_perm")
public class PermView implements Persistable<Long> {

    @Id
    private Long id;
    private String permName;
    private Boolean valid;

    @DomainPath({"user", "role", "perm"})
    private List<UserView> users;

}
