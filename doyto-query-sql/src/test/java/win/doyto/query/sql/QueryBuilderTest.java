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

package win.doyto.query.sql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.core.Dialect;
import win.doyto.query.core.IdWrapper;
import win.doyto.query.core.LockMode;
import win.doyto.query.test.*;
import win.doyto.query.test.tpch.domain.partsupp.PartsuppEntity;
import win.doyto.query.test.tpch.domain.partsupp.PartsuppKey;

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
class QueryBuilderTest {

    private final QueryBuilder testQueryBuilder = new QueryBuilder(TestEntity.class);
    private final QueryBuilder dynamicQueryBuilder = new QueryBuilder(DynamicEntity.class);
    private List<Object> argList;

    @BeforeEach
    void setUp() {
        argList = new ArrayList<>();
    }

    @Test
    void buildSelect() {
        TestQuery testQuery = TestQuery.builder().build();
        assertEquals("SELECT * FROM t_user t", testQueryBuilder.buildSelectAndArgs(testQuery, argList));
    }

    @Test
    void buildSelectWithWhere() {
        TestQuery testQuery = TestQuery.builder().username("test").build();
        assertEquals("SELECT * FROM t_user t WHERE username = ?", testQueryBuilder.buildSelectAndArgs(testQuery, argList));
    }

