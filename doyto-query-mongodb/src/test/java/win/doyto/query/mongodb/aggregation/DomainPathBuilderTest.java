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

package win.doyto.query.mongodb.aggregation;

import org.bson.BsonDocument;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.Test;
import win.doyto.query.core.PageQuery;
import win.doyto.query.mongodb.test.menu.MenuView;
import win.doyto.query.mongodb.test.perm.PermView;
import win.doyto.query.mongodb.test.role.RoleView;
import win.doyto.query.mongodb.test.user.UserView;
import win.doyto.query.test.menu.MenuQuery;
import win.doyto.query.test.perm.PermissionQuery;
import win.doyto.query.test.role.RoleQuery;
import win.doyto.query.test.user.UserQuery;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static win.doyto.query.mongodb.aggregation.DomainPathBuilder.buildLookUpForSubDomain;
import static win.doyto.query.mongodb.test.TestUtil.readString;

/**
 * DomainPathBuilderTest
 *
 * @author f0rb on 2022-05-19
 */
class DomainPathBuilderTest {

    @Test
    void buildDocForSubDomainWithOneJointAndQuery() throws NoSuchFieldException {
        Field field = UserView.class.getDeclaredField("roles");
        RoleQuery roleQuery = RoleQuery.builder().valid(true).build();

        Bson bson = buildLookUpForSubDomain(roleQuery, RoleView.class, field);

        BsonDocument result = bson.toBsonDocument();
        BsonDocument expected = BsonDocument.parse(readString("/query_roles_in_user.json"));
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void buildDocForSubDomainWithTwoJointsAndQuery() throws NoSuchFieldException {
        Field field = UserView.class.getDeclaredField("perms");
        PermissionQuery permissionQuery = PermissionQuery.builder().valid(true).build();

        Bson bson = buildLookUpForSubDomain(permissionQuery, PermView.class, field);

        BsonDocument result = bson.toBsonDocument();
        BsonDocument expected = BsonDocument.parse(readString("/query_perms_in_user.json"));
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void buildDocForSubDomainWithThreeJointsAndQuery() throws NoSuchFieldException {
        Field field = UserView.class.getDeclaredField("menus");
        MenuQuery menuQuery = MenuQuery.builder().valid(true).build();

        Bson bson = buildLookUpForSubDomain(menuQuery, MenuView.class, field);

        BsonDocument result = bson.toBsonDocument();
        BsonDocument expected = BsonDocument.parse(readString("/query_menus_in_user.json"));
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void buildDocForManyToOne() throws NoSuchFieldException {
        Field field = UserView.class.getDeclaredField("createUser");

        Bson bson = buildLookUpForSubDomain(new PageQuery(), UserView.class, field);

        BsonDocument result = bson.toBsonDocument();
        BsonDocument expected = BsonDocument.parse(readString("/query_create_user_in_user.json"));
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void buildDocForOneToMany() throws NoSuchFieldException {
        Field field = UserView.class.getDeclaredField("createdUsers");

        Bson bson = buildLookUpForSubDomain(new PageQuery(), UserView.class, field);

        BsonDocument result = bson.toBsonDocument();
        BsonDocument expected = BsonDocument.parse(readString("/query_created_users_in_user.json"));
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void buildDocForSubDomainReverseWithThreeJointsAndQuery() throws NoSuchFieldException {
        Field field = PermView.class.getDeclaredField("users");
        UserQuery userQuery = UserQuery.builder().id(1).build();

        Bson bson = buildLookUpForSubDomain(userQuery, UserView.class, field);

        BsonDocument result = bson.toBsonDocument();
        BsonDocument expected = BsonDocument.parse(readString("/query_users_in_perm.json"));
        assertThat(result).isEqualTo(expected);
    }
}
