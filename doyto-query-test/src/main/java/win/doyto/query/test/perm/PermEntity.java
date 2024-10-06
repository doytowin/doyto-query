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

package win.doyto.query.test.perm;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.annotation.DomainPath;
import win.doyto.query.entity.AbstractPersistable;
import win.doyto.query.test.user.UserEntity;

import java.util.List;

/**
 * PermEntity
 *
 * @author f0rb on 2022-03-26
 */
@Getter
@Setter
public class PermEntity extends AbstractPersistable<Integer> {
    private String permName;
    private Boolean valid;

    @DomainPath({"perm", "role", "user"})
    private List<UserEntity> users;

}
