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

package win.doyto.query.core;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import win.doyto.query.geo.Box;
import win.doyto.query.geo.Circle;
import win.doyto.query.geo.Near;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * QuerySuffix
 *
 * @author f0rb on 2019-05-16
 * @since 0.0.1
 */
@SuppressWarnings("java:S115")
@Slf4j
@AllArgsConstructor
public enum QuerySuffix {
    Not,
    NotLike(Constants.LIKE_PREDICATE),
    Like(Constants.LIKE_PREDICATE),
    Contain(Constants.LIKE_PREDICATE),
    Start(Constants.LIKE_PREDICATE),
    End(Constants.LIKE_PREDICATE),
    NotIn(new NotInPredicate()),
    In(new InPredicate()),
    NotNull,
    Null,
    Gt,
    Ge,
    Lt,
    Le,
    Eq,
    Any,
    All,

    Exists, // for MongoDB

    Near(Near.class::isInstance),
    NearSphere(Near.class::isInstance),
    Center(Circle.class::isInstance),
    CenterSphere(Circle.class::isInstance),
    Box(Box.class::isInstance),
    // short for Polygon
    Py(o -> o instanceof Collection && ((Collection<?>) o).size() >= 3),
    Within,
    // short for Intersects
    IntX,

    NONE;

    private static final Pattern SUFFIX_PTN = Pattern.compile(
            Arrays.stream(values())
                  .filter(querySuffix -> querySuffix != NONE)
                  .map(Enum::name)
                  .collect(Collectors.joining("|", "(", ")$")));

    QuerySuffix() {
        this(c -> true);
    }

    private final Predicate<Object> typeValidator;

    public static QuerySuffix resolve(String fieldName) {
        Matcher matcher = SUFFIX_PTN.matcher(fieldName);
        return matcher.find() ? valueOf(matcher.group()) : NONE;
    }

    public static boolean isValidValue(Object value, Field field) {
        return !(value == null
                || (value instanceof Boolean && field.getType().isPrimitive() && Boolean.FALSE.equals(value))
                || (resolve(field.getName()).shouldIgnore(value))
        );
    }

    public String resolveColumnName(String fieldName) {
        String suffix = this.name();
        return fieldName.endsWith(suffix) ? fieldName.substring(0, fieldName.length() - suffix.length()) : fieldName;
    }

    public boolean shouldIgnore(Object value) {
        return !typeValidator.test(value);
    }

    static class InPredicate implements Predicate<Object> {
        @Override
        public boolean test(Object o) {
            if (o instanceof Collection || o instanceof DoytoQuery) {
                return true;
            }
            log.warn("Type of field which ends with In/NotIn should be Collection.");
            return false;
        }
    }

    static class NotInPredicate extends InPredicate {
        @Override
        public boolean test(Object o) {
            return super.test(o) && !((Collection<?>) o).isEmpty();
        }
    }

    @SuppressWarnings("java:S1214")
    private interface Constants {
        Predicate<Object> LIKE_PREDICATE = value -> {
            if (value instanceof String) {
                return !((String) value).trim().isEmpty();
            }
            log.warn("Type of field which ends with Like should be String.");
            return false;
        };
    }
}
