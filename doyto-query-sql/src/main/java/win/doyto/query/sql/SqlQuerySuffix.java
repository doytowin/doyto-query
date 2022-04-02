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

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import win.doyto.query.annotation.Enumerated;
import win.doyto.query.util.ColumnUtil;
import win.doyto.query.util.CommonUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.persistence.EnumType;

import static win.doyto.query.sql.Constant.SPACE;

/**
 * QuerySuffix
 *
 * @author f0rb
 */
@SuppressWarnings("java:S115")
@Getter
@Slf4j
enum SqlQuerySuffix {
    Not("!="),
    NotLike("NOT LIKE", ValueProcessor.LIKE_VALUE_PROCESSOR),
    Like("LIKE", ValueProcessor.LIKE_VALUE_PROCESSOR),
    Contain("LIKE", ValueProcessor.LIKE_VALUE_PROCESSOR),
    Start("LIKE", new LikeValueProcessor() {
        @Override
        public Object escapeValue(Object value) {
            return CommonUtil.escapeStart(String.valueOf(value));
        }
    }),
    NotIn("NOT IN", new InValueProcessor() {
        @Override
        public boolean shouldIgnore(Object value) {
            return super.shouldIgnore(value) || ((Collection<?>) value).isEmpty();
        }
    }),
    In("IN", new InValueProcessor()),
    NotNull("IS NOT NULL", ValueProcessor.EMPTY),
    Null("IS NULL", ValueProcessor.EMPTY),
    Gt(">"),
    Ge(">="),
    Lt("<"),
    Le("<="),
    Eq("="),
    NONE("=");

    private static final Pattern SUFFIX_PTN = Pattern.compile(
            Arrays.stream(values())
                  .filter(querySuffix -> querySuffix != NONE)
                  .map(Enum::name)
                  .collect(Collectors.joining("|", "(", ")$")));

    private final String op;
    private final ValueProcessor valueProcessor;

    SqlQuerySuffix(String op) {
        this(op, ValueProcessor.PLACE_HOLDER);
    }

    SqlQuerySuffix(String op, ValueProcessor valueProcessor) {
        this.op = op;
        this.valueProcessor = valueProcessor;
    }

    static SqlQuerySuffix resolve(String fieldName) {
        Matcher matcher = SUFFIX_PTN.matcher(fieldName);
        return matcher.find() ? valueOf(matcher.group()) : NONE;
    }

    static String buildConditionForFieldContainsOr(String fieldNameWithOr, List<Object> argList, Object value) {
        final String alias;
        int indexOfDot = fieldNameWithOr.indexOf('.') + 1;
        if (indexOfDot > 0) {
            alias = fieldNameWithOr.substring(0, indexOfDot);
            fieldNameWithOr = fieldNameWithOr.substring(indexOfDot);
        } else {
            alias = "";
        }
        return Arrays.stream(CommonUtil.splitByOr(fieldNameWithOr))
                     .map(fieldName -> buildConditionForField(alias + fieldName, argList, value))
                     .collect(Collectors.joining(Constant.SPACE_OR, "(", ")"));
    }

    static String buildConditionForField(String fieldName, List<Object> argList, Object value) {
        SqlQuerySuffix sqlQuerySuffix = resolve(fieldName);
        value = sqlQuerySuffix.valueProcessor.escapeValue(value);
        String columnName = sqlQuerySuffix.resolveColumnName(fieldName);
        columnName = ColumnUtil.convertColumn(columnName);
        return sqlQuerySuffix.buildColumnCondition(columnName, argList, value);
    }

    String resolveColumnName(String fieldName) {
        String suffix = this.name();
        return fieldName.endsWith(suffix) ? fieldName.substring(0, fieldName.length() - suffix.length()) : fieldName;
    }

    String buildColumnCondition(String columnName, List<Object> argList, Object value) {
        if (shouldIgnore(value)) {
            return null;
        }
        String placeHolderEx = valueProcessor.getPlaceHolderEx(value);
        appendArg(argList, value, placeHolderEx);
        return buildColumnClause(columnName, placeHolderEx);
    }

    public boolean shouldIgnore(Object value) {
        return valueProcessor.shouldIgnore(value);
    }

    private String buildColumnClause(String columnName, String placeHolderEx) {
        if (!placeHolderEx.isEmpty()) {
            placeHolderEx = SPACE + placeHolderEx;
        }
        return columnName + SPACE + getOp() + placeHolderEx;
    }

    private static void appendArg(List<Object> argList, Object value, String placeHolderEx) {
        if (value instanceof Collection) {
            appendCollectionArg(argList, (Collection<?>) value);
        } else if (placeHolderEx.contains(Constant.PLACE_HOLDER)) {
            appendSingleArg(argList, value);
        }
    }

    private static void appendSingleArg(List<Object> argList, Object value) {
        argList.add(value);
    }

    private static void appendCollectionArg(List<Object> argList, Collection<?> collection) {
        if (collection.isEmpty()) {
            return;
        }
        Object next = collection.iterator().next();
        if (next instanceof Enum<?>) {
            appendEnumCollectionArg(argList, collection, next);
        } else {
            appendCommonCollectionArg(argList, collection);
        }
    }

    private static void appendEnumCollectionArg(List<Object> argList, Collection<?> collection, Object instance) {
        Enumerated enumerated = instance.getClass().getAnnotation(Enumerated.class);
        boolean enumToString = enumerated != null && enumerated.value() == EnumType.STRING;
        Function<Enum<?>, ?> enumMapper = enumToString ? Enum::toString : Enum::ordinal;
        collection.stream().map(element -> enumMapper.apply((Enum<?>) element)).forEach(argList::add);
    }

    private static void appendCommonCollectionArg(List<Object> argList, Collection<?> collection) {
        argList.addAll(collection);
    }

    @SuppressWarnings("java:S1214")
    interface ValueProcessor {
        ValueProcessor PLACE_HOLDER = value -> Constant.PLACE_HOLDER;
        ValueProcessor EMPTY = value -> Constant.EMPTY;
        ValueProcessor LIKE_VALUE_PROCESSOR = new LikeValueProcessor();

        String getPlaceHolderEx(Object value);

        default Object escapeValue(Object value) {
            return value;
        }

        /**
         * For Like operator
         */
        default boolean shouldIgnore(Object value) {
            return false;
        }
    }

    static class InValueProcessor implements ValueProcessor {
        @Override
        public boolean shouldIgnore(Object value) {
            if (!(value instanceof Collection)) {
                log.warn("Type of field which ends with In/NotIn should be Collection.");
                return true;
            }
            return false;
        }

        @Override
        public String getPlaceHolderEx(Object value) {
            int size = ((Collection<?>) value).size();
            return size == 0 ? "(null)" :
                    IntStream.range(0, size).mapToObj(i -> Constant.PLACE_HOLDER)
                             .collect(CommonUtil.CLT_COMMA_WITH_PAREN);
        }
    }

    private static class LikeValueProcessor implements ValueProcessor {
        @Override
        public String getPlaceHolderEx(Object value) {
            return Constant.PLACE_HOLDER;
        }

        @Override
        public boolean shouldIgnore(Object value) {
            if (!(value instanceof String)) {
                log.warn("Type of field which ends with Like should be String.");
                return true;
            }
            return StringUtils.isBlank((String) value);
        }

        @Override
        public Object escapeValue(Object value) {
            return CommonUtil.escapeLike(String.valueOf(value));
        }
    }

}
