/*
 * Copyright © 2019-2022 Forb Yuan
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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.core.Dialect;
import win.doyto.query.test.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * QueryBuilderTest
 *
 * @author f0rb 2019-05-12
 */
@ResourceLock(value = "mapCamelCaseToUnderscore")
class QueryBuilderTest {

    private final QueryBuilder testQueryBuilder = new QueryBuilder(TestEntity.class);
    private final QueryBuilder dynamicQueryBuilder = new QueryBuilder(DynamicEntity.class);
    private List<Object> argList;

    @BeforeEach
    void setUp() {
        GlobalConfiguration.instance().setMapCamelCaseToUnderscore(false);
        GlobalConfiguration.instance().setDialect(new SimpleDialect());
        argList = new ArrayList<>();
    }

    @AfterEach
    void tearDown() {
        GlobalConfiguration.instance().setMapCamelCaseToUnderscore(true);
    }

    @Test
    void buildSelect() {
        TestQuery testQuery = TestQuery.builder().build();
        assertEquals("SELECT * FROM user", testQueryBuilder.buildSelectAndArgs(testQuery, argList));
    }

    @Test
    void buildSelectWithWhere() {
        TestQuery testQuery = TestQuery.builder().username("test").build();
        assertEquals("SELECT * FROM user WHERE username = ?", testQueryBuilder.buildSelectAndArgs(testQuery, argList));
    }

