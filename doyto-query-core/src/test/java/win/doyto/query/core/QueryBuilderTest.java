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

    @Test
    public void buildSelect() {
        UserQuery userQuery = UserQuery.builder().build();
        QueryBuilder queryBuilder = new QueryBuilder();
        assertEquals("SELECT * FROM user", queryBuilder.buildSelect(userQuery));
    }
}
