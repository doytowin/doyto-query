package win.doyto.query.core;

import org.junit.jupiter.api.Test;

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
}
