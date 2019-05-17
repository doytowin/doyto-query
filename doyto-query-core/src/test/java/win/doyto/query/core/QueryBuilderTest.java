package win.doyto.query.core;

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
 * @author f0rb
 * @date 2019-05-12
 */
public class QueryBuilderTest {

    private QueryBuilder queryBuilder = new QueryBuilder();

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
        LinkedList<Object> argList = new LinkedList<>();
        assertEquals("SELECT * FROM user WHERE username = ?",
                     queryBuilder.buildSelectAndArgs(userQuery, argList));
        assertEquals(1, argList.size());
        assertEquals("test", argList.get(0));
    }

    @Test
    public void buildSelectAndArgsWithCustomWhere() {
        LinkedList<Object> argList = new LinkedList<>();

        UserQuery userQuery = UserQuery.builder().account("test").build();
        assertEquals("SELECT * FROM user WHERE (username = ? OR email = ? OR mobile = ?)",
                     queryBuilder.buildSelectAndArgs(userQuery, argList));
        assertEquals(3, argList.size());
    }

    @Test
    public void buildCountAndArgsWithWhere() {
        UserQuery userQuery = UserQuery.builder().username("test").build();
        userQuery.setPageNumber(2).setPageSize(10);
        LinkedList<Object> argList = new LinkedList<>();
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
        UserQuery userQuery = UserQuery.builder().usernameLike("test").build();
        assertEquals("SELECT * FROM user WHERE username LIKE #{usernameLike}",
                     queryBuilder.buildSelect(userQuery));
    }

    @Test
    public void supportInSuffix() {
        List<Integer> ids = Arrays.asList(1, 2, 3);
        UserQuery userQuery = UserQuery.builder().idIn(ids).build();
        assertEquals("SELECT * FROM user WHERE id IN (#{idIn[0]}, #{idIn[1]}, #{idIn[2]})",
                     queryBuilder.buildSelect(userQuery));

        LinkedList<Object> argList = new LinkedList<>();
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

        LinkedList<Object> argList = new LinkedList<>();
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

        LinkedList<Object> argList = new LinkedList<>();
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

        LinkedList<Object> argList = new LinkedList<>();
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

        LinkedList<Object> argList = new LinkedList<>();
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

        LinkedList<Object> argList = new LinkedList<>();
        assertEquals("SELECT * FROM user WHERE username = ? AND createTime <= ?",
                     queryBuilder.buildSelectAndArgs(userQuery, argList));
        assertThat(argList).containsExactly("test", date);
    }

    @Test
    void supportOr() {
        UserQuery userQuery = UserQuery.builder().usernameOrEmailOrMobile("test").build();
        assertEquals("SELECT * FROM user WHERE (username = #{usernameOrEmailOrMobile} OR email = #{usernameOrEmailOrMobile} OR mobile = #{usernameOrEmailOrMobile})",
                     queryBuilder.buildSelect(userQuery));

        LinkedList<Object> argList = new LinkedList<>();
        assertEquals("SELECT * FROM user WHERE (username = ? OR email = ? OR mobile = ?)",
                     queryBuilder.buildSelectAndArgs(userQuery, argList));
        assertThat(argList).containsExactly("test", "test", "test");

    }

    @Test
    void supportOrWithLike() {
        UserQuery userQuery = UserQuery.builder().usernameOrEmailOrMobileLike("test").build();
        assertEquals("SELECT * FROM user WHERE (username LIKE #{usernameOrEmailOrMobileLike} OR email LIKE #{usernameOrEmailOrMobileLike} OR mobile LIKE #{usernameOrEmailOrMobileLike})",
                     queryBuilder.buildSelect(userQuery));

        LinkedList<Object> argList = new LinkedList<>();
        assertEquals("SELECT * FROM user WHERE (username LIKE ? OR email LIKE ? OR mobile LIKE ?)",
                     queryBuilder.buildSelectAndArgs(userQuery, argList));
        assertThat(argList).containsExactly("test", "test", "test");

    }
}
