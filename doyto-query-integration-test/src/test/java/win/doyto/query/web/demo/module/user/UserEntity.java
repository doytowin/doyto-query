/*
 * Copyright © 2019-2025 DoytoWin, Inc.
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

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import win.doyto.query.annotation.EnumType;
import win.doyto.query.annotation.Enumerated;
import win.doyto.query.entity.AbstractCommonEntity;
import win.doyto.query.validation.CreateGroup;

/**
 * UserEntity
 *
 * @author f0rb on 2020-04-01
 */
@Getter
@Setter
public class UserEntity extends AbstractCommonEntity<Long, Long> {

    @NotNull(groups = CreateGroup.class)
    private String username;
    private String email;
    private String mobile;

    @NotNull(groups = CreateGroup.class)
    private String password;
    private String nickname;
    private Boolean valid;
    private String memo;
    @Enumerated(EnumType.STRING)
    private UserLevel userLevel;
}