    @Test
    void buildSelectWithWhereAndPage() {
        TestQuery testQuery = TestQuery.builder().username("test").pageNumber(4).pageSize(10).build();
        assertEquals("SELECT * FROM t_user t WHERE username = ? LIMIT 10 OFFSET 30",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));
    }

    @Test
    void buildSelectWithCustomWhere() {
        TestQuery testQuery = TestQuery.builder().account("test").build();
        assertEquals("SELECT * FROM t_user t WHERE (username = ? OR email = ? OR mobile = ?)",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));
    }

    @Test
    void buildSelectWithArgs() {
        TestQuery testQuery = TestQuery.builder().username("test").build();
        assertEquals("SELECT * FROM t_user t WHERE username = ?",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));
        assertEquals(1, argList.size());
        assertEquals("test", argList.get(0));
    }

    @Test
    void buildSelectAndArgsWithCustomWhere() {

        TestQuery testQuery = TestQuery.builder().account("test").build();
        assertEquals("SELECT * FROM t_user t WHERE (username = ? OR email = ? OR mobile = ?)",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));
        assertEquals(3, argList.size());
    }

    @Test
    void buildCountAndArgsWithWhere() {
        TestQuery testQuery = TestQuery.builder().username("test").pageNumber(3).pageSize(10).sort("create_time,asc").build();

        assertEquals("SELECT * FROM t_user t WHERE username = ? ORDER BY create_time asc LIMIT 10 OFFSET 20",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));

        assertEquals("SELECT count(*) FROM t_user t WHERE username = ?",
                     testQueryBuilder.buildCountAndArgs(testQuery).getSql());
    }

    @Test
    void buildCountWithWhere() {
        TestQuery testQuery = TestQuery.builder().username("test").build();
        testQuery.setPageNumber(0);
        SqlAndArgs sqlAndArgs = testQueryBuilder.buildCountAndArgs(testQuery);
        assertEquals("SELECT count(*) FROM t_user t WHERE username = ?", sqlAndArgs.getSql());
        assertThat(sqlAndArgs.getArgs()).containsExactly("test");
    }

    @Test
    void supportLikeSuffix() {
        TestQuery testQuery = TestQuery.builder().usernameLike("_test%f0rb").build();

        assertEquals("SELECT * FROM t_user t WHERE username LIKE ?",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).containsExactly("_test%f0rb");
    }

    @Test
    void supportContainSuffix() {
        TestQuery testQuery = TestQuery.builder().usernameContain("_test%f0rb").build();

        assertEquals("SELECT * FROM t_user t WHERE username LIKE ? ESCAPE '\\'",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).containsExactly("%\\_test\\%f0rb%");
    }

    @Test
    void supportInSuffix() {
        List<Integer> ids = Arrays.asList(1, 2, 3);
        TestQuery testQuery = TestQuery.builder().idIn(ids).build();

        assertEquals("SELECT * FROM t_user t WHERE id IN (?, ?, ?)",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).containsExactly(1, 2, 3);

    }

    @Test
    void supportNotInSuffix() {
        TestQuery testQuery = TestQuery.builder().idNotIn(Arrays.asList(1, 2)).build();

        assertEquals("SELECT * FROM t_user t WHERE id NOT IN (?, ?)",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).containsExactly(1, 2);
    }

    @Test
    void supportGtSuffix() {
        Date createTimeGt = new Date();
        TestQuery testQuery = TestQuery.builder().username("test").createTimeGt(createTimeGt).build();

        assertEquals("SELECT * FROM t_user t WHERE username = ? AND create_time > ?",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).containsExactly("test", createTimeGt);
    }

    @Test
    void supportGeSuffix() {
        Date date = new Date();
        TestQuery testQuery = TestQuery.builder().username("test").createTimeGe(date).build();

        assertEquals("SELECT * FROM t_user t WHERE username = ? AND create_time >= ?",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).containsExactly("test", date);
    }

    @Test
    void supportLtSuffix() {
        Date date = new Date();
        TestQuery testQuery = TestQuery.builder().username("test").createTimeLt(date).build();

        assertEquals("SELECT * FROM t_user t WHERE username = ? AND create_time < ?",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).containsExactly("test", date);
    }

    @Test
    void supportLeSuffix() {
        Date date = new Date();
        TestQuery testQuery = TestQuery.builder().username("test").createTimeLe(date).build();

        assertEquals("SELECT * FROM t_user t WHERE username = ? AND create_time <= ?",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).containsExactly("test", date);
    }

    @Test
    void supportOr() {
        TestQuery testQuery = TestQuery.builder().usernameOrEmailOrMobile("test").build();

        assertEquals("SELECT * FROM t_user t WHERE (username = ? OR email = ? OR mobile = ?)",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).containsExactly("test", "test", "test");

    }

    @Test
    void supportOrWithLike() {
        TestQuery testQuery = TestQuery.builder().usernameOrEmailOrMobileLike("test").build();

        assertEquals("SELECT * FROM t_user t WHERE (username = ? OR email = ? OR mobile LIKE ?)",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).containsExactly("test", "test", "%test%");

    }

    @Test
    void supportSort() {
        TestQuery testQuery = TestQuery.builder().usernameLike("test")
                                       .pageNumber(6).pageSize(10)
                                       .sort("id,desc;create_time,asc").build();
        assertEquals("SELECT * FROM t_user t WHERE username LIKE ? ORDER BY id desc, create_time asc LIMIT 10 OFFSET 50",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));
    }

    @Test
    void buildSelectIdWithArgs() {
        TestQuery testQuery = TestQuery.builder().username("test").build();

        SqlAndArgs sqlAndArgs = testQueryBuilder.buildSelectColumnsAndArgs(testQuery, "id");

        assertEquals("SELECT id FROM t_user t WHERE username = ?", sqlAndArgs.getSql());
        assertThat(sqlAndArgs.getArgs()).containsExactly("test");
    }

    @Test
    void buildSelectColumnsAndArgs() {
        TestQuery testQuery = TestQuery.builder().build();

        SqlAndArgs sqlAndArgs = testQueryBuilder.buildSelectColumnsAndArgs(testQuery, "username", "password");

        assertEquals("SELECT username, password FROM t_user t", sqlAndArgs.getSql());
    }

    @Test
    void defaultEnumOrdinal() {
        TestQuery testQuery = TestQuery.builder().userLevel(TestEnum.VIP).build();
        assertEquals("SELECT * FROM t_user t WHERE user_level = ?",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).containsExactly(0);

    }

    @Test
    void supportIsNull() {
        TestQuery testQuery = TestQuery.builder().memoNull(true).build();

        String sql = testQueryBuilder.buildSelectAndArgs(testQuery, argList);

        assertThat(sql).isEqualTo("SELECT * FROM t_user t WHERE memo IS NULL");
        assertThat(argList).isEmpty();
    }

    @Test
    void supportIsNullWithValueFalse() {
        TestQuery testQuery = TestQuery.builder().memoNull(false).build();

        String sql = testQueryBuilder.buildSelectAndArgs(testQuery, argList);

        assertThat(sql).isEqualTo("SELECT * FROM t_user t WHERE memo IS NOT NULL");
        assertThat(argList).isEmpty();
    }

    @Test
    void customPageDialect() {
        GlobalConfiguration globalConfiguration = GlobalConfiguration.instance();
        Dialect origin = globalConfiguration.getDialect();
        globalConfiguration.setDialect(
            (sql, limit, offset) -> String.format("SELECT LIMIT %d %d %s", offset, offset + limit, sql.substring("SELECT ".length())));

        TestQuery testQuery = TestQuery.builder().pageNumber(3).pageSize(10).build();
        assertEquals("SELECT LIMIT 20 30 * FROM t_user t",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));

        // reset
        globalConfiguration.setDialect(origin);

    }

    @Test
    void ignoreNotInWhenEmpty() {
        List<Integer> ids = Arrays.asList();
        TestQuery testQuery = TestQuery.builder().idIn(ids).idNotIn(ids).build();

        assertEquals("SELECT * FROM t_user t WHERE id IN (null)",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).isEmpty();
    }

    @Test
    void supportNot() {
        TestQuery testQuery = TestQuery.builder().userLevelNot(TestEnum.VIP).build();
        assertEquals("SELECT * FROM t_user t WHERE user_level != ?",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).containsExactly(0);
    }

    @Test
    void supportStart() {
        TestQuery testQuery = TestQuery.builder().usernameStart("test").build();
        assertEquals("SELECT * FROM t_user t WHERE username LIKE ?",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).containsExactly("test%");
    }

    @Test
    void ignoreFieldWhenLikeValueIsEmpty() {
        TestQuery testQuery = TestQuery.builder().email("").usernameLike("").build();
        SqlAndArgs sqlAndArgs = testQueryBuilder.buildSelectIdAndArgs(testQuery);
        assertEquals("SELECT id FROM t_user t WHERE email = ?", sqlAndArgs.getSql());
        assertThat(sqlAndArgs.getArgs()).containsExactly("");
    }

    @Test
    void supportResolveEnumListToOrdinalList() {
        TestQuery testQuery = TestQuery.builder().userLevelIn(Arrays.asList(TestEnum.NORMAL)).build();
        assertEquals("SELECT * FROM t_user t WHERE user_level IN (?)", testQueryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).containsExactly(1);
    }

    @Test
    void supportResolveEnumListToStringList() {
        TestQuery testQuery = TestQuery.builder().statusIn(Arrays.asList(TestStringEnum.E1)).build();
        assertEquals("SELECT * FROM t_user t WHERE status IN (?)", testQueryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).containsExactly("E1");
    }

    @Test
    void buildSelectColumnsAndArgsForDynamicColumn() {
        DynamicQuery dynamicQuery = DynamicQuery.builder().user("f0rb").project("i18n").locale("zh").scoreLt(100).build();

        SqlAndArgs sqlAndArgs = dynamicQueryBuilder.buildSelectColumnsAndArgs(dynamicQuery, "locale_${locale}");

        assertEquals("SELECT locale_zh FROM t_dynamic_f0rb_i18n t WHERE score < ?", sqlAndArgs.getSql());
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
        Account accountOr = Account.builder().username("f0rb").email("f0rb").mobile("f0rb").build();
        TestQuery testQuery = TestQuery.builder().accountOr(accountOr).build();

        String sql = testQueryBuilder.buildSelectAndArgs(testQuery, argList);

        assertThat(sql).isEqualTo("SELECT * FROM t_user t WHERE (username = ? OR email = ? OR mobile = ?)");
        assertThat(argList).containsExactly("f0rb", "f0rb", "f0rb");
    }

    @Test
    void buildOrClauseForQueryObjectNamedOr() {
        TestQuery or = TestQuery.builder().username("f0rb").email("f0rb").build();
        TestQuery testQuery = TestQuery.builder().or(or).build();

        String sql = testQueryBuilder.buildSelectAndArgs(testQuery, argList);

        assertThat(sql).isEqualTo("SELECT * FROM t_user t WHERE (username = ? OR email = ?)");
        assertThat(argList).containsExactly("f0rb", "f0rb");
    }

    @Test
    void buildAndClauseForQueryObjectNamedAnd() {
        TestQuery and = TestQuery.builder().username("f0rb").email("f0rb").build();
        TestQuery testQuery = TestQuery.builder().and(and).build();

        String sql = testQueryBuilder.buildSelectAndArgs(testQuery, argList);

        assertThat(sql).isEqualTo("SELECT * FROM t_user t WHERE username = ? AND email = ?");
        assertThat(argList).containsExactly("f0rb", "f0rb");
    }

    @Test
    void buildOrClauseIgnoreNull() {
        Account accountOr = Account.builder().username("f0rb").email("f0rb").build();
        TestQuery testQuery = TestQuery.builder().accountOr(accountOr).build();

        String sql = testQueryBuilder.buildSelectAndArgs(testQuery, argList);

        assertThat(sql).isEqualTo("SELECT * FROM t_user t WHERE (username = ? OR email = ?)");
        assertThat(argList).containsExactly("f0rb", "f0rb");
    }

    @Test
    void buildOrClauseForBasicTypeCollection() {
        TestQuery testQuery = TestQuery.builder().usernameContainOr(Arrays.asList("test1", "test2", "test3")).build();

        String sql = testQueryBuilder.buildSelectAndArgs(testQuery, argList);

        assertThat(sql).isEqualTo("SELECT * FROM t_user t WHERE (username LIKE ? OR username LIKE ? OR username LIKE ?)");
        assertThat(argList).containsExactly("%test1%", "%test2%", "%test3%");
    }

    @Test
    void buildOrClauseForBasicTypeCollectionWithZeroElems() {
        TestQuery testQuery = TestQuery.builder().usernameContainOr(List.of()).build();

        String sql = testQueryBuilder.buildSelectAndArgs(testQuery, argList);

        assertThat(sql).isEqualTo("SELECT * FROM t_user t");
        assertThat(argList).isEmpty();
    }

    @Test
    void shouldIgnoreOrFieldWhenAllValuesAreNull() {
        Account accountOr = Account.builder().build();
        TestQuery testQuery = TestQuery.builder().accountOr(accountOr).build();

        String sql = testQueryBuilder.buildSelectAndArgs(testQuery, argList);

        assertThat(sql).isEqualTo("SELECT * FROM t_user t");
        assertThat(argList).containsExactly();
    }

    @Test
    void buildAnyClause() {
        TestQuery queryByInvalid = TestQuery.builder().valid(false).build();
        TestQuery testQuery = TestQuery.builder().scoreGtAny(queryByInvalid).build();
        assertEquals("SELECT * FROM t_user t WHERE score > ANY(SELECT score FROM t_user WHERE valid = ?)",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).containsExactly(false);
    }

    @Test
    void buildAllClause() {
        TestQuery queryByInvalid = TestQuery.builder().valid(false).build();
        TestQuery testQuery = TestQuery.builder().scoreLtAll(queryByInvalid).build();
        assertEquals("SELECT * FROM t_user t WHERE score < ALL(SELECT score FROM t_user WHERE valid = ?)",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).containsExactly(false);
    }

    @Test
    void buildSubqueryWithComparisonOperators() {
        TestQuery queryByInvalid = TestQuery.builder().valid(false).build();
        TestQuery testQuery = TestQuery.builder().scoreGt1(queryByInvalid).build();
        assertEquals("SELECT * FROM t_user t WHERE score > (SELECT avg(score) FROM t_user WHERE valid = ?)",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).containsExactly(false);
    }

    @Test
    void buildSubqueryWithComparisonOperatorsAndAggregateColumn() {
        TestQuery queryByInvalid = TestQuery.builder().valid(false).build();
        TestQuery testQuery = TestQuery.builder().scoreGt3(queryByInvalid).build();
        assertEquals("SELECT * FROM t_user t WHERE score > (SELECT avg(score) FROM t_user WHERE valid = ?)",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).containsExactly(false);
    }

    @Test
    void buildForSameColumnAndOperator() {
        TestQuery queryByInvalid = TestQuery.builder().valid(false).build();
        TestQuery testQuery = TestQuery.builder().scoreGt(60.).scoreGt1(queryByInvalid).build();
        assertEquals("SELECT * FROM t_user t WHERE score > (SELECT avg(score) FROM t_user WHERE valid = ?) AND score > ?",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).containsExactly(false, 60.);
    }

    @Test
    void buildInQueryClause() {
        TestQuery queryByInvalid = TestQuery.builder().valid(false).build();
        TestQuery testQuery = TestQuery.builder().scoreIn(queryByInvalid).build();
        assertEquals("SELECT * FROM t_user t WHERE score IN (SELECT score FROM t_user WHERE valid = ?)",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).containsExactly(false);
    }

    @Test
    void shouldNotParseFieldEndingWithDigitsButNotInstanceOfDoytoQuery() {
        TestQuery testQuery = TestQuery.builder().scoreGt2(60.).build();
        assertEquals("SELECT * FROM t_user t WHERE score_gt2 = ?",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).containsExactly(60.);
    }

    @Test
    void shouldParseFieldWithSubqueryFormat() {
        TestQuery queryByInvalid = TestQuery.builder().valid(false).build();
        TestQuery testQuery = TestQuery.builder().scoreGt$avgScoreFromUser(queryByInvalid).build();
        assertEquals("SELECT * FROM t_user t WHERE score > (SELECT avg(score) FROM t_user WHERE valid = ?)",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).containsExactly(false);
    }

    @Test
    void shouldSupportExists() {
        TestQuery queryByInvalid = TestQuery.builder().valid(false).build();
        TestQuery testQuery = TestQuery.builder().userExists(queryByInvalid).build();
        assertEquals("SELECT * FROM t_user t WHERE EXISTS(SELECT * FROM t_user t1 WHERE t.id = t1.create_user_id AND t1.valid = ?)",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).containsExactly(false);
    }

    @Test
    void shouldSupportNotExists() {
        TestQuery queryByInvalid = TestQuery.builder().valid(false).build();
        TestQuery testQuery = TestQuery.builder().userNotExists(queryByInvalid).build();
        assertEquals("SELECT * FROM t_user t WHERE NOT EXISTS(SELECT * FROM t_user t1 WHERE t.id = t1.create_user_id AND t1.valid = ?)",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).containsExactly(false);
    }

    @Test
    void buildSelectByCompositeId() {
        QueryBuilder queryBuilder = new QueryBuilder(PartsuppEntity.class);
        PartsuppKey partSuppKey = new PartsuppKey(1, 2);
        SqlAndArgs sqlAndArgs = queryBuilder.buildSelectById(IdWrapper.build(partSuppKey), "*");

        assertThat(sqlAndArgs.getSql()).isEqualTo("SELECT * FROM t_partsupp WHERE ps_partkey = ? AND ps_suppkey = ?");
        assertThat(sqlAndArgs.getArgs()).containsExactly(1, 2);
    }

    @Test
    void buildSelectForShare() {
        TestQuery testQuery = TestQuery.builder().id(1).lockMode(LockMode.PESSIMISTIC_READ).build();

        String sql = testQueryBuilder.buildSelectAndArgs(testQuery, argList);

        assertThat(sql).isEqualTo("SELECT * FROM t_user t WHERE id = ? FOR SHARE");
        assertThat(argList).containsExactly(1);
    }

    @Test
    void buildSelectForUpdate() {
        TestQuery testQuery = TestQuery.builder().id(1).lockMode(LockMode.PESSIMISTIC_WRITE).build();

        String sql = testQueryBuilder.buildSelectAndArgs(testQuery, argList);

        assertThat(sql).isEqualTo("SELECT * FROM t_user t WHERE id = ? FOR UPDATE");
        assertThat(argList).containsExactly(1);
    }
}
