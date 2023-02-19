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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.sql.field.FieldMapper;
import win.doyto.query.test.TestEntity;
import win.doyto.query.test.TestQuery;
import win.doyto.query.util.ColumnUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * MapCamelCaseToUnderscoreTest
 *
 * @author f0rb on 2023/1/4
 * @since 1.0.1
 */
class MapCamelCaseToUnderscoreTest {
    private final QueryBuilder testQueryBuilder = new QueryBuilder(TestEntity.class);
    private final List<Object> argList = new ArrayList<>();

    @BeforeAll
    static void beforeAll() {
        ColumnUtil.initFields(TestQuery.class, FieldMapper::init);
    }

    @Test
    void supportDisAbleMapFieldToUnderscore() {
        GlobalConfiguration.instance().setMapCamelCaseToUnderscore(false);

        Date date = new Date();
        TestQuery testQuery = TestQuery.builder().usernameOrUserCodeLike("test").createTimeLt(date).build();

        assertEquals("SELECT * FROM t_user t WHERE (username = ? OR userCode LIKE ?) AND createTime < ?",
                     testQueryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).containsExactly("test", "%test%", date);

        GlobalConfiguration.instance().setMapCamelCaseToUnderscore(true);
    }
}
