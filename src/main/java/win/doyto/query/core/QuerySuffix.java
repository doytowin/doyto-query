package win.doyto.query.core;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
    Like,
    NotIn("NOT IN"),
    In,
    NotNull("IS NOT NULL"),
    Null("IS NULL"),
    Gt(">"),
    Ge(">="),
    Lt("<"),
    Le("<="),
    NONE("=");

    QuerySuffix() {
        this.op = name().toUpperCase();
    }

    QuerySuffix(String op) {
        this.op = op;
    }

    private final String op;

    static final Pattern SUFFIX_PTN;

    private static final Map<QuerySuffix, Function<ColumnMeta, String>> sqlFuncMap = new EnumMap<>(QuerySuffix.class);

    static {
        List<String> suffixList = Arrays.stream(values()).filter(querySuffix -> querySuffix != NONE).map(Enum::name).collect(Collectors.toList());
        String suffixPtn = StringUtils.join(suffixList, "|");
        SUFFIX_PTN = Pattern.compile("(" + suffixPtn + ")$");

        for (QuerySuffix querySuffix : values()) {
            sqlFuncMap.put(querySuffix, columnMeta -> columnMeta.defaultSql(querySuffix));
        }

        sqlFuncMap.put(Null, columnMeta -> columnMeta.defaultSql(Null, ""));
        sqlFuncMap.put(NotNull, columnMeta -> columnMeta.defaultSql(NotNull, ""));

        sqlFuncMap.put(In, columnMeta -> buildSqlForCollection(columnMeta, In));
        sqlFuncMap.put(NotIn, columnMeta -> buildSqlForCollection(columnMeta, NotIn));
    }

    static String buildAndSql(String fieldName, Object value, List<Object> argList) {
        QuerySuffix querySuffix = resolve(fieldName);
        if (querySuffix == Like) {
            value = CommonUtil.escapeLike(String.valueOf(value));
        }
        return sqlFuncMap.get(querySuffix).apply(new ColumnMeta(fieldName, value, argList));
    }

    static QuerySuffix resolve(String fieldName) {
        QuerySuffix querySuffix = NONE;
        Matcher matcher = SUFFIX_PTN.matcher(fieldName);
        if (matcher.find()) {
            querySuffix = valueOf(matcher.group());
        }
        return querySuffix;
    }

    private static String buildSqlForCollection(ColumnMeta columnMeta, QuerySuffix querySuffix) {
        int size = ((Collection) columnMeta.value).size();
        return columnMeta.defaultSql(querySuffix, CommonUtil.generateReplaceHoldersForCollection(size));
    }

    String resolveColumnName(String fieldName) {
        String suffix = this.name();
        return fieldName.endsWith(suffix) ? fieldName.substring(0, fieldName.length() - suffix.length()) : fieldName;
    }
}
