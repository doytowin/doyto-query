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

package win.doyto.query.core;

import lombok.Getter;

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
    stdDevPop("stddev_pop"),
    stdDevSamp("stddev_samp"),
    stdDev("stddev"),
    addToSet,
    push,
    count {
        @Override
        public String resolveColumnName(String viewFieldName) {
            return viewFieldName.equals(name()) ? "*" : super.resolveColumnName(viewFieldName);
        }
    },

    NONE(0);

    private static final Pattern PREFIX_PTN = Pattern.compile(
            Arrays.stream(values())
                  .map(Enum::name)
                  .collect(Collectors.joining("|", "^\\b(", ")(?=[A-Z])|\\bcount"))
    );

    private final int prefixLength;

    @Getter
    private final String name;

    AggregationPrefix() {
        this.name = name();
        this.prefixLength = name().length();
    }

    AggregationPrefix(String name) {
        this.name = name;
        this.prefixLength = name().length();
    }

    AggregationPrefix(int prefixLength) {
        this.name = name();
        this.prefixLength = prefixLength;
    }

    public static AggregationPrefix resolveField(String fieldName) {
        Matcher matcher = PREFIX_PTN.matcher(fieldName);
        return matcher.find() ? valueOf(matcher.group()) : NONE;
    }

    public String resolveColumnName(String viewFieldName) {
        return viewFieldName.substring(prefixLength);
    }

}
