package win.doyto.query.core;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import win.doyto.query.annotation.Enumerated;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.persistence.EnumType;

import static win.doyto.query.core.Constant.*;

/**
 * QuerySuffix
 *
 * @author f0rb
 */
@SuppressWarnings("java:S115")
@Getter
enum QuerySuffix {
    Not("!="),
    NotLike("NOT LIKE"),
    Start("LIKE"),
    Like("LIKE"),
    NotIn("NOT IN", Ex.COLLECTION),
    In("IN", Ex.COLLECTION),
    NotNull("IS NOT NULL", Ex.EMPTY),
    Null("IS NULL", Ex.EMPTY),
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
    private final Ex ex;

    QuerySuffix(String op) {
        this(op, Ex.REPLACE_HOLDER);
    }

    QuerySuffix(String op, Ex ex) {
        this.op = op;
        this.ex = ex;
    }

    static QuerySuffix resolve(String fieldName) {
        Matcher matcher = SUFFIX_PTN.matcher(fieldName);
        return matcher.find() ? valueOf(matcher.group()) : NONE;
    }

    private static String processOrStatement(List<Object> argList, Object value, String fieldName) {
        final String alias;
        int indexOfDot = fieldName.indexOf('.') + 1;
        if (indexOfDot > 0) {
            alias = fieldName.substring(0, indexOfDot);
            fieldName = fieldName.substring(indexOfDot);
        } else {
            alias = "";
        }
        String andSql = Arrays.stream(CommonUtil.splitByOr(fieldName))
                              .map(s -> buildAndSql(argList, value, alias + s))
                              .collect(Collectors.joining(Constant.SPACE_OR));
        return CommonUtil.wrapWithParenthesis(andSql);
    }

    static String buildAndSql(List<Object> argList, Object value, String fieldName) {
        if (CommonUtil.containsOr(fieldName)) {
            return processOrStatement(argList, value, fieldName);
        }
        QuerySuffix querySuffix = resolve(fieldName);
        if (querySuffix == Like) {
            value = CommonUtil.escapeLike(String.valueOf(value));
        } else if (querySuffix == Start) {
            value = CommonUtil.escapeStart(String.valueOf(value));
        }
        String columnName = querySuffix.resolveColumnName(fieldName);
        columnName = CommonUtil.convertColumn(columnName);
        return querySuffix.buildAndClauseWithArgs(columnName, argList, value);
    }

    String resolveColumnName(String fieldName) {
        String suffix = this.name();
        return fieldName.endsWith(suffix) ? fieldName.substring(0, fieldName.length() - suffix.length()) : fieldName;
    }

    private String buildAndClauseWithArgs(String columnName, List<Object> argList, Object value) {
        if (shouldIgnoreBlankStringForLikeOp(value)) {
            return null;
        }
        String placeHolderEx = ex.getEx(value);
        appendArg(argList, value, placeHolderEx);
        return buildAndClause(columnName, placeHolderEx);
    }

    private boolean shouldIgnoreBlankStringForLikeOp(Object value) {
        return getOp().contains("LIKE") && value instanceof String && StringUtils.isBlank((String) value);
    }

    private String buildAndClause(String columnName, String placeHolderEx) {
        if (!placeHolderEx.isEmpty()) {
            placeHolderEx = SPACE + placeHolderEx;
        }
        return columnName + SPACE + getOp() + placeHolderEx;
    }

    @SuppressWarnings("unchecked")
    private static void appendArg(List<Object> argList, Object value, String placeHolderEx) {
        if (value instanceof Collection) {
            appendCollectionArg(argList, (Collection<Object>) value);
        } else if (placeHolderEx.contains(REPLACE_HOLDER)) {
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
    interface Ex {
        Ex REPLACE_HOLDER = value -> Constant.REPLACE_HOLDER;
        Ex EMPTY = value -> Constant.EMPTY;
        Ex COLLECTION = value -> {
            int size = ((Collection<?>) value).size();
            String replaceHolders = IntStream.range(0, size).mapToObj(i -> Constant.REPLACE_HOLDER).collect(Collectors.joining(SEPARATOR));
            return CommonUtil.wrapWithParenthesis(StringUtils.trimToNull(replaceHolders));
        };

        String getEx(Object value);
    }

}
