package win.doyto.query.core;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import win.doyto.query.annotation.Enumerated;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.persistence.EnumType;

import static win.doyto.query.core.CommonUtil.containsOr;
import static win.doyto.query.core.CommonUtil.convertColumn;
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
        if (containsOr(fieldName)) {
            return processOrStatement(argList, value, fieldName);
        }
        QuerySuffix querySuffix = resolve(fieldName);
        if (querySuffix == Like) {
            value = CommonUtil.escapeLike(String.valueOf(value));
        } else if (querySuffix == Start) {
            value = CommonUtil.escapeStart(String.valueOf(value));
        }
        return querySuffix.buildAndClauseWithArgs(argList, value, fieldName, querySuffix.getEx(value));
    }

    private String buildAndClauseWithArgs(List<Object> argList, Object value, String fieldName, String ex) {
        if (getOp().contains("LIKE") && value instanceof String && StringUtils.isBlank((String) value)) {
            return null;
        }
        if (!ex.isEmpty()) {
            ex = SPACE + ex;
        }
        String columnName = resolveColumnName(fieldName);
        appendArgs(ex, value, argList);
        return convertColumn(columnName) + SPACE + getOp() + ex;
    }

    @SuppressWarnings("unchecked")
    private static void appendArgs(String ex, Object value, List<Object> argList) {
        if (value instanceof Collection) {
            Collection<Object> collection = (Collection<Object>) value;
            if (collection.isEmpty()) {
                return;
            }
            Object next = collection.iterator().next();
            if (next instanceof Enum<?>) {
                Enumerated enumerated = next.getClass().getAnnotation(Enumerated.class);
                boolean isString = enumerated != null && enumerated.value() == EnumType.STRING;
                collection.stream()
                          .map(element -> isString ? element.toString() : ((Enum<?>) element).ordinal())
                          .forEach(argList::add);
            } else {
                argList.addAll(collection);
            }
        } else if (ex.contains(REPLACE_HOLDER)) {
            argList.add(value);
        }
    }

    String resolveColumnName(String fieldName) {
        String suffix = this.name();
        return fieldName.endsWith(suffix) ? fieldName.substring(0, fieldName.length() - suffix.length()) : fieldName;
    }

    String getEx(Object value) {
        return ex.getEx(value);
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
