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

package win.doyto.query.mongodb;

import win.doyto.query.util.CommonUtil;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * AggregationPrefix
 *
 * @author f0rb on 2022-01-26
 */
@SuppressWarnings("java:S115")
public enum AggregationPrefix {
    sum,
    max,
    min,
    avg,
    first,
    last,
    stdDevPop,
    stdDevSamp,
    addToSet,
    push,

    NONE(0);

    private static final Pattern SUFFIX_PTN = Pattern.compile(
            Arrays.stream(values())
                  .map(Enum::name)
                  .collect(Collectors.joining("|", "^\\b(", ")(?=[A-Z])"))
    );

    private final int prefixLength;

    AggregationPrefix() {
        this.prefixLength = name().length();
    }

    AggregationPrefix(int prefixLength) {
        this.prefixLength = prefixLength;
    }

    public static AggregationPrefix resolveField(String fieldName) {
        Matcher matcher = SUFFIX_PTN.matcher(fieldName);
        return matcher.find() ? valueOf(matcher.group()) : NONE;
    }

    public String resolveColumnName(String viewFieldName) {
        return CommonUtil.camelize(viewFieldName.substring(prefixLength));
    }

}