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

package win.doyto.query.sql;

import org.junit.jupiter.api.Test;
import win.doyto.query.test.join.MaxIdView;
import win.doyto.query.test.join.TestJoinQuery;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * JoinQueryBuilderTest
 *
 * @author f0rb on 2021-12-11
 */
class JoinQueryBuilderTest {
    @Test
    void supportAggregateQuery() {
        JoinQueryBuilder joinQueryBuilder = new JoinQueryBuilder(MaxIdView.class);
        SqlAndArgs sqlAndArgs = joinQueryBuilder.buildJoinSelectAndArgs(new TestJoinQuery());
        assertEquals("SELECT max(id) AS maxId FROM user", sqlAndArgs.getSql());
    }
}