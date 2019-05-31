package win.doyto.query.core;

import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import win.doyto.query.util.ColumnUtil;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static win.doyto.query.core.CommonUtil.wrapWithParenthesis;
import static win.doyto.query.core.QueryBuilder.SEPARATOR;

/**
 * QuerySuffix
 *
 * @author f0rb
 */
@SuppressWarnings("squid:S00115")
@Getter
enum QuerySuffix {
    Like, NotIn("NOT IN"), In, Gt(">"), Ge(">="), Lt("<"), Le("<="), NONE("=");

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
        List<String> suffixList = Arrays.stream(QuerySuffix.values()).filter(querySuffix -> querySuffix != NONE).map(Enum::name).collect(Collectors.toList());
        String suffixPtn = StringUtils.join(suffixList, "|");
        SUFFIX_PTN = Pattern.compile("(" + suffixPtn + ")$");

        for (QuerySuffix querySuffix : QuerySuffix.values()) {
            sqlFuncMap.put(querySuffix, columnMeta -> columnMeta.defaultSql(querySuffix));
        }

        sqlFuncMap.put(QuerySuffix.In, columnMeta -> buildSqlForCollection(columnMeta, QuerySuffix.In));
        sqlFuncMap.put(QuerySuffix.NotIn, columnMeta -> buildSqlForCollection(columnMeta, QuerySuffix.NotIn));
    }

    static String buildAndSql(String fieldName, @NonNull Object value, List<Object> argList) {
        QuerySuffix querySuffix = resolve(fieldName);
        if (querySuffix == Like) {
            value = ColumnUtil.escapeLike(String.valueOf(value));
        }
        return sqlFuncMap.get(querySuffix).apply(new ColumnMeta(fieldName, value, argList));
    }

    static QuerySuffix resolve(String fieldName) {
        QuerySuffix querySuffix = QuerySuffix.NONE;
        Matcher matcher = SUFFIX_PTN.matcher(fieldName);
        if (matcher.find()) {
            querySuffix = QuerySuffix.valueOf(matcher.group());
        }
        return querySuffix;
    }

    private static String buildSqlForCollection(ColumnMeta columnMeta, QuerySuffix querySuffix) {
        int size = ((Collection) columnMeta.value).size();
        String ex = StringUtils.join(IntStream.range(0, size).mapToObj(i -> QueryBuilder.REPLACE_HOLDER).collect(Collectors.toList()), SEPARATOR);
        return columnMeta.defaultSql(querySuffix, wrapWithParenthesis(StringUtils.trimToNull(ex)));
    }

    String resolveColumnName(String fieldName) {
        String suffix = this.name();
        return fieldName.endsWith(suffix) ? fieldName.substring(0, fieldName.length() - suffix.length()) : fieldName;
    }
}
