/*
 * Copyright © 2019-2025 DoytoWin, Inc.
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
import win.doyto.query.config.GlobalConfiguration;

import java.lang.reflect.Field;
import java.util.List;
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
                ),
                Arguments.of(
                        new String[]{"menu", "menu<-parent_id"},
                        new String[]{"menu", "menu"},
                        new String[]{"parent_id", "id"},
                        new String[]{"t_menu"}
                ),
                Arguments.of(
                        new String[]{"menu->parent_id", "menu"},
                        new String[]{"menu", "menu"},
                        new String[]{"id", "parent_id"},
                        new String[]{"t_menu"}
                )
        );
    }

    @ParameterizedTest
    @MethodSource("domainPathProvider")
    void shouldResolveDomainPath(String[] originDomainPath, String[] domainPath, String[] joinIds, String[] joinTables) {
        DomainPathDetail domainPathDetail = DomainPathDetail.buildBy(originDomainPath, "id", "id", s -> s);
        assertThat(domainPathDetail.getDomainPath()).isEqualTo(domainPath);
        List<Relation> relations = domainPathDetail.getRelations();
        for (int i = 0; i < relations.size(); i++) {
            Relation relation = relations.get(i);
            assertThat(relation.getAssociativeTable()).isEqualTo(joinTables[i]);
            assertThat(relation.getFk1()).isEqualTo(joinIds[i]);
            assertThat(relation.getFk2()).isEqualTo(joinIds[i + 1]);
        }
    }

    static Stream<Arguments> domainPathProvider2() {
        GlobalConfiguration.registerJoinTable("product", "orders", "a_orders_and_product");
        return Stream.of(
                Arguments.of(
                        new String[]{"user", "orders<-user_id", "product"},
                        new String[]{"user", "orders", "product"},
                        new String[][]{
                                new String[]{"user_id", "t_orders", "id"},
                                new String[]{"orders_id", "a_orders_and_product", "product_id"}
                        }
                ),
                Arguments.of(
                        new String[]{"product", "orders->user_id", "user"},
                        new String[]{"product", "orders", "user"},
                        new String[][]{
                                new String[]{"product_id", "a_orders_and_product", "orders_id"},
                                new String[]{"id", "t_orders", "user_id"},
                        }
                )
        );
    }
    @ParameterizedTest
    @MethodSource("domainPathProvider2")
    void shouldResolveDomainPath2(String[] originDomainPath, String[] domainPath, String[][] expected) {
        DomainPathDetail domainPathDetail = DomainPathDetail.buildBy(originDomainPath, "id", "id", s -> s);
        assertThat(domainPathDetail.getDomainPath()).isEqualTo(domainPath);
        List<Relation> relations = domainPathDetail.getRelations();
        for (int i = 0; i < relations.size(); i++) {
            Relation relation = relations.get(i);
            assertThat(relation.getFk1()).isEqualTo(expected[i][0]);
            assertThat(relation.getAssociativeTable()).isEqualTo(expected[i][1]);
            assertThat(relation.getFk2()).isEqualTo(expected[i][2]);
        }
    }

    @Test
    void shouldConvertFieldByFunc() throws NoSuchFieldException {
        Field field = UserView.class.getDeclaredField("createRoles");
        DomainPath domainPathAnno = field.getAnnotation(DomainPath.class);

        DomainPathDetail domainPathDetail = DomainPathDetail.buildBy(domainPathAnno, s -> s.equals("id") ? "_id" : s);
        Relation baseRelation = domainPathDetail.getBaseRelation();
        assertThat(baseRelation.getFk1()).isEqualTo("create_user_id");
        assertThat(baseRelation.getFk2()).isEqualTo("_id");
    }

}