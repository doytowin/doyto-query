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

package win.doyto.query.sql;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import win.doyto.query.util.ColumnUtil;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static win.doyto.query.sql.Constant.*;

/**
 * CompoundOperatorsSuffix
 *
 * @author f0rb on 2023/1/11
 * @since 1.0.1
 */
@SuppressWarnings("java:S115")
@AllArgsConstructor
enum CompoundOperatorsSuffix {
    Ae(" += "),
    Se(" -= "),
    Me(" *= "),
    NONE(" = ");

    private static final Pattern SUFFIX_PTN = Pattern.compile(
            Arrays.stream(values())
                  .filter(suffix -> suffix != NONE)
                  .map(Enum::name)
                  .collect(Collectors.joining("|", OP, CP + "$")));

    private final String op;

    static CompoundOperatorsSuffix resolve(String fieldName) {
        Matcher matcher = SUFFIX_PTN.matcher(fieldName);
        return matcher.find() ? valueOf(matcher.group()) : NONE;
    }

    static String mapField(String fieldName) {
        CompoundOperatorsSuffix suffix = resolve(fieldName);
        String column = ColumnUtil.convertColumn(StringUtils.removeEnd(fieldName, suffix.name()));
        return column + suffix.op + PLACE_HOLDER;
    }
}
