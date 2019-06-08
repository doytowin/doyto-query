package win.doyto.query.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.core.test.PermissionQuery;
import win.doyto.query.core.test.TestQuery;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * FieldProcessorTest
 *
 * @author f0rb on 2019-06-04
 */
class FieldProcessorTest {

    private List<Object> argList;

    @BeforeEach
    void setUp() {
        GlobalConfiguration.instance().setMapCamelCaseToUnderscore(false);
        argList = new ArrayList<>();
    }

    @Test
    void testResolveNestedQuery() throws NoSuchFieldException {
        TestQuery testQuery = TestQuery.builder().roleId(1).build();
        Field field = testQuery.getClass().getDeclaredField("roleId");
        String fieldName = field.getName();
        String sql = FieldProcessor.resolvedSubQuery(
            argList, 1, field.getAnnotation(SubQuery.class),
            new FieldProcessor.DefaultProcessor(fieldName));
        assertEquals("id IN (SELECT userId FROM t_user_and_role WHERE roleId = ?)", sql);
    }

    @Test
    void testResolveNestedQueries() throws NoSuchFieldException {
        PermissionQuery permissionQuery = PermissionQuery.builder().userId(2).build();
        Field field = permissionQuery.getClass().getDeclaredField("userId");
        String fieldName = field.getName();
        String sql = FieldProcessor.resolvedNestedQueries(
            argList, 2, field.getAnnotation(NestedQueries.class),
            new FieldProcessor.DefaultProcessor(fieldName));

        assertEquals("id IN (SELECT permId FROM t_role_and_perm WHERE roleId IN (SELECT roleId FROM t_user_and_role WHERE userId = ?))", sql);
        assertThat(argList).containsExactly(2);
    }

}