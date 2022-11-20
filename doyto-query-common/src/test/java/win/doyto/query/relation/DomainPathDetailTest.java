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

package win.doyto.query.relation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import win.doyto.query.annotation.DomainPath;
import win.doyto.query.test.user.UserView;

import java.lang.reflect.Field;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * DomainPathDetailTest
 *
 * @author f0rb on 2022/11/20
 * @since 1.0.0
 */
class DomainPathDetailTest {
    static Stream<Arguments> domainPathProvider() {
        return Stream.of(
                Arguments.of(
                        new String[]{"user", "role"},
                        new String[]{"user", "role"},
                        new String[]{"user_id", "role_id"},
                        new String[]{"a_user_and_role"}
                ),
                Arguments.of(
                        new String[]{"user", "role", "perm"},
                        new String[]{"user", "role", "perm"},
                        new String[]{"user_id", "role_id", "perm_id"},
                        new String[]{"a_user_and_role", "a_role_and_perm"}
                ),
                Arguments.of(
                        new String[]{"role", "~", "user"},
                        new String[]{"role", "user"},
                        new String[]{"role_id", "user_id"},
                        new String[]{"a_user_and_role"}
                ),
                Arguments.of(
                        new String[]{"perm", "~", "role", "~", "user"},
                        new String[]{"perm", "role", "user"},
                        new String[]{"perm_id", "role_id", "user_id"},
                        new String[]{"a_role_and_perm", "a_user_and_role"}
                )
        );
    }

    @ParameterizedTest
    @MethodSource("domainPathProvider")
    void shouldResolveDomainPath(String[] originDomainPath, String[] domainPath, String[] joinIds, String[] joinTables) {
        DomainPathDetail domainPathDetail = DomainPathDetail.buildBy(originDomainPath, "id", "id", s -> s);
        assertThat(domainPathDetail.getDomainPath()).isEqualTo(domainPath);
        assertThat(domainPathDetail.getJoinIds()).isEqualTo(joinIds);
        assertThat(domainPathDetail.getJoinTables()).isEqualTo(joinTables);
    }

    @Test
    void shouldConvertFieldByFunc() throws NoSuchFieldException {
        Field field = UserView.class.getDeclaredField("createRoles");
        DomainPath domainPathAnno = field.getAnnotation(DomainPath.class);

        DomainPathDetail domainPathDetail = DomainPathDetail.buildBy(domainPathAnno, s -> s.equals("id") ? "_id" : s);
        assertThat(domainPathDetail.getLocalFieldColumn()).isEqualTo("_id");
        assertThat(domainPathDetail.getForeignFieldColumn()).isEqualTo("create_user_id");
        assertThat(domainPathDetail.getJoinIds()[0]).isEqualTo("create_user_id");
        assertThat(domainPathDetail.onlyOneDomain()).isTrue();
    }

}