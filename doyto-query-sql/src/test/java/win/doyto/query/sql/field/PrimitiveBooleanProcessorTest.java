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

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class PrimitiveBooleanProcessorTest {

    @Test
    void shouldReturnConditionWhenValueIsTrueForNullField() {
        PrimitiveBooleanProcessor ltPbp = new PrimitiveBooleanProcessor("memoNull");
        String condition = ltPbp.process("", new ArrayList<>(), true);
        assertThat(condition).isEqualTo("memo IS NULL");
    }

    @Test
    void shouldReturnNullWhenValueIsFalseForNotNullField() {
        PrimitiveBooleanProcessor ltPbp = new PrimitiveBooleanProcessor("memoNotNull");
        String condition = ltPbp.process("", new ArrayList<>(), true);
        assertThat(condition).isEqualTo("memo IS NOT NULL");

        assertThat(ltPbp.process("", new ArrayList<>(), false)).isNull();
    }

    @Test
    void supportColumnComparison() {
        PrimitiveBooleanProcessor ltPbp = new PrimitiveBooleanProcessor("commitDateLtReceiptDate");
        String condition = ltPbp.process("", new ArrayList<>(), true);
        assertThat(condition).isEqualTo("commit_date < receipt_date");
    }

    @Test
    void supportColumnComparisonWithAlias() {
        PrimitiveBooleanProcessor ltPbp = new PrimitiveBooleanProcessor("alias$commitDateLtAlias$receiptDate");
        String condition = ltPbp.process("o.", new ArrayList<>(), true);
        assertThat(condition).isEqualTo("o.commit_date < o.receipt_date");
    }

}