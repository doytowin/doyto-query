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

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import win.doyto.query.annotation.EnumType;
import win.doyto.query.annotation.Enumerated;
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.sql.BuildHelper;
import win.doyto.query.util.CommonUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static win.doyto.query.sql.Constant.*;

/**
 * SqlQuerySuffix
 *
 * @author f0rb on 2021-12-01
 * @since 0.3.0
 */
@SuppressWarnings("java:S115")
@Getter
@Slf4j
public enum SqlQuerySuffix {
    Not("!="),
    Ne("<>"),
    NotLike(NOT_LIKE, ValueProcessor.LIKE_VALUE_PROCESSOR),
    Like(LIKE, ValueProcessor.LIKE_VALUE_PROCESSOR),
    NotContain(NOT_LIKE, ValueProcessor.CONTAIN_VALUE_PROCESSOR),
    Contain(LIKE, ValueProcessor.CONTAIN_VALUE_PROCESSOR),
    NotStart(NOT_LIKE, new ContainValueProcessor() {
        @Override
        public Object escapeValue(Object value) {
            return CommonUtil.escapeStart(String.valueOf(value));
        }
    }),
    Start(LIKE, new ContainValueProcessor() {
        @Override
        public Object escapeValue(Object value) {
            return CommonUtil.escapeStart(String.valueOf(value));
        }
    }),
    NotEnd(NOT_LIKE, new ContainValueProcessor() {
        @Override
        public Object escapeValue(Object value) {
            return CommonUtil.escapeEnd(String.valueOf(value));
        }
    }),
    End(LIKE, new ContainValueProcessor() {
        @Override
        public Object escapeValue(Object value) {
            return CommonUtil.escapeEnd(String.valueOf(value));
        }
    }),
    NotIn("NOT IN", new InValueProcessor() {
        @Override
        public boolean shouldIgnore(Object value) {
            return super.shouldIgnore(value) || ((Collection<?>) value).isEmpty();
        }
    }),
    In("IN", new InValueProcessor()),
    NotNull("IS NOT NULL", ValueProcessor.EMPTY_PROCESSOR),
    Null("IS NULL", ValueProcessor.EMPTY_PROCESSOR),
    Gt(">"),
    Ge(">="),
    Lt("<"),
    Le("<="),
    Eq("="),
    Any("ANY"),
    All("ALL"),
    NONE("=");

    private static final Pattern SUFFIX_PTN = Pattern.compile(
            Arrays.stream(values())
                  .filter(querySuffix -> querySuffix != NONE)
                  .map(Enum::name)
                  .collect(Collectors.joining("|", OP, CP + "$")));

    private final String op;
    private final ValueProcessor valueProcessor;

    SqlQuerySuffix(String op) {
        this(op, ValueProcessor.PLACE_HOLDER_PROCESSOR);
    }

    SqlQuerySuffix(String op, ValueProcessor valueProcessor) {
        this.op = op;
        this.valueProcessor = valueProcessor;
    }

    static SqlQuerySuffix resolve(String fieldName) {
        Matcher matcher = SUFFIX_PTN.matcher(fieldName);
        return matcher.find() ? valueOf(matcher.group()) : NONE;
    }

    public String removeSuffix(String fieldName) {
        return StringUtils.removeEnd(fieldName, this.name());
    }

    public String buildColumnCondition(String columnName, List<Object> argList, Object value) {
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
        } else if (placeHolderEx.contains(PLACE_HOLDER)) {
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
        ValueProcessor PLACE_HOLDER_PROCESSOR = value -> PLACE_HOLDER;
        ValueProcessor EMPTY_PROCESSOR = value -> EMPTY;
        ValueProcessor LIKE_VALUE_PROCESSOR = new LikeValueProcessor();
        ValueProcessor CONTAIN_VALUE_PROCESSOR = new ContainValueProcessor();

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
            return size == 0 ? "(null)" : BuildHelper.buildPlaceHolders(size);
        }
    }

    private static class LikeValueProcessor implements ValueProcessor {
        @Override
        public String getPlaceHolderEx(Object value) {
            if (StringUtils.contains(value.toString(), "\\")) {
                return PLACE_HOLDER + " ESCAPE '\\'";
            }
            return PLACE_HOLDER;
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
            String like = String.valueOf(value);
            if (GlobalConfiguration.instance().getWildcardPtn().matcher(like).find()) {
                return like;
            }
            return CommonUtil.escapeLike(like);
        }
    }

    private static class ContainValueProcessor extends LikeValueProcessor {
        @Override
        public Object escapeValue(Object value) {
            return CommonUtil.escapeLike(String.valueOf(value));
        }
    }

}
