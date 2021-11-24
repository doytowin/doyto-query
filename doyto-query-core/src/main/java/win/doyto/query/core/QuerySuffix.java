package win.doyto.query.core;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import win.doyto.query.annotation.Enumerated;
import win.doyto.query.util.ColumnUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.persistence.EnumType;

import static win.doyto.query.core.Constant.SEPARATOR;
import static win.doyto.query.core.Constant.SPACE;

/**
 * QuerySuffix
 *
 * @author f0rb
 */
@SuppressWarnings("java:S115")
@Getter
@Slf4j
enum QuerySuffix {
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

    private static final Pattern SUFFIX_PTN;

    static {
        List<String> suffixList = Arrays.stream(values()).filter(querySuffix -> querySuffix != NONE).map(Enum::name).collect(Collectors.toList());
        String suffixPtn = StringUtils.join(suffixList, "|");
        SUFFIX_PTN = Pattern.compile("(" + suffixPtn + ")$");
    }

    private final String op;
    private final ValueProcessor valueProcessor;

    QuerySuffix(String op) {
        this(op, ValueProcessor.PLACE_HOLDER);
    }

    QuerySuffix(String op, ValueProcessor valueProcessor) {
        this.op = op;
        this.valueProcessor = valueProcessor;
    }

    static QuerySuffix resolve(String fieldName) {
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
        String andSql = Arrays.stream(CommonUtil.splitByOr(fieldNameWithOr))
                              .map(fieldName -> buildConditionForField(alias + fieldName, argList, value))
                              .collect(Collectors.joining(Constant.SPACE_OR));
        return CommonUtil.wrapWithParenthesis(andSql);
    }

    static String buildConditionForField(String fieldName, List<Object> argList, Object value) {
        QuerySuffix querySuffix = resolve(fieldName);
        value = querySuffix.valueProcessor.escapeValue(value);
        String columnName = querySuffix.resolveColumnName(fieldName);
        columnName = ColumnUtil.convertColumn(columnName);
        return querySuffix.buildColumnCondition(columnName, argList, value);
    }

    String resolveColumnName(String fieldName) {
        String suffix = this.name();
        return fieldName.endsWith(suffix) ? fieldName.substring(0, fieldName.length() - suffix.length()) : fieldName;
    }

    private String buildColumnCondition(String columnName, List<Object> argList, Object value) {
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

    @SuppressWarnings("unchecked")
    private static void appendArg(List<Object> argList, Object value, String placeHolderEx) {
        if (value instanceof Collection) {
            appendCollectionArg(argList, (Collection<Object>) value);
        } else if (placeHolderEx.contains(Constant.PLACE_HOLDER)) {
            appendSingleArg(argList, value);
        }
    }

    private static void appendSingleArg(List<Object> argList, Object value) {
        argList.add(value);
    }

    private static void appendCollectionArg(List<Object> argList, Collection<Object> collection) {
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

    private static void appendEnumCollectionArg(List<Object> argList, Collection<Object> collection, Object instance) {
        Enumerated enumerated = instance.getClass().getAnnotation(Enumerated.class);
        boolean enumToString = enumerated != null && enumerated.value() == EnumType.STRING;
        Function<Enum<?>, ?> enumMapper = enumToString ? Enum::toString : Enum::ordinal;
        collection.stream().map(element -> enumMapper.apply((Enum<?>) element)).forEach(argList::add);
    }

    private static void appendCommonCollectionArg(List<Object> argList, Collection<Object> collection) {
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
            String placeHolders = IntStream.range(0, size).mapToObj(i -> Constant.PLACE_HOLDER).collect(Collectors.joining(SEPARATOR));
            return CommonUtil.wrapWithParenthesis(StringUtils.trimToNull(placeHolders));
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
