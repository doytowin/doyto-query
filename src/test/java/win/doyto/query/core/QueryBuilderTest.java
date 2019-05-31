package win.doyto.query.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import win.doyto.query.core.module.menu.MenuQuery;
import win.doyto.query.core.module.permission.PermissionQuery;
import win.doyto.query.core.module.user.UserLevel;
import win.doyto.query.core.module.user.UserQuery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static win.doyto.query.core.QueryBuilder.resolvedNestedQuery;

/**
 * QueryBuilderTest
 *
 * @author f0rb 2019-05-12
 */
public class QueryBuilderTest {

    private QueryBuilder queryBuilder = new QueryBuilder();
    private List<Object> argList;

    @BeforeEach
    void setUp() {
        GlobalConfiguration.instance().setMapCamelCaseToUnderscore(false);
        argList = new ArrayList<>();
    }

    @Test
    public void buildSelect() {
        UserQuery userQuery = UserQuery.builder().build();
        assertEquals("SELECT * FROM user", queryBuilder.buildSelectAndArgs(userQuery, argList));
    }

    @Test
    public void buildSelectWithWhere() {
        UserQuery userQuery = UserQuery.builder().username("test").build();
        assertEquals("SELECT * FROM user WHERE username = ?", queryBuilder.buildSelectAndArgs(userQuery, argList));
    }

    @Test
    public void buildSelectWithWhereAndPage() {
        UserQuery userQuery = UserQuery.builder().username("test").build();
        userQuery.setPageNumber(3).setPageSize(10);
        assertEquals("SELECT * FROM user WHERE username = ? LIMIT 10 OFFSET 30",
                     queryBuilder.buildSelectAndArgs(userQuery, argList));
    }

    @Test
    public void buildSelectWithCustomWhere() {
        UserQuery userQuery = UserQuery.builder().account("test").build();
        assertEquals("SELECT * FROM user WHERE (username = ? OR email = ? OR mobile = ?)",
                     queryBuilder.buildSelectAndArgs(userQuery, argList));
    }

    @Test
    public void buildSelectWithArgs() {
        UserQuery userQuery = UserQuery.builder().username("test").build();
        assertEquals("SELECT * FROM user WHERE username = ?",
                     queryBuilder.buildSelectAndArgs(userQuery, argList));
        assertEquals(1, argList.size());
        assertEquals("test", argList.get(0));
    }

    @Test
    public void buildSelectAndArgsWithCustomWhere() {

        UserQuery userQuery = UserQuery.builder().account("test").build();
        assertEquals("SELECT * FROM user WHERE (username = ? OR email = ? OR mobile = ?)",
                     queryBuilder.buildSelectAndArgs(userQuery, argList));
        assertEquals(3, argList.size());
    }

    @Test
    public void buildCountAndArgsWithWhere() {
        UserQuery userQuery = UserQuery.builder().username("test").build();
        userQuery.setPageNumber(2).setPageSize(10);
        assertEquals("SELECT * FROM user WHERE username = ? LIMIT 10 OFFSET 20",
                     queryBuilder.buildSelectAndArgs(userQuery, argList));

        List<Object> countArgList = new ArrayList<>();
        assertEquals("SELECT count(*) FROM user WHERE username = ?",
                     queryBuilder.buildCountAndArgs(userQuery, countArgList));
    }

    @Test
    public void buildCountWithWhere() {
        UserQuery userQuery = UserQuery.builder().username("test").build();
        userQuery.setPageNumber(0);
        assertEquals("SELECT count(*) FROM user WHERE username = ?",
                     queryBuilder.buildCountAndArgs(userQuery, argList));
        assertThat(argList).containsExactly("test");
    }

    @Test
    public void supportLikeSuffix() {
        UserQuery userQuery = UserQuery.builder().usernameLike("_test%f0rb").build();

        assertEquals("SELECT * FROM user WHERE username LIKE ?",
                     queryBuilder.buildSelectAndArgs(userQuery, argList));
        assertThat(argList).containsExactly("%\\_test\\%f0rb%");
    }

    @Test
    public void supportInSuffix() {
        List<Integer> ids = Arrays.asList(1, 2, 3);
        UserQuery userQuery = UserQuery.builder().idIn(ids).build();

        assertEquals("SELECT * FROM user WHERE id IN (?, ?, ?)",
                     queryBuilder.buildSelectAndArgs(userQuery, argList));
        assertThat(argList).containsExactly(1, 2, 3);

    }

