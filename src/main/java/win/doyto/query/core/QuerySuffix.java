package win.doyto.query.core;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static win.doyto.query.core.CommonUtil.containsOr;
import static win.doyto.query.core.Constant.SEPARATOR;
import static win.doyto.query.core.Constant.WHERE;

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
    NONE("=");

    private static final Pattern SUFFIX_PTN;
    private static final Map<QuerySuffix, Function<ColumnMeta, String>> sqlFuncMap = new EnumMap<>(QuerySuffix.class);

    static {
        List<String> suffixList = Arrays.stream(values()).filter(querySuffix -> querySuffix != NONE).map(Enum::name).collect(Collectors.toList());
        String suffixPtn = StringUtils.join(suffixList, "|");
        SUFFIX_PTN = Pattern.compile("(" + suffixPtn + ")$");
        Arrays.stream(values()).forEach(querySuffix -> sqlFuncMap.put(querySuffix, querySuffix::buildAndSql));
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

    static String buildAndSql(List<Object> argList, Object value, String fieldName) {
        if (containsOr(fieldName)) {
            final String alias;
            int indexOfDot = fieldName.indexOf('.') + 1;
            if (indexOfDot > 0) {
                alias = fieldName.substring(0, indexOfDot);
                fieldName = fieldName.substring(indexOfDot);
            } else {
                alias = "";
            }
            Object finalValue = value;
            String andSql = Arrays.stream(CommonUtil.splitByOr(fieldName))
                                  .map(s -> buildAndSql(argList, finalValue, alias + s))
                                  .collect(Collectors.joining(Constant.SPACE_OR));
            return CommonUtil.wrapWithParenthesis(andSql);
        }
        QuerySuffix querySuffix = resolve(fieldName);
        if (querySuffix == Like) {
            value = CommonUtil.escapeLike(String.valueOf(value));
        } else if (querySuffix == Start) {
            value = CommonUtil.escapeStart(String.valueOf(value));
        }
        return sqlFuncMap.get(querySuffix).apply(new ColumnMeta(fieldName, value, argList));
    }

    static String buildWhereSql(List<Object> argList, Object value, String fieldName) {
        return WHERE + buildAndSql(argList, value, fieldName);
    }

    private String buildAndSql(ColumnMeta columnMeta) {
        return columnMeta.defaultSql(this, this.getEx(columnMeta.value));
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
