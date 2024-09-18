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

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import win.doyto.query.core.AggregateClient;
import win.doyto.query.test.user.UserLevelAggregateQuery;
import win.doyto.query.test.user.UserLevelCountView;
import win.doyto.query.web.response.JsonBody;

import java.util.List;

/**
 * UserAggregateController
 *
 * @author f0rb on 2024/9/18
 */
@AllArgsConstructor
@JsonBody
@RestController
public class UserAggregateController {

    private AggregateClient aggregateClient;

    @GetMapping("user/queryCountOfEachLevel")
    public List<UserLevelCountView> queryCountOfEachLevel(UserLevelAggregateQuery query) {
        return aggregateClient.query(UserLevelCountView.class, query);
    }

}
