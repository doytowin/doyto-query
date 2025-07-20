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

package win.doyto.query.sql.field;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static win.doyto.query.sql.field.SqlQuerySuffixTest.fieldInTestQuery;

/**
 * OrFieldProcessorTest
 *
 * @author f0rb on 2023/7/10
 * @since 1.0.2
 */
class OrFieldProcessorTest {
    ArrayList<Object> argList = new ArrayList<>();

    @Test
    void buildConditionForFieldContainsOr() {
        OrFieldProcessor orFieldProcessor = new OrFieldProcessor(fieldInTestQuery("usernameOrUserCodeLike"));
        String condition = orFieldProcessor.process("", argList, "test");
        assertEquals("(username = ? OR user_code LIKE ?)", condition);
        assertThat(argList).containsExactly("test", "%test%");
    }

    @Test
    void buildConditionForFieldContainsOrAndAlias() {
        OrFieldProcessor orFieldProcessor = new OrFieldProcessor(fieldInTestQuery("usernameOrUserCodeLike"));
        String condition = orFieldProcessor.process("u.", argList, "test");
        assertEquals("(u.username = ? OR u.user_code LIKE ?)", condition);
        assertThat(argList).containsExactly("test", "%test%");
    }
}