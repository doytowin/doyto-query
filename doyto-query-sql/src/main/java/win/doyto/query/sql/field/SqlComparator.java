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

package win.doyto.query.sql.field;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import win.doyto.query.util.ColumnUtil;
import win.doyto.query.util.CommonUtil;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * SqlComparator
 *
 * @author f0rb on 2023/2/18
 * @since 1.0.1
 */
@SuppressWarnings("java:S115")
@Slf4j
@AllArgsConstructor
enum SqlComparator {
    Ne(" != "),
    Gt(" > "),
    Ge(" >= "),
    Lt(" < "),
    Le(" <= "),
    Eq(" = ");

    private static final Pattern CONDITION_PTN;

    static {
        String ptn = Arrays.stream(values())
                           .map(Enum::name)
                           .collect(Collectors.joining("|", "(\\w+)(", ")(\\w+)"));
        CONDITION_PTN = Pattern.compile(ptn);
    }

    private final String op;

    static String buildClause(String fieldName) {
        Matcher matcher = CONDITION_PTN.matcher(fieldName);
        if (!matcher.find()) {
            return null;
        }
        String c1 = ColumnUtil.convertColumn(matcher.group(1));
        String op = valueOf(matcher.group(2)).op;
        String c2 = ColumnUtil.convertColumn(CommonUtil.camelize(matcher.group(3)));
        return c1 + op + c2;
    }

}
