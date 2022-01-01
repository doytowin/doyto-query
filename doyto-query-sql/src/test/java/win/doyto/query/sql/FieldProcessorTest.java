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

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.test.PermissionQuery;
import win.doyto.query.test.UserLevel;
import win.doyto.query.test.UserQuery;

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

    @SneakyThrows
    private Field initField(String fieldName) {
        Field field = PermissionQuery.class.getDeclaredField(fieldName);
        FieldProcessor.init(field);
        return field;
    }

    @Test
    void testResolveNestedQueries() {
        Field field = initField("userId");

        String sql = FieldProcessor.execute(field, argList, 2);

        String expected = "id IN (SELECT permId FROM t_role_and_perm WHERE roleId IN " +
                "(SELECT roleId FROM t_user_and_role WHERE userId = ?))";
        assertEquals(expected, sql);
        assertThat(argList).containsExactly(2);
    }

    @Test
    void testCustomWhereColumnForNextNestedQuery() {
        Field field = initField("user");

        UserQuery userQuery = UserQuery.builder().usernameLike("test").userLevel(UserLevel.普通).build();
        String sql = FieldProcessor.execute(field, argList, userQuery);

        String expected = "id IN (SELECT permId FROM t_role_and_perm WHERE roleId IN " +
                "(SELECT roleId FROM t_user_and_role WHERE userId IN " +
                "(SELECT id FROM t_user WHERE username LIKE ? AND userLevel = ?)))";
        assertEquals(expected, sql);
        assertThat(argList).containsExactly("%test%", UserLevel.普通.ordinal());
    }

    @Test
    void testNestedQueryOnFieldWithOr() {
        Field field = initField("roleCodeLikeOrRoleNameLike");

        String sql = FieldProcessor.execute(field, argList, "test");

        String expected = "id IN (SELECT permId FROM t_role_and_perm WHERE roleId IN " +
                "(SELECT id FROM t_role WHERE (roleCode LIKE ? OR roleName LIKE ?)))";
        assertEquals(expected, sql);
        assertThat(argList).containsExactly("%test%", "%test%");
    }
}