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

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.RowMapper;
import win.doyto.query.core.*;
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
        AggregateChain<UserLevelCountView> chain = aggregateClient
                .aggregate(UserLevelCountView.class)
                .where(new UserLevelQuery(true))
                .having(UserLevelHaving.builder().countGt(1).countLt(10).build())
                .paging(PageQuery.builder().pageSize(10).build())
                .mapper((RowMapper<UserLevelCountView>) (rs, rn) -> {
                    UserLevelCountView view = new UserLevelCountView();
                    view.setUserLevel(UserLevel.valueOf(rs.getString("userLevel")));
                    view.setValid(rs.getBoolean("valid"));
                    view.setCount(rs.getLong("count"));
                    return view;
                });
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
        AggregatedQuery aggregatedQuery = AggregatedQuery
                .builder().query(new UserLevelQuery(true))
                .having(UserLevelHaving.builder().countGt(1).countLt(10).build())
                .pageSize(10).build();

        assertThat(aggregateClient.count(UserLevelCountView.class, aggregatedQuery))
                .isEqualTo(1);

        PageList<UserLevelCountView> userLevelCountViews = aggregateClient.page(UserLevelCountView.class, aggregatedQuery);
        assertThat(userLevelCountViews.getList())
                .hasSize(1)
                .extracting("userLevel", "valid", "count")
                .containsExactlyInAnyOrder(
                        new Tuple(UserLevel.普通, true, 2L)
                );
    }
}
