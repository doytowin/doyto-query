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

package win.doyto.query.jdbc;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.RowMapper;
import win.doyto.query.core.AggregateChain;
import win.doyto.query.core.AggregateClient;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.core.PageList;
import win.doyto.query.test.user.UserLevel;
import win.doyto.query.test.user.UserLevelCountView;
import win.doyto.query.test.user.UserLevelHaving;
import win.doyto.query.test.user.UserLevelQuery;

import javax.annotation.Resource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AggregateClientTest
 *
 * @author f0rb on 2024/8/12
 */
class AggregateClientTest extends JdbcApplicationTest {
    @Resource
    private AggregateClient aggregateClient;

    @Test
    void supportAggregateQuery() {
        UserLevelQuery query = UserLevelHaving.builder().countGt(1).countLt(10).valid(true).build();
        AggregateChain<UserLevelCountView> chain = aggregateClient
                .aggregate(UserLevelCountView.class)
                .mapper((RowMapper<UserLevelCountView>) (rs, rn) -> {
                    UserLevelCountView view = new UserLevelCountView();
                    view.setUserLevel(UserLevel.valueOf(rs.getString("userLevel")));
                    view.setValid(rs.getBoolean("valid"));
                    view.setCount(rs.getLong("count"));
                    return view;
                }).filter(query);
        chain.print();
        PageList<UserLevelCountView> userLevelCountViews = chain.page();
        assertThat(userLevelCountViews.getTotal()).isEqualTo(1);
        assertThat(userLevelCountViews.getList())
                .hasSize(1)
                .extracting("userLevel", "valid", "count")
                .containsExactlyInAnyOrder(
                        new Tuple(UserLevel.普通, true, 2L)
                );
    }

    @Test
    void testPage() {
        DoytoQuery aggregateQuery = UserLevelHaving
                .builder().countGt(1).countLt(10).valid(true).pageSize(10).build();

        long count = aggregateClient.count(UserLevelCountView.class, aggregateQuery);
        assertThat(count).isEqualTo(1);

        PageList<UserLevelCountView> userLevelCountViews = aggregateClient.page(UserLevelCountView.class, aggregateQuery);
        assertThat(userLevelCountViews.getList())
                .hasSize(1)
                .extracting("userLevel", "valid", "count")
                .containsExactlyInAnyOrder(
                        new Tuple(UserLevel.普通, true, 2L)
                );
    }
}
