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
import win.doyto.query.test.TestQuery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * BuildHelperTest
 *
 * @author f0rb on 2021-02-16
 */
class BuildHelperTest {
    List<Object> args = new ArrayList<>();

    @Test
    void buildOrderByForFieldSorting() {
        TestQuery testQuery = TestQuery.builder().sort("FIELD(status,1,3,2,0);id,DESC").build();
        assertEquals(" ORDER BY FIELD(status,1,3,2,0), id DESC", BuildHelper.buildOrderBy(testQuery));

        testQuery.setSort(OrderByBuilder.create().field("gender", "'male'", "'female'").desc("id").toString());
        assertEquals(" ORDER BY field(gender,'male','female'), id desc", BuildHelper.buildOrderBy(testQuery));
    }

    private static class UserDetailEntity {
    }

    @Test
    void resolveTableNameForEntityWithoutAnnotationTable() {
        String tableName = BuildHelper.resolveTableName(UserDetailEntity.class);
        assertEquals("t_user_detail", tableName);
    }

    private static class UserDetailView {
    }

    @Test
    void resolveTableNameForViewWithoutAnnotationTable() {
        String tableName = BuildHelper.resolveTableName(UserDetailView.class);
        assertEquals("t_user_detail", tableName);
    }

    @Test
    void treatAsNormalFieldWhenAnnotatedByGroupBy() {
        TestHaving having = TestHaving.builder().firstName("test").firstFirstName("test").build();
        String havingClause = BuildHelper.buildCondition(" HAVING ", having, new ArrayList<>());
        assertThat(havingClause).isEqualTo(" HAVING first_name = ? AND first(first_name) = ?");
    }

    @Test
    void givenExWithSuffixWhenReplaceShouldBeExpression() {
        String input = "SELECT o_year, SUM(CASE WHEN #{nationEq} THEN volume ELSE 0 END) / SUM(volume) AS mkt_share";

        TestQuery query = TestQuery.builder().nationEq("BRAZIL").build();
        String sql = BuildHelper.replaceExpressionInString(input, query,  args);

        assertThat(sql).isEqualTo("SELECT o_year, SUM(CASE WHEN nation = ? THEN volume ELSE 0 END) / SUM(volume) AS mkt_share");
        assertThat(args).containsExactly("BRAZIL");
    }

    @Test
    void buildAndClauseForBasicTypeCollection() {
        TestQuery query = TestQuery.builder().usernameNeAnd(Arrays.asList("test1", "test2")).build();
        String sql = BuildHelper.buildWhere(query, args);

        assertThat(sql).isEqualTo(" WHERE username <> ? AND username <> ?");
        assertThat(args).containsExactly("test1", "test2");
    }
}