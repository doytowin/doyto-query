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
}
