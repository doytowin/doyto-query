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

package win.doyto.query.sql;

import org.junit.jupiter.api.Test;
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.core.PageQuery;
import win.doyto.query.test.user.MaxIdView;
import win.doyto.query.test.user.UserLevelCountView;
import win.doyto.query.test.user.UserLevelHaving;
import win.doyto.query.test.user.UserLevelQuery;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AggregateQueryBuilderTest
 *
 * @author f0rb on 2024/8/12
 */
class AggregateQueryBuilderTest {

    @Test
    void supportAggregateQuery() {
        EntityMetadata em = EntityMetadata.build(MaxIdView.class);
        SqlAndArgs sqlAndArgs = AggregateQueryBuilder.buildSelectAndArgs(em, new PageQuery());
        assertThat(sqlAndArgs.getSql()).isEqualTo("SELECT max(id) AS maxId, first(create_user_id) AS firstCreateUserId FROM t_user");
    }

    @Test
    void buildSelectAndArgs() {
        GlobalConfiguration.instance().setMapCamelCaseToUnderscore(false);

        try {
            EntityMetadata entityMetadata = EntityMetadata.build(UserLevelCountView.class);
            UserLevelHaving query = UserLevelHaving.builder().countGt(1).countLt(10).valid(true).build();
            SqlAndArgs sqlAndArgs = AggregateQueryBuilder.buildSelectAndArgs(entityMetadata, query);

            String expected = "SELECT userLevel, valid, count(*) AS count FROM t_user WHERE valid = ? " +
                    "GROUP BY userLevel, valid HAVING count(*) > ? AND count(*) < ?";

            assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
            assertThat(sqlAndArgs.getArgs()).containsExactly(true, 1, 10);
        } finally {
            GlobalConfiguration.instance().setMapCamelCaseToUnderscore(true);
        }
    }
}
