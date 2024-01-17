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

package win.doyto.query.web.component;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import win.doyto.query.test.TestQuery;
import win.doyto.query.test.user.UserQuery;
import win.doyto.query.validation.PageGroup;
import win.doyto.query.web.response.JsonBody;

/**
 * SortController
 *
 * @author f0rb on 2023/11/30
 * @since 1.0.3
 */
@JsonBody
@RestController
public class SortController {

    @GetMapping("sort")
    String getSort(@Validated(PageGroup.class) TestQuery query) {
        return query.getSort();
    }

    @GetMapping("sort2")
    String getSort(@Validated(PageGroup.class) UserQuery query) {
        return query.getSort();
    }

}
