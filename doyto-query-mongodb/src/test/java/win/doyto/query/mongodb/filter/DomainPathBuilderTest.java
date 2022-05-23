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

package win.doyto.query.mongodb.filter;

import lombok.SneakyThrows;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.Test;
import org.springframework.util.StreamUtils;
import win.doyto.query.core.PageQuery;
import win.doyto.query.mongodb.test.join.MenuEntity;
import win.doyto.query.mongodb.test.join.PermEntity;
import win.doyto.query.mongodb.test.join.RoleEntity;
import win.doyto.query.mongodb.test.join.UserEntity;
import win.doyto.query.test.MenuQuery;
import win.doyto.query.test.PermissionQuery;
import win.doyto.query.test.UserQuery;
import win.doyto.query.test.role.RoleQuery;

import java.lang.reflect.Field;
import java.nio.charset.Charset;

import static org.assertj.core.api.Assertions.assertThat;
import static win.doyto.query.mongodb.filter.DomainPathBuilder.buildLookUpForSubDomain;

/**
 * DomainPathBuilderTest
 *
 * @author f0rb on 2022-05-19
 */
class DomainPathBuilderTest {

    @SneakyThrows
    private static String readString(String name) {
        return StreamUtils.copyToString(DomainPathBuilderTest.class.getResourceAsStream(name), Charset.defaultCharset());
    }

    @Test
    void buildDocForSubDomainWithOneJointAndQuery() throws NoSuchFieldException {
        Field field = UserEntity.class.getDeclaredField("roles");
        RoleQuery roleQuery = RoleQuery.builder().valid(true).build();

        Bson bson = buildLookUpForSubDomain(roleQuery, RoleEntity.class, field);

        BsonDocument result = bson.toBsonDocument();
        BsonDocument expected = BsonDocument.parse(readString("/query_user_with_roles.json"));
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void buildDocForSubDomainWithTwoJointsAndQuery() throws NoSuchFieldException {
        Field field = UserEntity.class.getDeclaredField("perms");
        PermissionQuery permissionQuery = PermissionQuery.builder().valid(true).build();

        Bson bson = buildLookUpForSubDomain(permissionQuery, PermEntity.class, field);

        BsonDocument result = bson.toBsonDocument();
        BsonDocument expected = BsonDocument.parse(readString("/query_user_with_perms.json"));
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void buildDocForSubDomainWithThreeJointsAndQuery() throws NoSuchFieldException {
        Field field = UserEntity.class.getDeclaredField("menus");
        MenuQuery menuQuery = MenuQuery.builder().valid(true).build();

        Bson bson = buildLookUpForSubDomain(menuQuery, MenuEntity.class, field);

        BsonDocument result = bson.toBsonDocument();
        BsonDocument expected = BsonDocument.parse(readString("/query_user_with_menus.json"));
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void buildDocForManyToOne() throws NoSuchFieldException {
        Field field = UserEntity.class.getDeclaredField("createUser");

        Bson bson = buildLookUpForSubDomain(new PageQuery(), UserEntity.class, field);

        BsonDocument result = bson.toBsonDocument();
        BsonDocument expected = BsonDocument.parse(readString("/query_user_with_create_user.json"));
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void buildDocForOneToMany() throws NoSuchFieldException {
        Field field = UserEntity.class.getDeclaredField("createdUsers");

        Bson bson = buildLookUpForSubDomain(new PageQuery(), UserEntity.class, field);

        BsonDocument result = bson.toBsonDocument();
        BsonDocument expected = BsonDocument.parse(readString("/query_user_with_created_users.json"));
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void buildDocForSubDomainReverseWithThreeJointsAndQuery() throws NoSuchFieldException {
        Field field = PermEntity.class.getDeclaredField("users");
        UserQuery userQuery = UserQuery.builder().id(1).build();

        Bson bson = buildLookUpForSubDomain(userQuery, UserEntity.class, field);

        BsonDocument result = bson.toBsonDocument();
        BsonDocument expected = BsonDocument.parse(readString("/query_perm_with_users.json"));
        assertThat(result).isEqualTo(expected);
    }
}
