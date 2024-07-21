/*
 * Copyright © 2019-2024 Forb Yuan
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
import win.doyto.query.test.Account;

import java.util.ArrayList;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static win.doyto.query.sql.Constant.OR;
import static win.doyto.query.sql.field.SqlQuerySuffixTest.fieldInTestQuery;

/**
 * OrSuffixTest
 *
 * @author f0rb on 2024/7/15
 */
class OrSuffixTest {
    ArrayList<Object> argList = new ArrayList<>();

    @Test
    void buildOrConditionForFieldEndWithOr() {
        FieldProcessor orSuffixProcessor = new ConnectableFieldProcessor(fieldInTestQuery("accountOr").getType(), OR);
        Account accountOr = Account.builder().username("test").email("test@qq.com").build();
        String condition = orSuffixProcessor.process("", argList, accountOr);
        assertEquals("(username = ? OR email = ?)", condition);
        assertThat(argList).containsExactly("test", "test@qq.com");
    }

    @Test
    void buildOrConditionForNestedAND() {
        FieldProcessor orSuffixProcessor = new ConnectableFieldProcessor(fieldInTestQuery("accountOr").getType(), OR);
        Account account = Account.builder().username("test").mobile("18888888").build();
        Account accountOr = Account.builder().username("test").email("test@qq.com").accountAnd(account).build();
        String condition = orSuffixProcessor.process("", argList, accountOr);
        assertEquals("(username = ? OR email = ? OR (username = ? AND mobile = ?))", condition);
        assertThat(argList).containsExactly("test", "test@qq.com", "test", "18888888");
    }

    @Test
    void buildOrConditionForNestedOR() {
        FieldProcessor orSuffixProcessor = new ConnectableFieldProcessor(fieldInTestQuery("accountOr").getType(), OR);
        Account account = Account.builder().username("test").mobile("18888888").build();
        Account accountOr = Account.builder().username("test").email("test@qq.com").accountOr(account).build();
        String condition = orSuffixProcessor.process("", argList, accountOr);
        assertEquals("(username = ? OR email = ? OR (username = ? OR mobile = ?))", condition);
        assertThat(argList).containsExactly("test", "test@qq.com", "test", "18888888");
    }

    @Test
    void buildOrConditionForListWithCustomType() {
        FieldProcessor orSuffixProcessor = new OrCollectionProcessor(fieldInTestQuery("accountsOr"));
        Account account1 = Account.builder().username("test").mobile("18888888").build();
        Account account2 = Account.builder().username("test").email("test@qq.com").build();
        String condition = orSuffixProcessor.process("", argList, Arrays.asList(account1, account2));
        assertEquals("((username = ? AND mobile = ?) OR (username = ? AND email = ?))", condition);
        assertThat(argList).containsExactly("test", "18888888", "test", "test@qq.com");
    }
}
