package win.doyto.query.core;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static win.doyto.query.core.Constant.*;

/**
 * QuerySuffix
 *
 * @author f0rb
 */
@SuppressWarnings("squid:S00115")
@Getter
enum QuerySuffix {
    Not("!="),
    NotLike("NOT LIKE"),
    Start("LIKE"),
    Like,
    NotIn("NOT IN", Ex.collection),
    In("IN", Ex.collection),
    NotNull("IS NOT NULL", Ex.empty),
    Null("IS NULL", Ex.empty),
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
        Arrays.stream(values()).forEach(querySuffix -> sqlFuncMap.put(querySuffix, columnMeta -> columnMeta.defaultSql(querySuffix)));
    }

    private final String op;
    private final Ex ex;

    QuerySuffix() {
        this.op = name().toUpperCase();
        this.ex = Ex.placeHolder;
    }

    QuerySuffix(String op) {
        this(op, Ex.placeHolder);
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

    String resolveColumnName(String fieldName) {
        String suffix = this.name();
        return fieldName.endsWith(suffix) ? fieldName.substring(0, fieldName.length() - suffix.length()) : fieldName;
    }

    String getEx(Object value) {
        return ex.getEx(value);
    }

    @SuppressWarnings("squid:S1214")
    interface Ex {
        String getEx(Object value);

        Ex placeHolder = value -> REPLACE_HOLDER;
        Ex empty = value -> EMPTY;
        Ex collection = value -> {
            int size = ((Collection) value).size();
            return CommonUtil.wrapWithParenthesis(StringUtils.trimToNull(StringUtils.join(IntStream.range(0, size).mapToObj(i -> REPLACE_HOLDER).collect(Collectors.toList()), SEPARATOR)));
        };
    }

}
