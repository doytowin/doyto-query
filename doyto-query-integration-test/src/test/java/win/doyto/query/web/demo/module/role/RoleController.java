/*
 * Copyright © 2019-2021 Forb Yuan
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

package win.doyto.query.web.demo.module.role;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import win.doyto.query.test.role.RoleEntity;
import win.doyto.query.test.role.RoleQuery;
import win.doyto.query.web.controller.AbstractEIQController;

/**
 * UserController
 *
 * @author f0rb on 2020-04-01
 */
@RestController
@RequestMapping("role")
public class RoleController extends AbstractEIQController<RoleEntity, Integer, RoleQuery> {
}
