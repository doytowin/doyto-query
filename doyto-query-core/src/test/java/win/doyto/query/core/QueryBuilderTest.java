package win.doyto.query.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import win.doyto.query.user.UserQuery;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
        argList = new LinkedList<>();
    }

    @Test
    public void buildSelect() {
        UserQuery userQuery = UserQuery.builder().build();
        assertEquals("SELECT * FROM user", queryBuilder.buildSelect(userQuery));
    }

    @Test
    public void buildSelectWithWhere() {
        UserQuery userQuery = UserQuery.builder().username("test").build();
        assertEquals("SELECT * FROM user WHERE username = #{username}", queryBuilder.buildSelect(userQuery));
    }

    @Test
    public void buildSelectWithWhereAndPage() {
        UserQuery userQuery = UserQuery.builder().username("test").build();
        userQuery.setPageNumber(3).setPageSize(10);
        assertEquals("SELECT * FROM user WHERE username = #{username} LIMIT 10 OFFSET 30",
                     queryBuilder.buildSelect(userQuery));
    }

    @Test
    public void buildSelectWithCustomWhere() {
        UserQuery userQuery = UserQuery.builder().account("test").build();
        assertEquals("SELECT * FROM user WHERE (username = #{account} OR email = #{account} OR mobile = #{account})",
                     queryBuilder.buildSelect(userQuery));
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

        LinkedList<Object> countArgList = new LinkedList<>();
        assertEquals("SELECT count(*) FROM user WHERE username = ?",
                     queryBuilder.buildCountAndArgs(userQuery, countArgList));
    }

    @Test
    public void buildCountWithWhere() {
        UserQuery userQuery = UserQuery.builder().username("test").build();
        assertEquals("SELECT count(*) FROM user WHERE username = #{username}",
                     queryBuilder.buildCount(userQuery));
    }

    @Test
    public void supportLikeSuffix() {
        UserQuery userQuery = UserQuery.builder().usernameLike("_test%f0rb").build();
        assertEquals("SELECT * FROM user WHERE username LIKE #{usernameLike}",
                     queryBuilder.buildSelect(userQuery));

        assertEquals("SELECT * FROM user WHERE username LIKE ?",
                     queryBuilder.buildSelectAndArgs(userQuery, argList));
        assertThat(argList).containsExactly("%\\_test\\%f0rb%");
    }

    @Test
    public void supportInSuffix() {
        List<Integer> ids = Arrays.asList(1, 2, 3);
        UserQuery userQuery = UserQuery.builder().idIn(ids).build();
        assertEquals("SELECT * FROM user WHERE id IN (#{idIn[0]}, #{idIn[1]}, #{idIn[2]})",
                     queryBuilder.buildSelect(userQuery));

        assertEquals("SELECT * FROM user WHERE id IN (?, ?, ?)",
                     queryBuilder.buildSelectAndArgs(userQuery, argList));
        assertThat(argList).containsExactly(1, 2, 3);

    }

    @Test
    public void supportNotInSuffix() {
        List<Integer> ids = Arrays.asList(1, 2, 3);
        UserQuery userQuery = UserQuery.builder().idNotIn(ids).build();
        assertEquals("SELECT * FROM user WHERE id NOT IN (#{idNotIn[0]}, #{idNotIn[1]}, #{idNotIn[2]})",
                     queryBuilder.buildSelect(userQuery));

        assertEquals("SELECT * FROM user WHERE id NOT IN (?, ?, ?)",
                     queryBuilder.buildSelectAndArgs(userQuery, argList));
        assertThat(argList).containsExactly(1, 2, 3);
    }

    @Test
    public void supportGtSuffix() {
        Date createTimeGt = new Date();
        UserQuery userQuery = UserQuery.builder().username("test").createTimeGt(createTimeGt).build();
        assertEquals("SELECT * FROM user WHERE username = #{username} AND createTime > #{createTimeGt}",
                     queryBuilder.buildSelect(userQuery));

        assertEquals("SELECT * FROM user WHERE username = ? AND createTime > ?",
                     queryBuilder.buildSelectAndArgs(userQuery, argList));
        assertThat(argList).containsExactly("test", createTimeGt);
    }

    @Test
    public void supportGeSuffix() {
        Date date = new Date();
        UserQuery userQuery = UserQuery.builder().username("test").createTimeGe(date).build();
        assertEquals("SELECT * FROM user WHERE username = #{username} AND createTime >= #{createTimeGe}",
                     queryBuilder.buildSelect(userQuery));

        assertEquals("SELECT * FROM user WHERE username = ? AND createTime >= ?",
                     queryBuilder.buildSelectAndArgs(userQuery, argList));
        assertThat(argList).containsExactly("test", date);
    }

    @Test
    public void supportLtSuffix() {
        Date date = new Date();
        UserQuery userQuery = UserQuery.builder().username("test").createTimeLt(date).build();
        assertEquals("SELECT * FROM user WHERE username = #{username} AND createTime < #{createTimeLt}",
                     queryBuilder.buildSelect(userQuery));

        assertEquals("SELECT * FROM user WHERE username = ? AND createTime < ?",
                     queryBuilder.buildSelectAndArgs(userQuery, argList));
        assertThat(argList).containsExactly("test", date);
    }

    @Test
    public void supportLeSuffix() {
        Date date = new Date();
        UserQuery userQuery = UserQuery.builder().username("test").createTimeLe(date).build();
        assertEquals("SELECT * FROM user WHERE username = #{username} AND createTime <= #{createTimeLe}",
                     queryBuilder.buildSelect(userQuery));

        assertEquals("SELECT * FROM user WHERE username = ? AND createTime <= ?",
                     queryBuilder.buildSelectAndArgs(userQuery, argList));
        assertThat(argList).containsExactly("test", date);
    }

    @Test
    void supportOr() {
        UserQuery userQuery = UserQuery.builder().usernameOrEmailOrMobile("test").build();
        assertEquals("SELECT * FROM user WHERE (username = #{usernameOrEmailOrMobile} OR email = #{usernameOrEmailOrMobile} OR mobile = #{usernameOrEmailOrMobile})",
                     queryBuilder.buildSelect(userQuery));

        assertEquals("SELECT * FROM user WHERE (username = ? OR email = ? OR mobile = ?)",
                     queryBuilder.buildSelectAndArgs(userQuery, argList));
        assertThat(argList).containsExactly("test", "test", "test");

    }

    @Test
    void supportOrWithLike() {
        UserQuery userQuery = UserQuery.builder().usernameOrEmailOrMobileLike("test").build();
        assertEquals("SELECT * FROM user WHERE (username LIKE #{usernameOrEmailOrMobileLike} OR email LIKE #{usernameOrEmailOrMobileLike} OR mobile LIKE #{usernameOrEmailOrMobileLike})",
                     queryBuilder.buildSelect(userQuery));

        assertEquals("SELECT * FROM user WHERE (username LIKE ? OR email LIKE ? OR mobile LIKE ?)",
                     queryBuilder.buildSelectAndArgs(userQuery, argList));
        assertThat(argList).containsExactly("%test%", "%test%", "%test%");

    }

    @Test
    public void supportSort() {
        UserQuery userQuery = UserQuery.builder().usernameLike("test").build();
        userQuery.setPageNumber(5).setPageSize(10).setSort("id,desc;createTime,asc");
        assertEquals("SELECT * FROM user WHERE username LIKE #{usernameLike} ORDER BY id desc, createTime asc LIMIT 10 OFFSET 50",
                     queryBuilder.buildSelect(userQuery));
    }

    @Test
    public void supportMapFieldToUnderscore() {
        GlobalConfiguration.instance().setMapCamelCaseToUnderscore(true);

        Date date = new Date();
        UserQuery userQuery = UserQuery.builder().userNameOrUserCodeLike("test").createTimeLt(date).build();
        assertEquals("SELECT * FROM user WHERE (user_name LIKE #{userNameOrUserCodeLike} OR user_code LIKE #{userNameOrUserCodeLike}) AND create_time < #{createTimeLt}",
                     queryBuilder.buildSelect(userQuery));

        assertEquals("SELECT * FROM user WHERE (user_name LIKE ? OR user_code LIKE ?) AND create_time < ?",
                     queryBuilder.buildSelectAndArgs(userQuery, argList));
        assertThat(argList).containsExactly("%test%", "%test%", date);
    }

    @Test
    public void supportDynamicTableName() {
        DynamicQuery dynamicQuery = DynamicQuery.builder().user("f0rb").project("i18n").scoreLt(100).build();

        assertEquals("SELECT * FROM t_dynamic_f0rb_i18n WHERE score < #{scoreLt}",
                     queryBuilder.buildSelect(dynamicQuery));

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
}
