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

package win.doyto.query.jdbc;

import jakarta.annotation.Resource;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import win.doyto.query.test.user.UserLevel;
import win.doyto.query.test.user.UserLevelAggrQuery;
import win.doyto.query.test.user.UserLevelCountView;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JdbcAggregateTest
 *
 * @author f0rb on 2024/8/12
 */
public class JdbcAggregateTest extends JdbcApplicationTest {
    @Resource
    private JdbcAggregateClient jdbcAggregateClient;

    @Test
    void supportAggregateQuery() {
        UserLevelAggrQuery aggrQuery = UserLevelAggrQuery.builder().build();
        List<UserLevelCountView> userLevelCountViews = jdbcAggregateClient.aggregate(UserLevelCountView.class, aggrQuery);
        assertThat(userLevelCountViews)
                .hasSize(3)
                .extracting("userLevel", "valid", "count")
                .containsExactlyInAnyOrder(
                        new Tuple(UserLevel.高级, true, 1L),
                        new Tuple(UserLevel.普通, false, 1L),
                        new Tuple(UserLevel.普通, true, 2L)
                );
    }
}
