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

import org.springframework.stereotype.Service;
import win.doyto.query.service.AbstractCrudService;

/**
 * UserDetailService
 *
 * @author f0rb on 2019-06-26
 */
@Service
public class UserDetailService extends AbstractCrudService<UserDetailEntity, Long, UserQuery> {

    @Override
    public boolean isNewEntity(UserDetailEntity userDetailEntity) {
        return fetch(userDetailEntity.getId()) == null;
    }
}
