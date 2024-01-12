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

package win.doyto.query.web.demo.module.user;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.annotation.Entity;
import win.doyto.query.annotation.Transient;
import win.doyto.query.entity.Persistable;

/**
 * UserResponse
 *
 * @author f0rb on 2020-04-02
 */
@Getter
@Setter
@Entity(name = "t_user")
public class UserResponse implements Persistable<Long> {
    private Long id;
    private String username;
    private String email;
    private String mobile;
    private String nickname;
    private Boolean valid;
    private String memo;
    private UserLevel userLevel;
    @Transient
    private String address;

}