    @Test
    public void supportNotInSuffix() {
        UserQuery userQuery = UserQuery.builder().idNotIn(Arrays.asList()).build();

        assertEquals("SELECT * FROM user WHERE id NOT IN (null)",
                     queryBuilder.buildSelectAndArgs(userQuery, argList));
        assertThat(argList).isEmpty();
    }

    @Test
    public void supportGtSuffix() {
        Date createTimeGt = new Date();
        UserQuery userQuery = UserQuery.builder().username("test").createTimeGt(createTimeGt).build();

        assertEquals("SELECT * FROM user WHERE username = ? AND createTime > ?",
                     queryBuilder.buildSelectAndArgs(userQuery, argList));
        assertThat(argList).containsExactly("test", createTimeGt);
    }

    @Test
    public void supportGeSuffix() {
        Date date = new Date();
        UserQuery userQuery = UserQuery.builder().username("test").createTimeGe(date).build();

        assertEquals("SELECT * FROM user WHERE username = ? AND createTime >= ?",
                     queryBuilder.buildSelectAndArgs(userQuery, argList));
        assertThat(argList).containsExactly("test", date);
    }

    @Test
    public void supportLtSuffix() {
        Date date = new Date();
        UserQuery userQuery = UserQuery.builder().username("test").createTimeLt(date).build();

        assertEquals("SELECT * FROM user WHERE username = ? AND createTime < ?",
                     queryBuilder.buildSelectAndArgs(userQuery, argList));
        assertThat(argList).containsExactly("test", date);
    }

    @Test
    public void supportLeSuffix() {
        Date date = new Date();
        UserQuery userQuery = UserQuery.builder().username("test").createTimeLe(date).build();

        assertEquals("SELECT * FROM user WHERE username = ? AND createTime <= ?",
                     queryBuilder.buildSelectAndArgs(userQuery, argList));
        assertThat(argList).containsExactly("test", date);
    }

    @Test
    void supportOr() {
        UserQuery userQuery = UserQuery.builder().usernameOrEmailOrMobile("test").build();

        assertEquals("SELECT * FROM user WHERE (username = ? OR email = ? OR mobile = ?)",
                     queryBuilder.buildSelectAndArgs(userQuery, argList));
        assertThat(argList).containsExactly("test", "test", "test");

    }

    @Test
    void supportOrWithLike() {
        UserQuery userQuery = UserQuery.builder().usernameOrEmailOrMobileLike("test").build();

        assertEquals("SELECT * FROM user WHERE (username LIKE ? OR email LIKE ? OR mobile LIKE ?)",
                     queryBuilder.buildSelectAndArgs(userQuery, argList));
        assertThat(argList).containsExactly("%test%", "%test%", "%test%");

    }

    @Test
    public void supportSort() {
        UserQuery userQuery = UserQuery.builder().usernameLike("test").build();
        userQuery.setPageNumber(5).setPageSize(10).setSort("id,desc;createTime,asc");
        assertEquals("SELECT * FROM user WHERE username LIKE ? ORDER BY id desc, createTime asc LIMIT 10 OFFSET 50",
                     queryBuilder.buildSelectAndArgs(userQuery, argList));
    }

    @Test
    public void supportMapFieldToUnderscore() {
        GlobalConfiguration.instance().setMapCamelCaseToUnderscore(true);

        Date date = new Date();
        UserQuery userQuery = UserQuery.builder().userNameOrUserCodeLike("test").createTimeLt(date).build();

        assertEquals("SELECT * FROM user WHERE (user_name LIKE ? OR user_code LIKE ?) AND create_time < ?",
                     queryBuilder.buildSelectAndArgs(userQuery, argList));
        assertThat(argList).containsExactly("%test%", "%test%", date);
    }

    @Test
    public void supportDynamicTableName() {
        DynamicQuery dynamicQuery = DynamicQuery.builder().user("f0rb").project("i18n").scoreLt(100).build();

        assertEquals("SELECT * FROM t_dynamic_f0rb_i18n WHERE score < ?",
                     queryBuilder.buildSelectAndArgs(dynamicQuery, argList));
        assertThat(argList).containsExactly(100);
    }

    @Test
    public void buildDeleteAndArgs() {
        UserQuery userQuery = UserQuery.builder().username("test").build();
        userQuery.setPageNumber(3).setPageSize(10);
        assertEquals("DELETE FROM user WHERE username = ? LIMIT 10",
                     queryBuilder.buildDeleteAndArgs(userQuery, argList));
        assertEquals(1, argList.size());
        assertEquals("test", argList.get(0));
    }