    @Test
    void buildSelectWithWhereAndPage() {
        TestQuery testQuery = TestQuery.builder().username("test").pageNumber(4).pageSize(10).build();
        assertEquals("SELECT * FROM user WHERE username = ? LIMIT 10 OFFSET 30",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));
    }

    @Test
    void buildSelectWithCustomWhere() {
        TestQuery testQuery = TestQuery.builder().account("test").build();
        assertEquals("SELECT * FROM user WHERE (username = ? OR email = ? OR mobile = ?)",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));
    }

    @Test
    void buildSelectWithArgs() {
        TestQuery testQuery = TestQuery.builder().username("test").build();
        assertEquals("SELECT * FROM user WHERE username = ?",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));
        assertEquals(1, argList.size());
        assertEquals("test", argList.get(0));
    }

    @Test
    void buildSelectAndArgsWithCustomWhere() {

        TestQuery testQuery = TestQuery.builder().account("test").build();
        assertEquals("SELECT * FROM user WHERE (username = ? OR email = ? OR mobile = ?)",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));
        assertEquals(3, argList.size());
    }

    @Test
    void buildCountAndArgsWithWhere() {
        TestQuery testQuery = TestQuery.builder().username("test").pageNumber(3).pageSize(10).sort("createTime,asc").build();

        assertEquals("SELECT * FROM user WHERE username = ? ORDER BY createTime asc LIMIT 10 OFFSET 20",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));

        assertEquals("SELECT count(*) FROM user WHERE username = ?",
                     testQueryBuilder.buildCountAndArgs(testQuery).getSql());
    }

    @Test
    void buildCountWithWhere() {
        TestQuery testQuery = TestQuery.builder().username("test").build();
        testQuery.setPageNumber(0);
        SqlAndArgs sqlAndArgs = testQueryBuilder.buildCountAndArgs(testQuery);
        assertEquals("SELECT count(*) FROM user WHERE username = ?", sqlAndArgs.getSql());
        assertThat(sqlAndArgs.getArgs()).containsExactly("test");
    }

    @Test
    void supportLikeSuffix() {
        TestQuery testQuery = TestQuery.builder().usernameLike("_test%f0rb").build();

        assertEquals("SELECT * FROM user WHERE username LIKE ?",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).containsExactly("%\\_test\\%f0rb%");
    }

    @Test
    void supportInSuffix() {
        List<Integer> ids = Arrays.asList(1, 2, 3);
        TestQuery testQuery = TestQuery.builder().idIn(ids).build();

        assertEquals("SELECT * FROM user WHERE id IN (?, ?, ?)",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).containsExactly(1, 2, 3);

    }

    @Test
    void supportNotInSuffix() {
        TestQuery testQuery = TestQuery.builder().idNotIn(Arrays.asList(1, 2)).build();

        assertEquals("SELECT * FROM user WHERE id NOT IN (?, ?)",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).containsExactly(1, 2);
    }

    @Test
    void supportGtSuffix() {
        Date createTimeGt = new Date();
        TestQuery testQuery = TestQuery.builder().username("test").createTimeGt(createTimeGt).build();

        assertEquals("SELECT * FROM user WHERE username = ? AND createTime > ?",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).containsExactly("test", createTimeGt);
    }

    @Test
    void supportGeSuffix() {
        Date date = new Date();
        TestQuery testQuery = TestQuery.builder().username("test").createTimeGe(date).build();

        assertEquals("SELECT * FROM user WHERE username = ? AND createTime >= ?",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).containsExactly("test", date);
    }

    @Test
    void supportLtSuffix() {
        Date date = new Date();
        TestQuery testQuery = TestQuery.builder().username("test").createTimeLt(date).build();

        assertEquals("SELECT * FROM user WHERE username = ? AND createTime < ?",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).containsExactly("test", date);
    }

    @Test
    void supportLeSuffix() {
        Date date = new Date();
        TestQuery testQuery = TestQuery.builder().username("test").createTimeLe(date).build();

        assertEquals("SELECT * FROM user WHERE username = ? AND createTime <= ?",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).containsExactly("test", date);
    }

    @Test
    void supportOr() {
        TestQuery testQuery = TestQuery.builder().usernameOrEmailOrMobile("test").build();

        assertEquals("SELECT * FROM user WHERE (username = ? OR email = ? OR mobile = ?)",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).containsExactly("test", "test", "test");

    }

    @Test
    void supportOrWithLike() {
        TestQuery testQuery = TestQuery.builder().usernameOrEmailOrMobileLike("test").build();

        assertEquals("SELECT * FROM user WHERE (username = ? OR email = ? OR mobile LIKE ?)",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).containsExactly("test", "test", "%test%");

    }

    @Test
    void supportSort() {
        TestQuery testQuery = TestQuery.builder().usernameLike("test")
                                       .pageNumber(6).pageSize(10)
                                       .sort("id,desc;createTime,asc").build();
        assertEquals("SELECT * FROM user WHERE username LIKE ? ORDER BY id desc, createTime asc LIMIT 10 OFFSET 50",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));
    }

    @Test
    void supportMapFieldToUnderscore() {
        GlobalConfiguration.instance().setMapCamelCaseToUnderscore(true);

        Date date = new Date();
        TestQuery testQuery = TestQuery.builder().usernameOrUserCodeLike("test").createTimeLt(date).build();

        assertEquals("SELECT * FROM user WHERE (username = ? OR user_code LIKE ?) AND create_time < ?",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).containsExactly("test", "%test%", date);

        GlobalConfiguration.instance().setMapCamelCaseToUnderscore(false);
    }

    @Test
    void buildSelectIdWithArgs() {
        TestQuery testQuery = TestQuery.builder().username("test").build();

        SqlAndArgs sqlAndArgs = testQueryBuilder.buildSelectColumnsAndArgs(testQuery, "id");

        assertEquals("SELECT id FROM user WHERE username = ?", sqlAndArgs.getSql());
        assertThat(sqlAndArgs.getArgs()).containsExactly("test");
    }

    @Test
    void buildSelectColumnsAndArgs() {
        TestQuery testQuery = TestQuery.builder().build();

        SqlAndArgs sqlAndArgs = testQueryBuilder.buildSelectColumnsAndArgs(testQuery, "username", "password");

        assertEquals("SELECT username, password FROM user", sqlAndArgs.getSql());
    }

    @Test
    void defaultEnumOrdinal() {
        TestQuery testQuery = TestQuery.builder().userLevel(TestEnum.VIP).build();
        assertEquals("SELECT * FROM user WHERE userLevel = ?",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).containsExactly(0);

    }

    @Test
    void supportIsNull() {
        TestQuery testQuery = TestQuery.builder().memoNull(true).build();

        String sql = testQueryBuilder.buildSelectAndArgs(testQuery, argList);

        assertThat(sql).isEqualTo("SELECT * FROM user WHERE memo IS NULL");
        assertThat(argList).isEmpty();
    }

    @Test
    void customPageDialect() {
        GlobalConfiguration globalConfiguration = GlobalConfiguration.instance();
        Dialect origin = globalConfiguration.getDialect();
        globalConfiguration.setDialect(
            (sql, limit, offset) -> String.format("SELECT LIMIT %d %d %s", offset, offset + limit, sql.substring("SELECT ".length())));

        TestQuery testQuery = TestQuery.builder().pageNumber(3).pageSize(10).build();
        assertEquals("SELECT LIMIT 20 30 * FROM user",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));

        // reset
        globalConfiguration.setDialect(origin);

    }

    @Test
    void ignoreNotInWhenEmpty() {
        List<Integer> ids = Arrays.asList();
        TestQuery testQuery = TestQuery.builder().idIn(ids).idNotIn(ids).build();

        assertEquals("SELECT * FROM user WHERE id IN (null)",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).isEmpty();
    }

    @Test
    void supportNot() {
        TestQuery testQuery = TestQuery.builder().userLevelNot(TestEnum.VIP).build();
        assertEquals("SELECT * FROM user WHERE userLevel != ?",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).containsExactly(0);
    }

    @Test
    void supportStart() {
        TestQuery testQuery = TestQuery.builder().usernameStart("test").build();
        assertEquals("SELECT * FROM user WHERE username LIKE ?",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).containsExactly("test%");
    }

    @Test
    void ignoreFieldWhenLikeValueIsEmpty() {
        TestQuery testQuery = TestQuery.builder().email("").usernameLike("").build();
        SqlAndArgs sqlAndArgs = testQueryBuilder.buildSelectIdAndArgs(testQuery);
        assertEquals("SELECT id FROM user WHERE email = ?", sqlAndArgs.getSql());
        assertThat(sqlAndArgs.getArgs()).containsExactly("");
    }

    @Test
    void supportResolveEnumListToOrdinalList() {
        TestQuery testQuery = TestQuery.builder().userLevelIn(Arrays.asList(TestEnum.NORMAL)).build();
        assertEquals("SELECT * FROM user WHERE userLevel IN (?)", testQueryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).containsExactly(1);
    }

    @Test
    void supportResolveEnumListToStringList() {
        TestQuery testQuery = TestQuery.builder().statusIn(Arrays.asList(TestStringEnum.E1)).build();
        assertEquals("SELECT * FROM user WHERE status IN (?)", testQueryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).containsExactly("E1");
    }

    @Test
    void buildSelectColumnsAndArgsForDynamicColumn() {
        DynamicQuery dynamicQuery = DynamicQuery.builder().user("f0rb").project("i18n").locale("zh").scoreLt(100).build();

        SqlAndArgs sqlAndArgs = dynamicQueryBuilder.buildSelectColumnsAndArgs(dynamicQuery, "locale_${locale}");

        assertEquals("SELECT locale_zh FROM t_dynamic_f0rb_i18n WHERE score < ?", sqlAndArgs.getSql());
        assertThat(sqlAndArgs.getArgs()).containsExactly(100);
    }

    @Test
    void buildSelectByIdForDynamicColumn() {
        DynamicIdWrapper idWrapper = new DynamicIdWrapper(2, "f0rb", "i18n", "zh");

        SqlAndArgs sqlAndArgs = dynamicQueryBuilder.buildSelectById(idWrapper, "locale_${locale}");

        assertEquals("SELECT locale_zh FROM t_dynamic_f0rb_i18n WHERE id = ?", sqlAndArgs.getSql());
        assertThat(sqlAndArgs.getArgs()).containsExactly(2);
    }

    @Test
    void buildOrClause() {
        AccountOr accountOr = AccountOr.builder().username("f0rb").email("f0rb").mobile("f0rb").build();
        TestQuery testQuery = TestQuery.builder().account2(accountOr).build();

        String sql = testQueryBuilder.buildSelectAndArgs(testQuery, argList);

        assertThat(sql).isEqualTo("SELECT * FROM user WHERE (username = ? OR email = ? OR mobile = ?)");
        assertThat(argList).containsExactly("f0rb", "f0rb", "f0rb");
    }

    @Test
    void buildOrClauseIgnoreNull() {
        AccountOr accountOr = AccountOr.builder().username("f0rb").email("f0rb").build();
        TestQuery testQuery = TestQuery.builder().account2(accountOr).build();

        String sql = testQueryBuilder.buildSelectAndArgs(testQuery, argList);

        assertThat(sql).isEqualTo("SELECT * FROM user WHERE (username = ? OR email = ?)");
        assertThat(argList).containsExactly("f0rb", "f0rb");
    }

    @Test
    void shouldIgnoreOrFieldWhenAllValuesAreNull() {
        AccountOr accountOr = AccountOr.builder().build();
        TestQuery testQuery = TestQuery.builder().account2(accountOr).build();

        String sql = testQueryBuilder.buildSelectAndArgs(testQuery, argList);

        assertThat(sql).isEqualTo("SELECT * FROM user");
        assertThat(argList).containsExactly();
    }

    @Test
    void buildAnyClause() {
        TestQuery queryByInvalid = TestQuery.builder().valid(false).build();
        TestQuery testQuery = TestQuery.builder().scoreGtAny(queryByInvalid).build();
        assertEquals("SELECT * FROM user WHERE score > ANY(SELECT score FROM t_user WHERE valid = ?)",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).containsExactly(false);
    }

    @Test
    void buildAllClause() {
        TestQuery queryByInvalid = TestQuery.builder().valid(false).build();
        TestQuery testQuery = TestQuery.builder().scoreLtAll(queryByInvalid).build();
        assertEquals("SELECT * FROM user WHERE score < ALL(SELECT score FROM t_user WHERE valid = ?)",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).containsExactly(false);
    }

    @Test
    void buildSubqueryWithComparisonOperators() {
        TestQuery queryByInvalid = TestQuery.builder().valid(false).build();
        TestQuery testQuery = TestQuery.builder().scoreGt1(queryByInvalid).build();
        assertEquals("SELECT * FROM user WHERE score > (SELECT avg(score) FROM t_user WHERE valid = ?)",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).containsExactly(false);
    }

    @Test
    void buildForSameColumnAndOperator() {
        TestQuery queryByInvalid = TestQuery.builder().valid(false).build();
        TestQuery testQuery = TestQuery.builder().scoreGt(60.).scoreGt1(queryByInvalid).build();
        assertEquals("SELECT * FROM user WHERE score > (SELECT avg(score) FROM t_user WHERE valid = ?) AND score > ?",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).containsExactly(false, 60.);
    }

    @Test
    void buildInQueryClause() {
        TestQuery queryByInvalid = TestQuery.builder().valid(false).build();
        TestQuery testQuery = TestQuery.builder().scoreIn(queryByInvalid).build();
        assertEquals("SELECT * FROM user WHERE score IN (SELECT score FROM t_user WHERE valid = ?)",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).containsExactly(false);
    }

    @Test
    void shouldNotParseFieldEndingWithDigitsButNotInstanceOfDoytoQuery() {
        TestQuery testQuery = TestQuery.builder().scoreGt2(60.).build();
        assertEquals("SELECT * FROM user WHERE scoreGt2 = ?",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).containsExactly(60.);
    }

    @Test
    void shouldParseField() {
        TestQuery queryByInvalid = TestQuery.builder().valid(false).build();
        TestQuery testQuery = TestQuery.builder().scoreGt$avgScoreFromUser(queryByInvalid).build();
        assertEquals("SELECT * FROM user WHERE score > (SELECT avg(score) FROM t_user WHERE valid = ?)",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).containsExactly(false);
    }
}