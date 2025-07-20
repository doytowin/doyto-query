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

package win.doyto.query.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import win.doyto.query.config.GlobalConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static win.doyto.query.util.ColumnUtil.convertColumn;
import static win.doyto.query.util.CommonUtil.*;

/**
 * CommonUtilTest
 *
 * @author f0rb on 2019-05-31
 */
class CommonUtilTest {

    @Test
    void testEscapeLike() {
        assertNull(escapeLike(null));
        assertEquals("", escapeLike(""));
    }

    @ParameterizedTest
    @CsvSource({
            "'', ''",
            "admin, admin",
            "a%min, a\\%min",
            "a_min, a\\_min",
            "%, \\%",
            "ad%, ad\\%",
            "_, \\_",
            "\\, \\\\",
            "a\\_min, a\\\\\\_min",
    })
    void testEscape(String input, String expect) {
        assertEquals(expect, escape(input));
    }

    @Test
    void testSplitByOr() {
        assertArrayEquals(new String[]{"user", "emailAddress", "order"}, splitByOr("userOrEmailAddressOrOrder"));
    }

    @Test
    void testReplaceHolderInString() {
        assertEquals("_test1_", replaceHolderInString(new PlaceHolderObject("test1"), "_${part1}_"));
    }

    @Test
    void replaceHolderInStringShouldReadGetterFirst() {
        assertEquals("_test1_test2_", replaceHolderInString(new PlaceHolderObject("test1"), "_${part1}_${part2}_"));
    }

    @Getter
    @Setter
    @AllArgsConstructor
    static class PlaceHolderObject {
        private String part1;

        @SuppressWarnings("unused")
        public String getPart2() {
            return "test2";
        }
    }

    @Test
    void fixNPEInReadField() {
        Object noop = readField(new PlaceHolderObject("test1"), "noop");
        assertNull(noop);
    }

    @Test
    void supportMapFieldToUnderscoreOrCamelCaseByToggle() {
        GlobalConfiguration.instance().setMapCamelCaseToUnderscore(false);

        assertThat(convertColumn("userCode"))
                .isEqualTo("userCode");
        assertThat(convertColumn("user_code"))
                .isEqualTo("userCode");

        GlobalConfiguration.instance().setMapCamelCaseToUnderscore(true);

        assertThat(convertColumn("userCode"))
                .isEqualTo("user_code");
        assertThat(convertColumn("user_code"))
                .isEqualTo("user_code");
    }
}