    @Test
    public void buildSubquery() {
        UserQuery userQuery = UserQuery.builder().roleId(1).build();

        assertEquals("SELECT * FROM user WHERE id IN (SELECT userId FROM t_user_and_role WHERE roleId = ?)",
                     queryBuilder.buildSelectAndArgs(userQuery, argList));
        assertThat(argList).containsExactly(1);
    }

    @Test
    void testResolveNestedQuery() throws NoSuchFieldException {
        UserQuery userQuery = UserQuery.builder().roleId(1).build();
        assertEquals("id IN (SELECT userId FROM t_user_and_role WHERE roleId = ?)",
                     resolvedNestedQuery(userQuery.getClass().getDeclaredField("roleId")));
    }

    @Test
    void testResolveNestedQueries() throws NoSuchFieldException {
        PermissionQuery permissionQuery = PermissionQuery.builder().userId(1).build();
        assertEquals("id IN (SELECT permId FROM t_role_and_perm WHERE roleId IN (SELECT roleId FROM t_user_and_role WHERE userId = ?))",
                     resolvedNestedQuery(permissionQuery.getClass().getDeclaredField("userId")));
    }

    @Test
    public void buildNestedQuery() {
        PermissionQuery permissionQuery = PermissionQuery.builder().userId(1).build();

        assertEquals("SELECT * FROM permission WHERE id IN (SELECT permId FROM t_role_and_perm WHERE roleId IN " +
                         "(SELECT roleId FROM t_user_and_role WHERE userId = ?))",
                     queryBuilder.buildSelectAndArgs(permissionQuery, argList));
        assertThat(argList).containsExactly(1);
    }

    @Test
    public void buildNestedQuery2() {
        MenuQuery menuQuery = MenuQuery.builder().userId(1).build();

        String expected = "SELECT * FROM menu WHERE id IN (" +
            "SELECT menuId FROM t_perm_and_menu pm inner join t_perm p on p.id = pm.perm_id and p.valid = true WHERE permId IN (" +
            "SELECT permId FROM t_role_and_perm rp inner join t_role r on r.id = rp.role_id and r.valid = true WHERE roleId IN (" +
            "SELECT roleId FROM t_user_and_role WHERE userId = ?)))";
        assertEquals(expected, queryBuilder.buildSelectAndArgs(menuQuery, argList));
        assertThat(argList).containsExactly(1);
    }

    @Test
    public void build_boolean_field() {
        MenuQuery menuQuery = MenuQuery.builder().onlyParent(true).build();

        String expected = "SELECT * FROM menu WHERE id IN (SELECT parent_id FROM menu WHERE true = ?)";
        assertEquals(expected, queryBuilder.buildSelectAndArgs(menuQuery, argList));
        assertThat(argList).containsExactly(true);
    }

    @Test
    public void buildSelectIdWithArgs() {
        UserQuery userQuery = UserQuery.builder().username("test").build();
        assertEquals("SELECT id FROM user WHERE username = ?",
                     queryBuilder.buildSelectColumnsAndArgs(userQuery, argList, "id"));
        assertEquals(1, argList.size());
        assertThat(argList).containsExactly("test");
    }

    @Test
    public void buildSelectColumnsAndArgs() {
        UserQuery userQuery = UserQuery.builder().build();
        assertEquals("SELECT username, password FROM user",
                     queryBuilder.buildSelectColumnsAndArgs(userQuery, argList, "username", "password"));
    }

    @Test
    public void defaultEnumOrdinal() {
        UserQuery userQuery = UserQuery.builder().userLevel(UserLevel.VIP).build();
        assertEquals("SELECT * FROM user WHERE userLevel = ?",
                     queryBuilder.buildSelectAndArgs(userQuery, argList));
        assertThat(argList).containsExactly(0);

    }

    @Test
    public void fixSQLInject() {
        DynamicQuery dynamicQuery = DynamicQuery.builder().user("f0rb").project("; DROP TABLE menu;").scoreLt(100).build();

        assertEquals("SELECT * FROM t_dynamic_f0rb_${project} WHERE project = ? AND score < ?",
                     queryBuilder.buildSelectAndArgs(dynamicQuery, argList));
        assertThat(argList).containsExactly("; DROP TABLE menu;", 100);
    }
}