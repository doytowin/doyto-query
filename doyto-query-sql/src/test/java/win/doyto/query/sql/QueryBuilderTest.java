package win.doyto.query.sql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
class QueryBuilderTest {

    private QueryBuilder testQueryBuilder = new QueryBuilder(TestEntity.class);
    private QueryBuilder menuQueryBuilder = new QueryBuilder("menu", "id");
    private QueryBuilder permQueryBuilder = new QueryBuilder("permission", "id");
    private QueryBuilder dynamicQueryBuilder = new QueryBuilder(DynamicEntity.class);
    private List<Object> argList;

    @BeforeEach
    void setUp() {
        GlobalConfiguration.instance().setMapCamelCaseToUnderscore(false);
        GlobalConfiguration.instance().setDialect(new SimpleDialect());
        argList = new ArrayList<>();
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
        TestQuery testQuery = TestQuery.builder().username("test").pageNumber(3).pageSize(10).build();
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
        TestQuery testQuery = TestQuery.builder().username("test").pageNumber(2).pageSize(10).sort("createTime,asc").build();

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
                                       .pageNumber(5).pageSize(10)
                                       .sort("id,desc;createTime,asc").build();
        assertEquals("SELECT * FROM user WHERE username LIKE ? ORDER BY id desc, createTime asc LIMIT 10 OFFSET 50",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));
    }

    @Test
    void supportMapFieldToUnderscore() {
        GlobalConfiguration.instance().setMapCamelCaseToUnderscore(true);

        Date date = new Date();
        TestQuery testQuery = TestQuery.builder().userNameOrUserCodeLike("test").createTimeLt(date).build();

        assertEquals("SELECT * FROM user WHERE (user_name = ? OR user_code LIKE ?) AND create_time < ?",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).containsExactly("test", "%test%", date);
    }

    @Test
    void buildSubquery() {
        TestQuery testQuery = TestQuery.builder().roleId(1).build();

        assertEquals("SELECT * FROM user WHERE id IN (SELECT userId FROM t_user_and_role WHERE roleId = ?)",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).containsExactly(1);
    }


    @Test
    void buildNestedQuery() {
        PermissionQuery permissionQuery = PermissionQuery.builder().userId(1).build();

        assertEquals("SELECT * FROM permission WHERE id IN (SELECT permId FROM t_role_and_perm WHERE roleId IN " +
                         "(SELECT roleId FROM t_user_and_role WHERE userId = ?))",
                     permQueryBuilder.buildSelectAndArgs(permissionQuery, argList));
        assertThat(argList).containsExactly(1);
    }

    @Test
    void buildNestedQuery2() {
        MenuQuery menuQuery = MenuQuery.builder().userId(1).build();

        String expected = "SELECT * FROM menu WHERE id IN (" +
            "SELECT menuId FROM t_perm_and_menu pm inner join t_perm p on p.id = pm.perm_id and p.valid = true WHERE permId IN (" +
            "SELECT permId FROM t_role_and_perm rp inner join t_role r on r.id = rp.role_id and r.valid = true WHERE roleId IN (" +
            "SELECT roleId FROM t_user_and_role WHERE userId = ?)))";
        assertEquals(expected, menuQueryBuilder.buildSelectAndArgs(menuQuery, argList));
        assertThat(argList).containsExactly(1);
    }

    @Test
    void build_boolean_field() {
        MenuQuery menuQuery = MenuQuery.builder().onlyParent(true).build();

        String expected = "SELECT * FROM menu WHERE id IN (SELECT parent_id FROM menu)";
        assertEquals(expected, menuQueryBuilder.buildSelectAndArgs(menuQuery, argList));
        assertThat(argList).isEmpty();
    }

    /**
     * 感觉这个测试用例对TDD本身也是一个挑战,
     * 随着设计的不断抽象, 小步迭代式开发也不那么直观了
     * 必须要熟悉源码才能准确找到修改哪里
     */
    @Test
    void buildSubQueryWithQueryObject() {
        MenuQuery parentQuery = MenuQuery.builder().nameLike("test").valid(true).build();
        MenuQuery menuQuery = MenuQuery.builder().parent(parentQuery).build();

        String expected = "SELECT * FROM menu WHERE id IN (SELECT parent_id FROM menu WHERE name LIKE ? AND valid = ?)";
        assertEquals(expected, menuQueryBuilder.buildSelectAndArgs(menuQuery, argList));
        assertThat(argList).containsExactly("%test%", true);
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
        MenuQuery byNoParent = MenuQuery.builder().parentIdNull(true).build();

        assertEquals("SELECT * FROM menu WHERE parentId IS NULL",
                     menuQueryBuilder.buildSelectAndArgs(byNoParent, argList));
        assertThat(argList).isEmpty();
    }

    @Test
    void customPageDialect() {
        GlobalConfiguration globalConfiguration = GlobalConfiguration.instance();
        Dialect origin = globalConfiguration.getDialect();
        globalConfiguration.setDialect(
            (sql, limit, offset) -> String.format("SELECT LIMIT %d %d %s", offset, offset + limit, sql.substring("SELECT ".length())));

        TestQuery testQuery = TestQuery.builder().pageNumber(2).pageSize(10).build();
        assertEquals("SELECT LIMIT 20 30 * FROM user",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));

        // reset
        globalConfiguration.setDialect(origin);

    }

    @Test
    void buildNestedQueryIgnoreWhere() {
        PermissionQuery permissionQuery = PermissionQuery.builder().validUser(true).build();

        assertEquals("SELECT * FROM permission WHERE id IN (SELECT permId FROM t_role_and_perm WHERE roleId IN " +
                         "(SELECT roleId FROM t_user_and_role ur inner join user u on u.id = ur.userId and u.valid = ?))",
                     permQueryBuilder.buildSelectAndArgs(permissionQuery, argList));
        assertThat(argList).containsExactly(true);
    }

    @Test
    void buildSubQueryWithCollection() {
        PermissionQuery permissionQuery = PermissionQuery.builder().roleIdIn(Arrays.asList(1, 2, 3)).build();
        assertEquals("SELECT * FROM permission WHERE id IN (SELECT permId FROM t_role_and_perm WHERE roleId IN (?, ?, ?))",
                     permQueryBuilder.buildSelectAndArgs(permissionQuery, argList));
        assertThat(argList).containsExactly(1, 2, 3);
    }

    @Test
    void buildSubQueryWithNullCollection() {
        GlobalConfiguration.instance().setMapCamelCaseToUnderscore(true);

        PermissionQuery nullQuery = PermissionQuery.builder().roleIdIn(Arrays.asList()).build();
        assertEquals("SELECT * FROM permission WHERE id IN (SELECT permId FROM t_role_and_perm WHERE role_id IN (null))",
                     permQueryBuilder.buildSelectAndArgs(nullQuery, argList));
        assertThat(argList).isEmpty();
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
}