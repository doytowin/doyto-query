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

package win.doyto.query.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AggregationPrefixTest
 *
 * @author f0rb on 2022-01-26
 */
class AggregationPrefixTest {

    @Test
    void resolveField() {
        AggregationPrefix prefix = AggregationPrefix.resolveField("maxQty");
        assertThat(prefix).isEqualTo(AggregationPrefix.max);
    }

    @Test
    void fixWhenLengthOfFieldNameIsLessThanFour() {
        String fieldName = "qty";
        String resolved = AggregationPrefix.resolveField(fieldName).resolveColumnName(fieldName);
        assertThat(resolved).isEqualTo(fieldName);
    }

    @Test
    void shouldOnlyMatchPrefixWithTrailingCapitalLetter() {
        AggregationPrefix prefix = AggregationPrefix.resolveField("minute");
        assertThat(prefix).isEqualTo(AggregationPrefix.NONE);
    }

    @Test
    void fixShouldIgnoreWhenFieldEndsWithCount() {
        String fieldName = "discount";
        String resolved = AggregationPrefix.resolveField(fieldName).resolveColumnName(fieldName);
        assertThat(resolved).isEqualTo(fieldName);
    }

}