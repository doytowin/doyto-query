/*
 * Copyright © 2019-2023 Forb Yuan
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

import org.junit.jupiter.api.Test;
import win.doyto.query.test.user.UserEntity;
import win.doyto.query.test.user.UserQuery;
import win.doyto.query.test.user.UserQueryBuilder;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SqlBuilderFactoryTest
 *
 * @author f0rb on 2023/1/29
 * @since 1.0.1
 */
class SqlBuilderFactoryTest {

    @Test
    void shouldReturnCustomQueryBuilderIfFound() {
        SqlBuilder<UserEntity> sqlBuilder = SqlBuilderFactory.create(UserEntity.class);
        assertThat(sqlBuilder).isInstanceOf(UserQueryBuilder.class);

        UserQuery userQuery = UserQuery.builder().id(10)
                                       .idIn(Arrays.asList())
                                       .mobile("17718222222")
                                       .memoNull(true)
                                       .build();
        SqlAndArgs sqlAndArgs = sqlBuilder.buildSelectIdAndArgs(userQuery);

        SqlBuilder<UserEntity> baseBuilder = new CrudBuilder<>(UserEntity.class);
        SqlAndArgs expectedSql = baseBuilder.buildSelectIdAndArgs(userQuery);

        assertThat(sqlAndArgs.getSql()).isEqualTo(expectedSql.getSql());
        assertThat(sqlAndArgs.getArgs()).containsExactly(expectedSql.getArgs());
    }

}