/*
 * Copyright © 2019-2023 Forb Yuan
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
import win.doyto.query.annotation.Id;
import win.doyto.query.entity.Persistable;

import java.io.Serializable;

/**
 * UserDetailEntity
 *
 * @author f0rb on 2019-06-26
 */
@Getter
@Setter
public class UserDetailEntity implements Persistable<Long>, Serializable {

    @Id
    protected Long id;

    private String address;

    public static UserDetailEntity build(Long id, UserRequest request) {
        UserDetailEntity userDetailEntity = new UserDetailEntity();
        userDetailEntity.setId(id);
        userDetailEntity.setAddress(request.getAddress());
        return userDetailEntity;
    }
}
